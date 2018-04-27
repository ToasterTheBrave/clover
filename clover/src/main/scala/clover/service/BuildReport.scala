package clover.service

import java.io.{File, PrintWriter}

import clover.datastores.InfluxDBStore
import clover._
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import pureconfig.error.ConfigReaderFailures
import pureconfig.loadConfigFromFiles

object BuildReport {

  val sparkSession = SparkSession
    .builder()
    .master("local")
    .appName("Clover")
    .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .getOrCreate()

  import sparkSession.implicits._

  def main(args: Array[String]) {
    val configFile = args(0)
    val configFiles = Traversable(java.nio.file.Paths.get(s"/home/truppert/projects/master-project/clover/src/main/resources/${configFile}.conf"))

    val simpleConfig: Either[ConfigReaderFailures, CloverConfig] = loadConfigFromFiles[CloverConfig](configFiles)
    simpleConfig match {
      case Left(ex) => {
        ex.toList.foreach(println)
        throw new Exception("Error loading config")
      }
      case Right(config) => {
        val cloverStore = new InfluxDBStore(config.cloverStore.host, config.cloverStore.port).connect()
        val datetime = args(1)
        run(cloverStore, config, datetime)
      }
    }

    System.exit(0)
  }

  def run(cloverStore: InfluxDBStore, config: CloverConfig, datetime: String): Unit = {
    // Get a list of all evaluated measurements
    val allMeasurements = getAllMeasurements(config.metricSources)

    // Get all data points over the last hour
    val lastHourDF = getAllMetricsLastHourDF(cloverStore, allMeasurements, datetime)

    // Decide on what we need
    val mostAnomalous = getMostAnomalous(lastHourDF, 10)

    // Build a report using the data points decided on
    val reportData = buildReportData(mostAnomalous)

    // Actually write this report to a file for use by the report web ui
    writeReportData(reportData, s"${config.reportLocation}/$datetime.js")
  }

  def getAllMeasurements(metricSources: List[MetricSource]): List[Measurement] = {
    metricSources.map(metricSource => {
      metricSource.measurements
    }).reduce((a, b) => {
      a ++ b
    })
  }

  def getAllMetricsLastHourDF(cloverStore: InfluxDBStore, measurements: List[Measurement], endTime: String): DataFrame = {
    cloverStore.setDB("linear_regression_evaluated")
    measurements.map(measurement => {
      val measurementName = measurement.name.replaceAll("\\.", "_")

      val reportEndTime = Util.timeLongToString(Util.timeStringToLong(endTime) + (15 * 60 * 1000))
      val reportStartTime = Util.timeLongToString(Util.timeStringToLong(reportEndTime) - (60 * 60 * 1000))

      val allBetweenData = cloverStore.getAllBetween(measurementName + "_" + measurement.valueField, reportStartTime, reportEndTime)
      val neededData = allBetweenData.map(dataPoint => {
        val tags = measurement.partitions.map(x => {
          x + ": " + dataPoint(x)
        }).mkString(", ")
        (
          dataPoint("time").asInstanceOf[String],
          measurementName,
          dataPoint(measurement.valueField).asInstanceOf[BigDecimal].toDouble,
          measurement.valueField,
          tags,
          dataPoint("error").asInstanceOf[BigDecimal].toDouble,
          dataPoint("prediction").asInstanceOf[BigDecimal].toDouble,
          dataPoint("meanAbsoluteError").asInstanceOf[BigDecimal].toDouble
        )
      })

      neededData.toDF("time", "measurement", "value", "valueField", "tags", "error", "prediction", "meanAbsoluteError")

    }).reduce((a, b) => {
      a.union(b)
    })
  }


  def getMostAnomalous(df: DataFrame, count: Int): DataFrame = {
    val columns = df.columns.map(col)
    val window = Window.partitionBy($"time").orderBy($"absError".desc)

    df.filter(row => {
      row.get(row.fieldIndex("error")) != 0
    })
      .withColumn("absError", abs($"error"))
      .withColumn("rn", row_number().over(window))
      .where(s"rn <= $count")
      .select(columns: _*)
  }

  def buildReportData(df: DataFrame): Map[String, Array[String]] = {
    val tuples = df.collect().map(row => {
      val valuesMap = row.getValuesMap(df.columns)

      val measurement = valuesMap("measurement").asInstanceOf[String]
      val valuesField = valuesMap("valueField").asInstanceOf[String]
      val tags = valuesMap("tags").asInstanceOf[String]
      val value = valuesMap("value").asInstanceOf[Double]

      val column = "'" +
        measurement +
        " - " + valuesField +
        " - " + tags +
        "'"
      val datetime = valuesMap("time").asInstanceOf[String]
      val error = valuesMap("error").asInstanceOf[Double]
      val prediction = valuesMap("prediction").asInstanceOf[Double]
      val meanAbsoluteError = valuesMap("meanAbsoluteError").asInstanceOf[Double]
      val tooltip = s"$datetime\n$measurement - $valuesField\n$tags\nError: $error\nValue: $value\nExpected: $prediction +/- $meanAbsoluteError"
      (datetime, Map(column -> (error, tooltip)))
    })

    val columns = tuples.flatMap(x => {
      x._2.keys
    }).distinct

    val groupedByDate = tuples.groupBy(x => {
      x._1
    }).map(x => {
      val time = x._1
      val colMap = x._2.map(_._2).reduce((a, b) => {
        a ++ b
      })
      (time, colMap)
    })

    val rows = groupedByDate.map(oneTime => {
      val data = columns.map(col => {
        val point = oneTime._2.getOrElse(col, (null, ""))
        s"${point._1}, `${point._2}`"
      })
      "[new Date('" + oneTime._1 + "'), " + data.mkString(", ") + "]"
    }).toArray

    Map(
      "columns" -> columns,
      "rows" -> rows
    )

  }

  def writeReportData(reportData: Map[String, Array[String]], location: String): Unit = {
    val output = "columns = [\n" +
      "  " + reportData("columns").mkString(",\n  ") + "\n" +
      "];\n\n" +
      "rows = [\n" +
      "  " + reportData("rows").mkString(",\n  ") + "\n" +
      "];\n"

    val pw = new PrintWriter(new File(location))
    pw.write(output)
    pw.close()
  }
}
