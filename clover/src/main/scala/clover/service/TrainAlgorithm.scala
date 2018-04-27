package clover.service

import clover._
import clover.algorithms.{Algorithm, LinearRegressionAlgorithm}
import clover.datastores.InfluxDBStore
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import pureconfig.error.ConfigReaderFailures
import pureconfig.loadConfigFromFiles
object TrainAlgorithm {

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
        run(cloverStore, config, List(new LinearRegressionAlgorithm(sparkSession)))
      }
    }
    System.exit(0)
  }

  def run(cloverStore: InfluxDBStore, config: CloverConfig, algorithms: List[Algorithm]): Unit = {
    config.metricSources.foreach(metricSource => {
      metricSource.measurements.foreach(measurement => {
        println
        println("Running training on " + measurement.name + " : " + measurement.partitions.mkString(",") + " : " + measurement.valueField)
        runTraining(cloverStore, algorithms, measurement)
      })
    })
  }

  def runTraining(cloverStore: InfluxDBStore, algorithms: List[Algorithm], measurement: Measurement): Unit = {
    algorithms.foreach(algorithm => {
      val transformedDataStore = cloverStore.setDB(algorithm.transformedDatabaseName())
      val measurementsDF = getRecentTransformedMeasurements(transformedDataStore, measurement, 50000)

      try {
        val model = algorithm.train(measurement, measurementsDF)
        val modelFileLocation = algorithm.modelLocation() + measurement.name.replaceAll("\\.", "_") + "-" + measurement.valueField
        val r2 = model.summary.r2adj
        if(r2 >= .3) {
          model.save(modelFileLocation)
        } else {
          println(s"WARNING - not saving model because of low r2: ${r2}")
        }
      } catch {
        case e: Exception => println("Exception thrown! - " + e.getMessage)
      }
    })
  }

  def getRecentTransformedMeasurements(database: InfluxDBStore, measurement: Measurement, count: Integer): DataFrame = {
    val recent = database
      .getAllRecent(measurement.name.replaceAll("\\.", "_") + "_" + measurement.valueField, count)

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
        case _ => throw new Exception("Unsupported field type: " + classString + "\n")
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
