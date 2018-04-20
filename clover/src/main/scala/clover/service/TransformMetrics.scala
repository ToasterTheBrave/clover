package clover.service

import clover.transformers.{LinearRegressionTransformer, Transformer}
import clover._
import clover.datastores.InfluxDBStore
import org.apache.spark.sql.functions.desc
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

object TransformMetrics {

  val sparkSession:SparkSession = SparkSession
    .builder()
    .master("local")
    .appName("Clover")
    .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .getOrCreate()

  val metricSources:List[MetricSource] = Config.metricSources()
  val cloverStore:InfluxDBStore = Config.cloverStore()

  def main(args: Array[String]) {
    run(List(new LinearRegressionTransformer(sparkSession)))
  }

  def run(transformers: List[Transformer]): Unit = {
    while(true) {
      metricSources.foreach(metricSource => {
        metricSource.measurements.foreach(measurement => {
          runTransformers(transformers, metricSource, measurement)
        })
      })
    }
  }

  def runTransformers(transformers: List[Transformer], metricSource: MetricSource, measurement: Measurement): Unit = {
    transformers.foreach(transformer => {
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

      val measurementsDF = getMeasurementsSinceLastRun(metricSource.database, measurement, lastTransformedTime)

      val transformedMetrics = transformer.transform(measurementsDF, measurement, lastTransformedTime)

      cloverStore.setDB(transformer.databaseName())
      writeTransformations(cloverStore, transformer, measurement, transformedMetrics)
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
      .getSince(measurementName, measurement.partitions, measurement.valueField, lastTransformedTime, 17, 2000)

    println("Measurements to transform: " + untransformed.size)

    convertMeasurementsToDF(measurement, untransformed)
  }

  def writeTransformations(transformerDataStore: InfluxDBStore, transformer: Transformer, measurement: Measurement, metricsDF: DataFrame): Unit = {
    val columns = metricsDF.columns

    metricsDF.collect.foreach(metric => {

      val fields = metric.getValuesMap(columns)

      val tags = measurement.partitions.map(partition_name => {
        (partition_name, fields.getOrElse(partition_name, ""))
      }).toMap

      val time = Util.timeLongToString(fields.getOrElse("time", 0L))

      val dataPoints = fields.filterKeys(x => {
        !(measurement.partitions ++ List("time")).contains(x)
      })

      val data = List(
        (time, dataPoints)
      )

      transformerDataStore.write(
        measurement.name.replaceAll("\\.", "_") + "_" + measurement.valueField,
        tags,
        data
      )
    })
  }

}
