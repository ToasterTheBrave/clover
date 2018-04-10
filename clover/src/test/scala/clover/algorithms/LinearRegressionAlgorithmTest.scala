package clover.algorithms

import clover.Measurement
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col
import org.scalatest.FunSuite
import org.apache.spark.sql.functions._

class LinearRegressionAlgorithmTest extends FunSuite {
  val sparkSession: SparkSession = SparkSession
    .builder()
    .master("local")
    .appName("Clover")
    .getOrCreate()

  val linearRegressionAlgorithm = new LinearRegressionAlgorithm(sparkSession)

  import sparkSession.implicits._

  val measurement = Measurement("test_measurement_name", List("test_partition_1", "test_partition_2"), "test_value_field")

  test("transformedDatabaseName") {
    assert(linearRegressionAlgorithm.transformedDatabaseName == "linear_regression_transformed")
  }

  test("evaluatedDatabaseName") {
    assert(linearRegressionAlgorithm.evaluatedDatabaseName == "linear_regression_evaluated")
  }

  test("colsToVector - returns vector from df with features.  Time and partition keys still intact, but not in features") {

    val df = List(
      (10.001, 10, "test_partition_2_name", 1.01, "test_partition_1_name", "2017-12-04T12:03:01Z"),
      (20.002, 20, "test_partition_2_name", 2.02, "test_partition_1_name", "2017-12-04T12:03:02Z"),
      (30.003, 30, "test_partition_2_name", 3.03, "test_partition_1_name", "2017-12-04T12:03:03Z"),
      (40.004, 40, "test_partition_2_name", 4.04, "test_partition_1_name", "2017-12-04T12:03:04Z"),
      (50.005, 50, "test_partition_2_name", 5.05, "test_partition_1_name", "2017-12-04T12:03:05Z")
    ).toDF("test_value_field", "test_calculated_field_1", "test_partition_2", "test_calculated_field_2", "test_partition_1", "time")

    val measurement = Measurement("test_measurement_name", List("test_partition_1", "test_partition_2"), "test_value_field")

    val assembler = new VectorAssembler()
      .setInputCols(Array("test_calculated_field_1", "test_calculated_field_2"))
      .setOutputCol("features")

    val expected = assembler.transform(df)
      .select($"test_value_field", $"test_partition_2", $"test_partition_1", $"time", $"features")

    val actual = linearRegressionAlgorithm.colsToVector(measurement, df)

    assert(expected.columns.deep == actual.columns.deep)
    assert(expected.collect.deep == actual.collect.deep)
  }

  test("train") {
    // Side effects only.  Nothing to test
  }

  test("loadModel") {
    // Side effects only.  Nothing to test
  }

  test("evaluate") {
    // Side effects only.  Nothing to test
  }

  test("buildEvaluatedDF - merges together all evaluated info into a DataFrame") {
    val df = List(
      (10.001, 10, "test_partition_2_name", 1.01, "test_partition_1_name", "2017-12-04T12:03:01Z", Vector(10, 1.01), 10.011),
      (20.002, 20, "test_partition_2_name", 2.02, "test_partition_1_name", "2017-12-04T12:03:02Z", Vector(20, 2.02), 20.012),
      (30.003, 30, "test_partition_2_name", 3.03, "test_partition_1_name", "2017-12-04T12:03:03Z", Vector(30, 3.03), 30.013),
      (40.004, 40, "test_partition_2_name", 4.04, "test_partition_1_name", "2017-12-04T12:03:04Z", Vector(40, 4.04), 40.014),
      (50.005, 50, "test_partition_2_name", 5.05, "test_partition_1_name", "2017-12-04T12:03:05Z", Vector(50, 5.05), 50.015)
    ).toDF("test_value_field", "test_calculated_field_1", "test_partition_2", "test_calculated_field_2", "test_partition_1", "time", "features", "prediction")

    val expected = List(
      ("2017-12-04T12:03:01Z", 10.001, 10.011, 0.02, "test_partition_1_name", "test_partition_2_name"),
      ("2017-12-04T12:03:02Z", 20.002, 20.012, 0.02, "test_partition_1_name", "test_partition_2_name"),
      ("2017-12-04T12:03:03Z", 30.003, 30.013, 0.02, "test_partition_1_name", "test_partition_2_name"),
      ("2017-12-04T12:03:04Z", 40.004, 40.014, 0.02, "test_partition_1_name", "test_partition_2_name"),
      ("2017-12-04T12:03:05Z", 50.005, 50.015, 0.02, "test_partition_1_name", "test_partition_2_name")
    ).toDF("time", "test_value_field", "prediction", "meanAbsoluteError", "test_partition_1", "test_partition_2")

    val actual = linearRegressionAlgorithm.buildEvaluatedDF(measurement, df, 0.02)

    assert(expected.columns.deep == actual.columns.deep)
    assert(expected.collect.deep == actual.collect.deep)
  }
}
