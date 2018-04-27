package clover.service

import clover.{Measurement, MetricSource, MetricSource}
import clover.datastores.InfluxDBStore
import org.apache.spark.sql.SparkSession
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

class BuildReportTest extends FunSuite with MockitoSugar {

  val sparkSession:SparkSession = SparkSession
    .builder()
    .master("local")
    .appName("Clover")
    .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .getOrCreate()

  import sparkSession.implicits._

  test("main") {
    // Side effects only.  Nothing to test
  }

  test("run") {
    // Side effects only.  Nothing to test
  }

  test("getAllMeasurements - returns a flat list of measurements from all metric sources") {
    val database = mock[InfluxDBStore]
    val metricSources = List(
      MetricSource("some-host", 1000, "some-database", List(
        Measurement("test_measurement_1", List("partition_1", "partition_2"), "value_field_1"),
        Measurement("test_measurement_1", List("partition_1", "partition_2"), "value_field_2"),
        Measurement("test_measurement_2", List("partition_3", "partition_4"), "value_field_1")
      )),
      MetricSource("some-host", 1000, "some-database", List(
        Measurement("test_measurement_3", List("partition_1", "partition_2"), "value_field_1"),
        Measurement("test_measurement_3", List("partition_1", "partition_2"), "value_field_2"),
        Measurement("test_measurement_4", List("partition_3", "partition_4"), "value_field_1")
      ))
    )
    assert(
      BuildReport.getAllMeasurements(metricSources)
      ==
      List(
        Measurement("test_measurement_1", List("partition_1", "partition_2"), "value_field_1"),
        Measurement("test_measurement_1", List("partition_1", "partition_2"), "value_field_2"),
        Measurement("test_measurement_2", List("partition_3", "partition_4"), "value_field_1"),
        Measurement("test_measurement_3", List("partition_1", "partition_2"), "value_field_1"),
        Measurement("test_measurement_3", List("partition_1", "partition_2"), "value_field_2"),
        Measurement("test_measurement_4", List("partition_3", "partition_4"), "value_field_1")
      )
    )
  }

