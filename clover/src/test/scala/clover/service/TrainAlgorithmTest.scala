package clover.service

import clover.Measurement
import clover.datastores.InfluxDBStore
import org.apache.spark.sql.SparkSession
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

class TrainAlgorithmTest extends FunSuite with MockitoSugar {

  val sparkSession:SparkSession = SparkSession
    .builder()
    .master("local")
    .appName("Clover")
    .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .getOrCreate()

  import sparkSession.implicits._

  val recentData = List(
    Map(
      "time" -> "2017-12-04T12:03:01Z",
      "test_partition_1" -> "test_partition_1_name",
      "test_partition_2" -> "test_partition_2_name",
      "test_value_field" -> BigDecimal(10.001),
      "test_calculated_field_1" -> 10L,
      "test_calculated_field_2" -> 1.01
    ),
    Map(
      "time" -> "2017-12-04T12:03:02Z",
      "test_partition_1" -> "test_partition_1_name",
      "test_partition_2" -> "test_partition_2_name",
      "test_value_field" -> BigDecimal(20.002),
      "test_calculated_field_1" -> 20L,
      "test_calculated_field_2" -> 2.02
    ),
    Map(
      "time" -> "2017-12-04T12:03:03Z",
      "test_partition_1" -> "test_partition_1_name",
      "test_partition_2" -> "test_partition_2_name",
      "test_value_field" -> BigDecimal(30.003),
      "test_calculated_field_1" -> 30L,
      "test_calculated_field_2" -> 3.03
    ),
    Map(
      "time" -> "2017-12-04T12:03:04Z",
      "test_partition_1" -> "test_partition_1_name",
      "test_partition_2" -> "test_partition_2_name",
      "test_value_field" -> BigDecimal(40.004),
      "test_calculated_field_1" -> 40L,
      "test_calculated_field_2" -> 4.04
    ),
    Map(
      "time" -> "2017-12-04T12:03:05Z",
      "test_partition_1" -> "test_partition_1_name",
      "test_partition_2" -> "test_partition_2_name",
      "test_value_field" -> BigDecimal(50.005),
      "test_calculated_field_1" -> 50L,
      "test_calculated_field_2" -> 5.05
    )
  )

  val measurement = Measurement("test_measurement_name", List("test_partition_1", "test_partition_2"), "test_value_field")

  test("main") {
    // Side effects only.  Nothing to test
  }

  test("run") {
    // Side effects only.  Nothing to test
  }

  test("runTraining") {
    // Side effects only.  Nothing to test
  }

  test("getRecentTransformations - gets from InfluxDB and returns a transformed df") {
    val database = mock[InfluxDBStore]

    when(database.getAllRecent("test_measurement_name_test_value_field", 5))
      .thenReturn(recentData)

    val expected = List(
      (10.001, 10, "test_partition_2_name", 1.01, "test_partition_1_name", "2017-12-04T12:03:01Z"),
      (20.002, 20, "test_partition_2_name", 2.02, "test_partition_1_name", "2017-12-04T12:03:02Z"),
      (30.003, 30, "test_partition_2_name", 3.03, "test_partition_1_name", "2017-12-04T12:03:03Z"),
      (40.004, 40, "test_partition_2_name", 4.04, "test_partition_1_name", "2017-12-04T12:03:04Z"),
      (50.005, 50, "test_partition_2_name", 5.05, "test_partition_1_name", "2017-12-04T12:03:05Z")
    ).toDF("test_value_field", "test_calculated_field_1", "test_partition_2", "test_calculated_field_2", "test_partition_1", "time")
    val actual = TrainAlgorithm.getRecentTransformedMeasurements(database, measurement, 5)

    assert(expected.columns.deep == actual.columns.deep)
    assert(expected.collect.deep == actual.collect.deep)
  }

  test("convertTransformedMeasurementsToDF - returns expected DF for database map passed in") {
    val expected = List(
      (10.001, 10, "test_partition_2_name", 1.01, "test_partition_1_name", "2017-12-04T12:03:01Z"),
      (20.002, 20, "test_partition_2_name", 2.02, "test_partition_1_name", "2017-12-04T12:03:02Z"),
      (30.003, 30, "test_partition_2_name", 3.03, "test_partition_1_name", "2017-12-04T12:03:03Z"),
      (40.004, 40, "test_partition_2_name", 4.04, "test_partition_1_name", "2017-12-04T12:03:04Z"),
      (50.005, 50, "test_partition_2_name", 5.05, "test_partition_1_name", "2017-12-04T12:03:05Z")
    ).toDF("test_value_field", "test_calculated_field_1", "test_partition_2", "test_calculated_field_2", "test_partition_1", "time")
    val actual = TrainAlgorithm.convertTransformedMeasurementsToDF(measurement, recentData)

    assert(expected.columns.deep == actual.columns.deep)
    assert(expected.collect.deep == actual.collect.deep)
  }
}
