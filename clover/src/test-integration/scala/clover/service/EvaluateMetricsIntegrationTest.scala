package clover.service

import clover.{Measurement, Util}
import clover.datastores.InfluxDBStore
import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterEach, FunSuite}

class EvaluateMetricsIntegrationTest extends FunSuite with BeforeAndAfterEach {

  val influxDB:InfluxDBStore = new InfluxDBStore("localhost", 8086).connect().setDB("clover_test")

  val sparkSession:SparkSession = SparkSession
    .builder()
    .master("local")
    .appName("Clover")
    .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .getOrCreate()

  import sparkSession.implicits._

  override def afterEach() {
    influxDB.deleteTable("test_measurement_1_test_value_field")
  }

  test("writeEvaluations - correctly writes to InfluxDB") {
    val measurement = Measurement("test_measurement_1", List("test_partition_1", "test_partition_2"), "test_value_field")

    val evaluationsDF = List(
      ("2017-12-04T12:03:01Z", "test_partition_1_name", "test_partition_2_name", 10.001, 10.011, 0.02, 0.01),
      ("2017-12-04T12:03:02Z", "test_partition_1_name", "test_partition_2_name", 20.002, 20.012, 0.02, 0.02),
      ("2017-12-04T12:03:03Z", "test_partition_1_name", "test_partition_2_name", 30.003, 30.013, 0.02, 0.03),
      ("2017-12-04T12:03:04Z", "test_partition_1_name", "test_partition_2_name", 40.004, 40.014, 0.02, 0.04),
      ("2017-12-04T12:03:05Z", "test_partition_1_name", "test_partition_2_name", 50.005, 50.015, 0.02, 0.05)
    ).toDF("time", "test_partition_1", "test_partition_2", "test_value_field", "prediction", "meanAbsoluteError", "error")

    EvaluateMetrics.writeEvaluations(influxDB, measurement, evaluationsDF)

    assert(
      influxDB.getAllRecent("test_measurement_1_test_value_field", 5)
      ==
      List(
        Map(
          "time" -> "2017-12-04T12:03:05Z",
          "test_partition_1" -> "test_partition_1_name",
          "test_partition_2" -> "test_partition_2_name",
          "test_value_field" -> 50.005,
          "prediction" -> 50.015,
          "meanAbsoluteError" -> 0.02,
          "error" -> 0.05
        ),
        Map(
          "time" -> "2017-12-04T12:03:04Z",
          "test_partition_1" -> "test_partition_1_name",
          "test_partition_2" -> "test_partition_2_name",
          "test_value_field" -> 40.004,
          "prediction" -> 40.014,
          "meanAbsoluteError" -> 0.02,
          "error" -> 0.04
        ),
        Map(
          "time" -> "2017-12-04T12:03:03Z",
          "test_partition_1" -> "test_partition_1_name",
          "test_partition_2" -> "test_partition_2_name",
          "test_value_field" -> 30.003,
          "prediction" -> 30.013,
          "meanAbsoluteError" -> 0.02,
          "error" -> 0.03
        ),
        Map(
          "time" -> "2017-12-04T12:03:02Z",
          "test_partition_1" -> "test_partition_1_name",
          "test_partition_2" -> "test_partition_2_name",
          "test_value_field" -> 20.002,
          "prediction" -> 20.012,
          "meanAbsoluteError" -> 0.02,
          "error" -> 0.02
        ),
        Map(
          "time" -> "2017-12-04T12:03:01Z",
          "test_partition_1" -> "test_partition_1_name",
          "test_partition_2" -> "test_partition_2_name",
          "test_value_field" -> 10.001,
          "prediction" -> 10.011,
          "meanAbsoluteError" -> 0.02,
          "error" -> 0.01
        )
      )
    )


  }
}
