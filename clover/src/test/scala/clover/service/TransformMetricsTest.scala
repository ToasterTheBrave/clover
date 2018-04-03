package clover.service

import clover.{Measurement, Util}
import clover.datastores.InfluxDBStore
import org.apache.spark.sql.SparkSession
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

class TransformMetricsTest extends FunSuite with MockitoSugar {

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
      "test_value_field" -> BigDecimal(10.001)
    ),
    Map(
      "time" -> "2017-12-04T12:03:02Z",
      "test_partition_1" -> "test_partition_1_name",
      "test_partition_2" -> "test_partition_2_name",
      "test_value_field" -> BigDecimal(20.002)
    ),
    Map(
      "time" -> "2017-12-04T12:03:03Z",
      "test_partition_1" -> "test_partition_1_name",
      "test_partition_2" -> "test_partition_2_name",
      "test_value_field" -> BigDecimal(30.003)
    ),
    Map(
      "time" -> "2017-12-04T12:03:04Z",
      "test_partition_1" -> "test_partition_1_name",
      "test_partition_2" -> "test_partition_2_name",
      "test_value_field" -> BigDecimal(40.004)
    ),
    Map(
      "time" -> "2017-12-04T12:03:05Z",
      "test_partition_1" -> "test_partition_1_name",
      "test_partition_2" -> "test_partition_2_name",
      "test_value_field" -> BigDecimal(50.005)
    )
  )

  val sinceData = List(
    Map(
      "time" -> "2017-12-04T12:03:06Z",
      "test_partition_1" -> "test_partition_1_name",
      "test_partition_2" -> "test_partition_2_name",
      "test_value_field" -> BigDecimal(60.006)
    ),
    Map(
      "time" -> "2017-12-04T12:03:07Z",
      "test_partition_1" -> "test_partition_1_name",
      "test_partition_2" -> "test_partition_2_name",
      "test_value_field" -> BigDecimal(70.007)
    ),
    Map(
      "time" -> "2017-12-04T12:03:08Z",
      "test_partition_1" -> "test_partition_1_name",
      "test_partition_2" -> "test_partition_2_name",
      "test_value_field" -> BigDecimal(80.008)
    ),
    Map(
      "time" -> "2017-12-04T12:03:09Z",
      "test_partition_1" -> "test_partition_1_name",
      "test_partition_2" -> "test_partition_2_name",
      "test_value_field" -> BigDecimal(90.009)
    )
  )

  test("main") {
    // Side effects only.  Nothing to test
  }

  test("run") {
    // Side effects only.  Nothing to test
  }

  test("runTransformers") {
    // Side effects only.  Nothing to test
  }

  test("convertMeasurementsToDF - returns expected data frame") {
    val measurement = Measurement("test_measurement_name", List("test_partition_1", "test_partition_2"), "test_value_field")

    val expected = List(
      (Util.timeStringToLong("2017-12-04T12:03:01Z"), "test_partition_1_name", "test_partition_2_name", 10.001),
      (Util.timeStringToLong("2017-12-04T12:03:02Z"), "test_partition_1_name", "test_partition_2_name", 20.002),
      (Util.timeStringToLong("2017-12-04T12:03:03Z"), "test_partition_1_name", "test_partition_2_name", 30.003),
      (Util.timeStringToLong("2017-12-04T12:03:04Z"), "test_partition_1_name", "test_partition_2_name", 40.004),
      (Util.timeStringToLong("2017-12-04T12:03:05Z"), "test_partition_1_name", "test_partition_2_name", 50.005)
    ).toDF("time", "test_partition_1", "test_partition_2", "test_value_field")

    val actual = TransformMetrics.convertMeasurementsToDF(measurement, recentData)

    assert(expected.columns.deep == actual.columns.deep)
    assert(expected.collect.deep == actual.collect.deep)
  }

  test("getInitialMeasurements - returns expected data frame") {
    val database = mock[InfluxDBStore]

    when(database.getRecent("test_measurement_name", List("test_partition_1", "test_partition_2"), "test_value_field", 5))
      .thenReturn(recentData)

    val expected = List(
      (Util.timeStringToLong("2017-12-04T12:03:01Z"), "test_partition_1_name", "test_partition_2_name", 10.001),
      (Util.timeStringToLong("2017-12-04T12:03:02Z"), "test_partition_1_name", "test_partition_2_name", 20.002),
      (Util.timeStringToLong("2017-12-04T12:03:03Z"), "test_partition_1_name", "test_partition_2_name", 30.003),
      (Util.timeStringToLong("2017-12-04T12:03:04Z"), "test_partition_1_name", "test_partition_2_name", 40.004),
      (Util.timeStringToLong("2017-12-04T12:03:05Z"), "test_partition_1_name", "test_partition_2_name", 50.005)
    ).toDF("time", "test_partition_1", "test_partition_2", "test_value_field")

    val measurement = Measurement("test_measurement_name", List("test_partition_1", "test_partition_2"), "test_value_field")
    val actual = TransformMetrics.getInitialMeasurements(database, measurement, 5)

    assert(expected.columns.deep == actual.columns.deep)
    assert(expected.collect.deep == actual.collect.deep)
  }

  test("reloadMeasurements - returns expected data frame") {
    val database = mock[InfluxDBStore]

    when(database.getSince("test_measurement_name", List("test_partition_1", "test_partition_2"), "test_value_field", "2017-12-04T12:03:05Z"))
      .thenReturn(sinceData)

    val expected = List(
      (Util.timeStringToLong("2017-12-04T12:03:09Z"), "test_partition_1_name", "test_partition_2_name", 90.009),
      (Util.timeStringToLong("2017-12-04T12:03:08Z"), "test_partition_1_name", "test_partition_2_name", 80.008),
      (Util.timeStringToLong("2017-12-04T12:03:07Z"), "test_partition_1_name", "test_partition_2_name", 70.007),
      (Util.timeStringToLong("2017-12-04T12:03:06Z"), "test_partition_1_name", "test_partition_2_name", 60.006),
      (Util.timeStringToLong("2017-12-04T12:03:05Z"), "test_partition_1_name", "test_partition_2_name", 50.005),
      (Util.timeStringToLong("2017-12-04T12:03:04Z"), "test_partition_1_name", "test_partition_2_name", 40.004),
      (Util.timeStringToLong("2017-12-04T12:03:03Z"), "test_partition_1_name", "test_partition_2_name", 30.003)
    ).toDF("time", "test_partition_1", "test_partition_2", "test_value_field")

    val measurement = Measurement("test_measurement_name", List("test_partition_1", "test_partition_2"), "test_value_field")
    val initialDF = List(
      (Util.timeStringToLong("2017-12-04T12:03:01Z"), "test_partition_1_name", "test_partition_2_name", 10.001),
      (Util.timeStringToLong("2017-12-04T12:03:02Z"), "test_partition_1_name", "test_partition_2_name", 20.002),
      (Util.timeStringToLong("2017-12-04T12:03:03Z"), "test_partition_1_name", "test_partition_2_name", 30.003),
      (Util.timeStringToLong("2017-12-04T12:03:04Z"), "test_partition_1_name", "test_partition_2_name", 40.004),
      (Util.timeStringToLong("2017-12-04T12:03:05Z"), "test_partition_1_name", "test_partition_2_name", 50.005)
    ).toDF("time", "test_partition_1", "test_partition_2", "test_value_field")

    val actual = TransformMetrics.reloadMeasurements(database, measurement, initialDF, 7)

    assert(expected.columns.deep == actual.columns.deep)
    assert(expected.collect.deep == actual.collect.deep)
  }

  test("writeTransformations") {
    // Side effects only.  Nothing to test
  }


}