  test("getAllMetricsLastHourDF - returns all metrics for the last hour for every measurement") {
    val database = mock[InfluxDBStore]
    val measurements = List(
      Measurement("test_measurement_1", List("partition_1", "partition_2"), "value_field_1"),
      Measurement("test_measurement_1", List("partition_1", "partition_2"), "value_field_2"),
      Measurement("test_measurement_2", List("partition_3", "partition_4"), "value_field_1")
    )
    val startTime = "2018-04-03T17:55:00Z"
    val endTime = "2018-04-03T18:55:00Z"

    val reportStartTime = "2018-04-03T18:10:00Z"
    val reportEndTime = "2018-04-03T19:10:00Z"

    when(database.getAllBetween("test_measurement_1_value_field_1", reportStartTime, reportEndTime))
      .thenReturn(List(
        Map(
          "time" -> "2018-04-03T17:55:00Z",
          "partition_1" -> "partition_1_value_1",
          "partition_2" -> "partition_2_value_1",
          "prediction" -> BigDecimal(100.001),
          "value_field_1" -> BigDecimal(100.100),
          "error" -> BigDecimal(0.01),
          "meanAbsoluteError" -> BigDecimal(0.1)
        ),
        Map(
          "time" -> "2018-04-03T17:56:00Z",
          "partition_1" -> "partition_1_value_2",
          "partition_2" -> "partition_2_value_2",
          "prediction" -> BigDecimal(200.002),
          "value_field_1" -> BigDecimal(200.200),
          "error" -> BigDecimal(0.02),
          "meanAbsoluteError" -> BigDecimal(0.2)
        ),
        Map(
          "time" -> "2018-04-03T17:57:00Z",
          "partition_1" -> "partition_1_value_3",
          "partition_2" -> "partition_2_value_3",
          "prediction" -> BigDecimal(300.003),
          "value_field_1" -> BigDecimal(300.300),
          "error" -> BigDecimal(0.03),
          "meanAbsoluteError" -> BigDecimal(0.3)
        )
      ))

    when(database.getAllBetween("test_measurement_1_value_field_2", reportStartTime, reportEndTime))
      .thenReturn(List(
        Map(
          "time" -> "2018-04-03T17:55:00Z",
          "partition_1" -> "partition_1_value_1",
          "partition_2" -> "partition_2_value_1",
          "prediction" -> BigDecimal(100.001),
          "value_field_2" -> BigDecimal(100.100),
          "error" -> BigDecimal(0.01),
          "meanAbsoluteError" -> BigDecimal(0.1)
        ),
        Map(
          "time" -> "2018-04-03T17:56:00Z",
          "partition_1" -> "partition_1_value_2",
          "partition_2" -> "partition_2_value_2",
          "prediction" -> BigDecimal(200.002),
          "value_field_2" -> BigDecimal(200.200),
          "error" -> BigDecimal(0.02),
          "meanAbsoluteError" -> BigDecimal(0.2)
        ),
        Map(
          "time" -> "2018-04-03T17:57:00Z",
          "partition_1" -> "partition_1_value_3",
          "partition_2" -> "partition_2_value_3",
          "prediction" -> BigDecimal(300.003),
          "value_field_2" -> BigDecimal(300.300),
          "error" -> BigDecimal(0.03),
          "meanAbsoluteError" -> BigDecimal(0.3)
        )
      ))

    when(database.getAllBetween("test_measurement_2_value_field_1", reportStartTime, reportEndTime))
      .thenReturn(List(
        Map(
          "time" -> "2018-04-03T17:55:00Z",
          "partition_3" -> "partition_3_value_1",
          "partition_4" -> "partition_4_value_1",
          "prediction" -> BigDecimal(100.001),
          "value_field_1" -> BigDecimal(100.100),
          "error" -> BigDecimal(0.01),
          "meanAbsoluteError" -> BigDecimal(0.1)
        ),
        Map(
          "time" -> "2018-04-03T17:56:00Z",
          "partition_3" -> "partition_3_value_2",
          "partition_4" -> "partition_4_value_2",
          "prediction" -> BigDecimal(200.002),
          "value_field_1" -> BigDecimal(200.200),
          "error" -> BigDecimal(0.02),
          "meanAbsoluteError" -> BigDecimal(0.2)
        ),
        Map(
          "time" -> "2018-04-03T17:57:00Z",
          "partition_3" -> "partition_3_value_3",
          "partition_4" -> "partition_4_value_3",
          "prediction" -> BigDecimal(300.003),
          "value_field_1" -> BigDecimal(300.300),
          "error" -> BigDecimal(0.03),
          "meanAbsoluteError" -> BigDecimal(0.3)
        )
      ))

    val expected = List(
      ("2018-04-03T17:55:00Z", "test_measurement_1", 100.1, "value_field_1", "partition_1: partition_1_value_1, partition_2: partition_2_value_1", 0.01, 100.001, 0.1),
      ("2018-04-03T17:56:00Z", "test_measurement_1", 200.2, "value_field_1", "partition_1: partition_1_value_2, partition_2: partition_2_value_2", 0.02, 200.002, 0.2),
      ("2018-04-03T17:57:00Z", "test_measurement_1", 300.3, "value_field_1", "partition_1: partition_1_value_3, partition_2: partition_2_value_3", 0.03, 300.003, 0.3),
      ("2018-04-03T17:55:00Z", "test_measurement_1", 100.1, "value_field_2", "partition_1: partition_1_value_1, partition_2: partition_2_value_1", 0.01, 100.001, 0.1),
      ("2018-04-03T17:56:00Z", "test_measurement_1", 200.2, "value_field_2", "partition_1: partition_1_value_2, partition_2: partition_2_value_2", 0.02, 200.002, 0.2),
      ("2018-04-03T17:57:00Z", "test_measurement_1", 300.3, "value_field_2", "partition_1: partition_1_value_3, partition_2: partition_2_value_3", 0.03, 300.003, 0.3),
      ("2018-04-03T17:55:00Z", "test_measurement_2", 100.1, "value_field_1", "partition_3: partition_3_value_1, partition_4: partition_4_value_1", 0.01, 100.001, 0.1),
      ("2018-04-03T17:56:00Z", "test_measurement_2", 200.2, "value_field_1", "partition_3: partition_3_value_2, partition_4: partition_4_value_2", 0.02, 200.002, 0.2),
      ("2018-04-03T17:57:00Z", "test_measurement_2", 300.3, "value_field_1", "partition_3: partition_3_value_3, partition_4: partition_4_value_3", 0.03, 300.003, 0.3)
    ).toDF("time", "measurement", "value", "valueField", "tags", "error", "prediction", "meanAbsoluteError")
    
    val actual = BuildReport.getAllMetricsLastHourDF(database, measurements, endTime)

    assert(actual.columns.deep == expected.columns.deep)
    assert(actual.collect.deep == expected.collect.deep)
  }

