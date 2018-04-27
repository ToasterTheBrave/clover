package clover.service

import clover.transformers.{LinearRegressionTransformer, Transformer}
import clover._
import clover.datastores.InfluxDBStore
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import pureconfig.error.ConfigReaderFailures
import pureconfig.loadConfigFromFiles

object TransformMetrics {

  val sparkSession:SparkSession = SparkSession
    .builder()
    .master("local")
    .appName("Clover")
    .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .getOrCreate()

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
        run(cloverStore, config, List(new LinearRegressionTransformer(sparkSession)))
      }
    }
  }

  def run(cloverStore: InfluxDBStore, config: CloverConfig, transformers: List[Transformer]): Unit = {
    while(true) {
      config.metricSources.foreach(metricSource => {
        val metricStore = new InfluxDBStore(metricSource.host, metricSource.port).connect().setDB(metricSource.db)
        metricSource.measurements.foreach(measurement => {
          runTransformers(cloverStore, transformers, metricStore, measurement)
        })
      })
    }
  }

  def runTransformers(cloverStore: InfluxDBStore, transformers: List[Transformer], metricStore: InfluxDBStore, measurement: Measurement): Unit = {
    transformers.foreach(transformer => {
      Thread.sleep(1000)
      try {
        cloverStore.setDB(transformer.databaseName())
        val lastTransformedTime = cloverStore.getLastProcessedTime(measurement.name.replaceAll("\\.", "_") + "_" + measurement.valueField)
        val behindAsMillis = System.currentTimeMillis() - Util.timeStringToLong(lastTransformedTime)
        val behindAsSeconds = behindAsMillis / 1000
        val behindHours = (behindAsSeconds / 3600).formatted("%02d")
        val behindMinutes = (behindAsSeconds % 3600 / 60).formatted("%02d")
        val behindSeconds = (behindAsSeconds % 3600 % 60).formatted("%02d")
        val behindTime = s"$behindHours:$behindMinutes:$behindSeconds"

        println
        println("Running transformers on " + measurement.name.replaceAll("\\.", "_") + " : " + measurement.partitions.mkString(",") + " : " + measurement.valueField)
        println("Last transformed: " + lastTransformedTime)
        println("Currently behind by " + behindTime)

        val measurementsDF = getMeasurementsSinceLastRun(metricStore, measurement, lastTransformedTime)

        val transformedMetrics = transformer.transform(measurementsDF, measurement, lastTransformedTime)

        cloverStore.setDB(transformer.databaseName())
        writeTransformations(cloverStore, transformer, measurement, transformedMetrics)
      } catch {
        case e: Exception => println("Exception thrown! - " + e.getMessage)

      }
    })
  }

  def convertMeasurementsToDF(measurement: Measurement, data: List[Map[String, Any]]): DataFrame = {
    val columns = List("time") ++ measurement.partitions ++ List(measurement.valueField)
    val schema = columns.map(x => {
      x match {
        case "time" => StructField(x, LongType)
        case measurement.valueField => StructField(x, DoubleType)
        case _ => StructField(x, StringType)
      }
    })

    val rows = data.map(x => {
      val partitionValues = measurement.partitions.map(partition => {
        x.getOrElse(partition, "").asInstanceOf[String]
      })

      val time = Util.timeStringToLong(x.getOrElse("time", "").asInstanceOf[String])
      val valueField = x.getOrElse(measurement.valueField, 0).asInstanceOf[BigDecimal].doubleValue

      val mergedList = List(time) ++ partitionValues ++ List(valueField)
      val row = Row(mergedList: _*)

      row
    })

    sparkSession.createDataFrame(sparkSession.sparkContext.parallelize(rows), StructType(schema))
  }

  def getMeasurementsSinceLastRun(database: InfluxDBStore, measurement: Measurement, lastTransformedTime: String): DataFrame = {
    // Special case for rails measurements because they use periods
    val measurementName = if(measurement.name.startsWith("rails")) {
      "\"" + measurement.name + "\""
    } else {
      measurement.name
    }

    val untransformed = database
      .getSince(measurementName, measurement.partitions, measurement.valueField, lastTransformedTime, 17, 10000)

    println("Measurements to transform: " + untransformed.size)

    convertMeasurementsToDF(measurement, untransformed)
  }

  def writeTransformations(transformerDataStore: InfluxDBStore, transformer: Transformer, measurement: Measurement, metricsDF: DataFrame): Unit = {
    val columns = metricsDF.columns

    val data = metricsDF.collect.map(row => {
      val fields = row.getValuesMap(columns)

      val tags = measurement.partitions.map(partition_name => {
        (partition_name, fields.getOrElse(partition_name, ""))
      }).toMap

      val time = Util.timeLongToString(fields.getOrElse("time", 0L))

      val dataPoints = fields.filterKeys(x => {
        !(measurement.partitions ++ List("time")).contains(x)
      })

      Map(
        "time" -> time,
        "measurement" -> (measurement.name.replaceAll("\\.", "_") + "_" + measurement.valueField),
        "tags" -> tags,
        "dataPoints" -> dataPoints
      )

    })

    val groupedByTags = data.groupBy(x => {
      x("tags")
    }).values

    groupedByTags.foreach(x => {

      val pointData = x.map(a => {
        Map(
          "measurement" -> a("measurement"),
          "tags" -> a("tags"),
          "data" -> List((a("time"), a("dataPoints")))
        )
      })
        .reduce((a, b) => {
        Map(
          "measurement" -> a("measurement"),
          "tags" -> a("tags"),
          "data" ->( a("data").asInstanceOf[List[(String, String)]] ++ b("data").asInstanceOf[List[(String, String)]])
        )
      })

      transformerDataStore.write(
        pointData("measurement").asInstanceOf[String],
        pointData("tags").asInstanceOf[Map[String, String]],
        pointData("data").asInstanceOf[List[(String, Map[String, Any])]]
      )
    })
  }

}
