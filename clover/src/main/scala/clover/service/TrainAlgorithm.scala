package clover.service

import clover._
import clover.algorithms.{Algorithm, LinearRegressionAlgorithm}
import clover.datastores.InfluxDBStore
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

object TrainAlgorithm {

  val sparkSession:SparkSession = SparkSession
    .builder()
    .master("local")
    .appName("Clover")
    .getOrCreate()

  val metricSources:List[MetricSource] = Config.metricSources()
  val cloverStore = Config.cloverStore()

  def main(args: Array[String]) {
    run(List(new LinearRegressionAlgorithm(sparkSession)))
  }

  def run(algorithms: List[Algorithm]): Unit = {
    metricSources.foreach(metricSource => {
      metricSource.measurements.foreach(measurement => {
        println("Running training on " + measurement.name + " : " + measurement.partitions.mkString(",") + " : " + measurement.valueField)
        runTraining(algorithms, measurement)
      })
    })
  }

  def runTraining(algorithms: List[Algorithm], measurement: Measurement): Unit = {
    algorithms.foreach(algorithm => {
      val algorithmDataStore = cloverStore.setDB(algorithm.databaseName())
      val measurementsDF = getRecentTransformedMeasurements(algorithmDataStore, measurement, 100000)

      try {
        val model = algorithm.train(measurement, measurementsDF)
        val modelFileLocation = algorithm.modelLocation() + measurement.name + "-" + measurement.valueField
        model.save(modelFileLocation)
      } catch {
        case e: Exception => println("Exception thrown! - " + e.getMessage)
      }
    })
  }

  def getRecentTransformedMeasurements(database: InfluxDBStore, measurement: Measurement, count: Integer): DataFrame = {
    val recent = database
      .getAllRecent(measurement.name + "_" + measurement.valueField, count)

    convertTransformedMeasurementsToDF(measurement, recent)
  }

  def convertTransformedMeasurementsToDF(measurement: Measurement, data: List[Map[String, Any]]): DataFrame = {
    val firstRow = data.head
    val columns = firstRow.keys.toList
    val schema = columns.map(x => {
      val classString = firstRow(x).getClass.toString
      classString match {
        case "class java.lang.Long" => StructField(x, LongType)
        case "class java.lang.Double" => StructField(x, DoubleType)
        case "class scala.math.BigDecimal" => StructField(x, DoubleType)
        case "class java.lang.String" => StructField(x, StringType)
        case _ => throw new Exception("Unsupported field type: " + classString)
      }
    })

    val rows = data.map(x => {
      val values = x.map(field => {
        if(field._2.getClass.toString.equals("class scala.math.BigDecimal")) {
          field._2.asInstanceOf[BigDecimal].doubleValue
        } else {
          field._2
        }
      })

      Row(values.toList: _*)
    })

    sparkSession.createDataFrame(sparkSession.sparkContext.parallelize(rows), StructType(schema))
  }

}
