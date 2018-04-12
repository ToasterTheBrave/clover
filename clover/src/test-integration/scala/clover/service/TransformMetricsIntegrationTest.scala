package clover.service

import clover.{Measurement, Util}
import clover.datastores.InfluxDBStore
import clover.transformers.Transformer
import org.apache.spark.sql.SparkSession
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, FunSuite}

class TransformMetricsIntegrationTest extends FunSuite with BeforeAndAfterEach with MockitoSugar {

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

  test("writeTransformations - data as expected to InfluxDB") {
    val measurement = Measurement("test_measurement_1", List("test_partition_1", "test_partition_2"), "test_value_field")

    val metricsDF = List(
      (Util.timeStringToLong("2017-12-04T12:03:01Z"), "test_partition_1_name", "test_partition_2_name", 10.001, 1.01, 1),
      (Util.timeStringToLong("2017-12-04T12:03:02Z"), "test_partition_1_name", "test_partition_2_name", 20.002, 2.02, 2),
      (Util.timeStringToLong("2017-12-04T12:03:03Z"), "test_partition_1_name", "test_partition_2_name", 30.003, 3.03, 3),
      (Util.timeStringToLong("2017-12-04T12:03:04Z"), "test_partition_1_name", "test_partition_2_name", 40.004, 4.04, 4),
      (Util.timeStringToLong("2017-12-04T12:03:05Z"), "test_partition_1_name", "test_partition_2_name", 50.005, 5.05, 5)
    ).toDF("time", "test_partition_1", "test_partition_2", "test_value_field", "test_calculated_field_1", "test_calculated_field_2")

    val transformer = mock[Transformer]
    when(transformer.databaseName())
      .thenReturn("clover_test")

    TransformMetrics.writeTransformations(influxDB, transformer, measurement, metricsDF)

    assert(
      influxDB.getRecent("test_measurement_1_test_value_field", List("test_partition_1", "test_partition_2"), "test_value_field", 5)
      ==
      List(
        Map(
          "time" -> "2017-12-04T12:03:05Z",
          "test_partition_1" -> "test_partition_1_name",
          "test_partition_2" -> "test_partition_2_name",
          "test_value_field" -> 50.005
        ),
        Map(
          "time" -> "2017-12-04T12:03:04Z",
          "test_partition_1" -> "test_partition_1_name",
          "test_partition_2" -> "test_partition_2_name",
          "test_value_field" -> 40.004
        ),
        Map(
          "time" -> "2017-12-04T12:03:03Z",
          "test_partition_1" -> "test_partition_1_name",
          "test_partition_2" -> "test_partition_2_name",
          "test_value_field" -> 30.003
        ),
        Map(
          "time" -> "2017-12-04T12:03:02Z",
          "test_partition_1" -> "test_partition_1_name",
          "test_partition_2" -> "test_partition_2_name",
          "test_value_field" -> 20.002
        ),
        Map(
          "time" -> "2017-12-04T12:03:01Z",
          "test_partition_1" -> "test_partition_1_name",
          "test_partition_2" -> "test_partition_2_name",
          "test_value_field" -> 10.001
        )
      )
    )

    assert(
      influxDB.getRecent("test_measurement_1_test_value_field", List("test_partition_1", "test_partition_2"), "test_calculated_field_1", 5)
        ==
        List(
          Map(
            "time" -> "2017-12-04T12:03:05Z",
            "test_partition_1" -> "test_partition_1_name",
            "test_partition_2" -> "test_partition_2_name",
            "test_calculated_field_1" -> 5.05
          ),
          Map(
            "time" -> "2017-12-04T12:03:04Z",
            "test_partition_1" -> "test_partition_1_name",
            "test_partition_2" -> "test_partition_2_name",
            "test_calculated_field_1" -> 4.04
          ),
          Map(
            "time" -> "2017-12-04T12:03:03Z",
            "test_partition_1" -> "test_partition_1_name",
            "test_partition_2" -> "test_partition_2_name",
            "test_calculated_field_1" -> 3.03
          ),
          Map(
            "time" -> "2017-12-04T12:03:02Z",
            "test_partition_1" -> "test_partition_1_name",
            "test_partition_2" -> "test_partition_2_name",
            "test_calculated_field_1" -> 2.02
          ),
          Map(
            "time" -> "2017-12-04T12:03:01Z",
            "test_partition_1" -> "test_partition_1_name",
            "test_partition_2" -> "test_partition_2_name",
            "test_calculated_field_1" -> 1.01
          )
        )
    )

    assert(
      influxDB.getRecent("test_measurement_1_test_value_field", List("test_partition_1", "test_partition_2"), "test_calculated_field_2", 5)
        ==
        List(
          Map(
            "time" -> "2017-12-04T12:03:05Z",
            "test_partition_1" -> "test_partition_1_name",
            "test_partition_2" -> "test_partition_2_name",
            "test_calculated_field_2" -> 5
          ),
          Map(
            "time" -> "2017-12-04T12:03:04Z",
            "test_partition_1" -> "test_partition_1_name",
            "test_partition_2" -> "test_partition_2_name",
            "test_calculated_field_2" -> 4
          ),
          Map(
            "time" -> "2017-12-04T12:03:03Z",
            "test_partition_1" -> "test_partition_1_name",
            "test_partition_2" -> "test_partition_2_name",
            "test_calculated_field_2" -> 3
          ),
          Map(
            "time" -> "2017-12-04T12:03:02Z",
            "test_partition_1" -> "test_partition_1_name",
            "test_partition_2" -> "test_partition_2_name",
            "test_calculated_field_2" -> 2
          ),
          Map(
            "time" -> "2017-12-04T12:03:01Z",
            "test_partition_1" -> "test_partition_1_name",
            "test_partition_2" -> "test_partition_2_name",
            "test_calculated_field_2" -> 1
          )
        )
    )
  }
}
