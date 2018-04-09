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

  test("databaseName") {
    assert(linearRegressionAlgorithm.databaseName == "linear_regression_transformed")
  }

  test("colsToVector - returns vector from df with time and partition keys removed") {

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
      .select(col("test_value_field") as "label", $"features")

    val actual = linearRegressionAlgorithm.colsToVector(measurement, df)

    assert(expected.columns.deep == actual.columns.deep)
    assert(expected.collect.deep == actual.collect.deep)
  }

  test("train") {
    // Side effects only.  Nothing to test
  }
}
