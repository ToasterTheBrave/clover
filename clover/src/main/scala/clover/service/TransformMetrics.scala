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
          println("Running transformers on " + measurement.name + " : " + measurement.partitions.mkString(",") + " : " + measurement.valueField)
          var measurementsDF = getInitialMeasurements(metricSource.database, measurement, 10000)
          measurementsDF = reloadMeasurements(metricSource.database, measurement, measurementsDF, 2000)
          runTransformers(transformers, measurement, measurementsDF)
        })
      })
    }
  }

  def runTransformers(transformers: List[Transformer], measurement: Measurement, metricsDF: DataFrame): Unit = {
    transformers.foreach(transformer => {
      val transformerDataStore = cloverStore.setDB(transformer.databaseName())
      val lastProcessedTime = transformerDataStore.getLastProcessedTime(measurement.name)
      val transformedMetrics = transformer.transform(metricsDF, measurement, lastProcessedTime)
      writeTransformations(transformerDataStore, transformer, measurement, transformedMetrics)
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

  def getInitialMeasurements(database: InfluxDBStore, measurement: Measurement, count: Integer): DataFrame = {
    val recent = database
      .getRecent(measurement.name, measurement.partitions, measurement.valueField, count)

    convertMeasurementsToDF(measurement, recent)
  }

  def reloadMeasurements(database: InfluxDBStore, measurement: Measurement, df: DataFrame, limit: Integer): DataFrame = {
    val lastTime = df.orderBy(desc("time")).take(1).head(0).asInstanceOf[Long]

    val lastTimeString = Util.timeLongToString(lastTime)

    val since = database
      .getSince(measurement.name, measurement.partitions, measurement.valueField, lastTimeString)

    val sinceDF = convertMeasurementsToDF(measurement, since)

    sinceDF
      .union(df)
      .orderBy(desc("time"))
      .limit(limit)
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
        measurement.name + "_" + measurement.valueField,
        tags,
        data
      )
    })
  }

}