  test("getMostAnomalous - returns the X most anomalous metrics per timestamp") {
    val lastHourDF = List(
      ("2018-04-03T17:55:00Z", "test_measurement_1", 100.1, "value_field_1", "partition_1: partition_1_value_1, partition_2: partition_2_value_1", 0.01, 100.001, 0.1),
      ("2018-04-03T17:56:00Z", "test_measurement_1", 200.2, "value_field_1", "partition_1: partition_1_value_2, partition_2: partition_2_value_2", 0.02, 200.002, 0.2),
      ("2018-04-03T17:57:00Z", "test_measurement_1", 300.3, "value_field_1", "partition_1: partition_1_value_3, partition_2: partition_2_value_3", 0.03, 300.003, 0.3),
      ("2018-04-03T17:55:00Z", "test_measurement_1", 100.1, "value_field_2", "partition_1: partition_1_value_1, partition_2: partition_2_value_1", 0.04, 100.001, 0.1),
      ("2018-04-03T17:56:00Z", "test_measurement_1", 200.2, "value_field_2", "partition_1: partition_1_value_2, partition_2: partition_2_value_2", 0.05, 200.002, 0.2),
      ("2018-04-03T17:57:00Z", "test_measurement_1", 300.3, "value_field_2", "partition_1: partition_1_value_3, partition_2: partition_2_value_3", 0.06, 300.003, 0.3),
      ("2018-04-03T17:55:00Z", "test_measurement_2", 100.1, "value_field_1", "partition_3: partition_3_value_1, partition_4: partition_4_value_1", 0.07, 100.001, 0.1),
      ("2018-04-03T17:56:00Z", "test_measurement_2", 200.2, "value_field_1", "partition_3: partition_3_value_2, partition_4: partition_4_value_2", 0.08, 200.002, 0.2),
      ("2018-04-03T17:57:00Z", "test_measurement_2", 300.3, "value_field_1", "partition_3: partition_3_value_3, partition_4: partition_4_value_3", 0.09, 300.003, 0.3)
    ).toDF("time", "measurement", "value", "valueField", "tags", "error", "prediction", "meanAbsoluteError")

    val expectedOne = List(
      ("2018-04-03T17:55:00Z", "test_measurement_2", 100.1, "value_field_1", "partition_3: partition_3_value_1, partition_4: partition_4_value_1", 0.07, 100.001, 0.1),
      ("2018-04-03T17:57:00Z", "test_measurement_2", 300.3, "value_field_1", "partition_3: partition_3_value_3, partition_4: partition_4_value_3", 0.09, 300.003, 0.3),
      ("2018-04-03T17:56:00Z", "test_measurement_2", 200.2, "value_field_1", "partition_3: partition_3_value_2, partition_4: partition_4_value_2", 0.08, 200.002, 0.2)
    ).toDF("time", "measurement", "value", "valueField", "tags", "error", "prediction", "meanAbsoluteError")

    val actualOne = BuildReport.getMostAnomalous(lastHourDF, 1)

    assert(actualOne.columns.deep == expectedOne.columns.deep)
    assert(actualOne.collect.deep == expectedOne.collect.deep)

    val expectedTwo = List(
      ("2018-04-03T17:55:00Z", "test_measurement_2", 100.1, "value_field_1", "partition_3: partition_3_value_1, partition_4: partition_4_value_1", 0.07, 100.001, 0.1),
      ("2018-04-03T17:55:00Z", "test_measurement_1", 100.1, "value_field_2", "partition_1: partition_1_value_1, partition_2: partition_2_value_1", 0.04, 100.001, 0.1),
      ("2018-04-03T17:57:00Z", "test_measurement_2", 300.3, "value_field_1", "partition_3: partition_3_value_3, partition_4: partition_4_value_3", 0.09, 300.003, 0.3),
      ("2018-04-03T17:57:00Z", "test_measurement_1", 300.3, "value_field_2", "partition_1: partition_1_value_3, partition_2: partition_2_value_3", 0.06, 300.003, 0.3),
      ("2018-04-03T17:56:00Z", "test_measurement_2", 200.2, "value_field_1", "partition_3: partition_3_value_2, partition_4: partition_4_value_2", 0.08, 200.002, 0.2),
      ("2018-04-03T17:56:00Z", "test_measurement_1", 200.2, "value_field_2", "partition_1: partition_1_value_2, partition_2: partition_2_value_2", 0.05, 200.002, 0.2)
    ).toDF("time", "measurement", "value", "valueField", "tags", "error", "prediction", "meanAbsoluteError")

    val actualTwo = BuildReport.getMostAnomalous(lastHourDF, 2)

    assert(actualTwo.columns.deep == expectedTwo.columns.deep)
    assert(actualTwo.collect.deep == expectedTwo.collect.deep)
  }

