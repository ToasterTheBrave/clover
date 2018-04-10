package clover.service

import clover.{Config, Measurement, MetricSource}
import clover.algorithms.{Algorithm, LinearRegressionAlgorithm}
import clover.datastores.InfluxDBStore
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

object EvaluateMetrics {

  val sparkSession = SparkSession
    .builder()
    .master("local")
    .appName("Clover")
    .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .getOrCreate()

  val metricSources:List[MetricSource] = Config.metricSources()
  val cloverStore:InfluxDBStore = Config.cloverStore()

  def main(args: Array[String]) {
    run(List(new LinearRegressionAlgorithm(sparkSession)))
  }

  def run(algorithms: List[Algorithm]): Unit = {
    while(true) {
      metricSources.foreach(metricSource => {
        metricSource.measurements.foreach(measurement => {
          runEvaluations(algorithms, measurement)
        })
      })
    }
  }

  def runEvaluations(algorithms: List[Algorithm], measurement: Measurement): Unit = {
    algorithms.foreach(algorithm => {
      try {
        cloverStore.setDB(algorithm.evaluatedDatabaseName())
        val lastEvaluatedTime = cloverStore.getLastProcessedTime(measurement.name + "_" + measurement.valueField)
        println("Running evaluation on " + measurement.name + " : " + measurement.valueField + " : " + lastEvaluatedTime)

        cloverStore.setDB(algorithm.transformedDatabaseName())
        val measurementsDF = getTransformedDF(cloverStore, measurement, lastEvaluatedTime)

        val model = algorithm.loadModel(measurement)

        val evaluatedMetrics = algorithm.evaluate(measurement, model, measurementsDF)

        val errorData = evaluatedErrorData(measurement, evaluatedMetrics)

        cloverStore.setDB(algorithm.evaluatedDatabaseName())
        writeEvaluations(cloverStore, measurement, errorData)
      } catch {
        case e: Exception => println("Exception thrown! - " + e.getMessage)
      }
    })
  }

  def writeEvaluations(dataStore: InfluxDBStore, measurement: Measurement, evaluationsDF: DataFrame): Unit = {
    evaluationsDF.collect.foreach(row => {
      val valuesMap = row.getValuesMap(evaluationsDF.columns)

      val tags = measurement.partitions.map(partition_name => {
        (partition_name, valuesMap(partition_name).asInstanceOf[String])
      }).toMap

      val dataPoints = valuesMap.filterKeys(x => {
        !(measurement.partitions ++ List("time")).contains(x)
      })

      val data = List(
        (valuesMap("time").asInstanceOf[String], dataPoints)
      )

      dataStore.write(
        measurement.name + "_" + measurement.valueField,
        tags,
        data
      )
    })
  }

  def evaluatedErrorData(measurement: Measurement, evaluatedDF: DataFrame): DataFrame = {
    val newRows = evaluatedDF.collect.map(row => {
      val valuesMap = row.getValuesMap(List("time", measurement.valueField, "prediction", "meanAbsoluteError") ++ measurement.partitions)
      val actual = valuesMap(measurement.valueField).asInstanceOf[Double]
      val prediction = valuesMap("prediction").asInstanceOf[Double]
      val meanAbsoluteError = valuesMap("meanAbsoluteError").asInstanceOf[Double]
      val lowThreshold = prediction - meanAbsoluteError
      val highThreshold = prediction + meanAbsoluteError
      val error = if(actual <= highThreshold && actual >= lowThreshold) {
        0.0
      } else if(actual > highThreshold) {
        val diff = actual - highThreshold
        diff / (2 * meanAbsoluteError)
      } else if(actual < lowThreshold) {
        val diff = actual - lowThreshold
        diff / (2 * meanAbsoluteError)
      } else {
        throw new Exception("Invalid condition")
      }

      val partitionValues = measurement.partitions.map(partition => {
        valuesMap(partition).asInstanceOf[String]
      })

      val mergedList = List(
        valuesMap("time").asInstanceOf[String],
        actual,
        prediction,
        meanAbsoluteError,
        error
      ) ++ partitionValues
      Row(mergedList: _*)
    })

    val columns = List("time", measurement.valueField, "prediction", "meanAbsoluteError", "error") ++ measurement.partitions
    val schema = columns.map(x => {
      x match {
        case "prediction" => StructField(x, DoubleType)
        case "meanAbsoluteError" => StructField(x, DoubleType)
        case measurement.valueField => StructField(x, DoubleType)
        case "error" => StructField(x, DoubleType)
        case _ => StructField(x, StringType)
      }
    })

    sparkSession.createDataFrame(sparkSession.sparkContext.parallelize(newRows), StructType(schema))
  }

  def getTransformedDF(database: InfluxDBStore, measurement: Measurement, lastEvalutedTime: String): DataFrame = {
    val dbResponse = database.getAllSince(measurement.name + "_" + measurement.valueField, lastEvalutedTime, 1000)
    convertTransformedMeasurementsToDF(measurement, dbResponse)
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
        if (field._2.getClass.toString.equals("class scala.math.BigDecimal")) {
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