  test("buildReportData - Return map of data ready to write to file") {
    val mostAnomalousDF = List(
      ("2018-04-03T17:55:00Z", "test_measurement_2", 100.1, "value_field_1", "partition_3: partition_3_value_1, partition_4: partition_4_value_1", 0.07, 100.001, 0.1),
      ("2018-04-03T17:57:00Z", "test_measurement_2", 300.3, "value_field_1", "partition_3: partition_3_value_3, partition_4: partition_4_value_3", 0.09, 300.003, 0.3),
      ("2018-04-03T17:56:00Z", "test_measurement_2", 200.2, "value_field_1", "partition_3: partition_3_value_2, partition_4: partition_4_value_2", 0.08, 200.002, 0.2)
    ).toDF("time", "measurement", "value", "valueField", "tags", "error", "prediction", "meanAbsoluteError")

    val expected = Map(
      "columns" -> Array(
        "'test_measurement_2 - value_field_1 - partition_3: partition_3_value_1, partition_4: partition_4_value_1'",
        "'test_measurement_2 - value_field_1 - partition_3: partition_3_value_3, partition_4: partition_4_value_3'",
        "'test_measurement_2 - value_field_1 - partition_3: partition_3_value_2, partition_4: partition_4_value_2'"
    ),
      "rows" -> Array(
        "[new Date('2018-04-03T17:57:00Z'), null, ``, 0.09, `2018-04-03T17:57:00Z\ntest_measurement_2 - value_field_1\npartition_3: partition_3_value_3, partition_4: partition_4_value_3\nError: 0.09\nValue: 300.3\nExpected: 300.003 +/- 0.3`, null, ``]",
        "[new Date('2018-04-03T17:55:00Z'), 0.07, `2018-04-03T17:55:00Z\ntest_measurement_2 - value_field_1\npartition_3: partition_3_value_1, partition_4: partition_4_value_1\nError: 0.07\nValue: 100.1\nExpected: 100.001 +/- 0.1`, null, ``, null, ``]",
        "[new Date('2018-04-03T17:56:00Z'), null, ``, null, ``, 0.08, `2018-04-03T17:56:00Z\ntest_measurement_2 - value_field_1\npartition_3: partition_3_value_2, partition_4: partition_4_value_2\nError: 0.08\nValue: 200.2\nExpected: 200.002 +/- 0.2`]"
      )
    )

    val actual = BuildReport.buildReportData(mostAnomalousDF)

    assert(actual("columns") sameElements expected("columns"))
    assert(actual("rows") sameElements expected("rows"))

  }

  test("writeReportData") {
    // Side effects only.  Nothing to test
  }
}
