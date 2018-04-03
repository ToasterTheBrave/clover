package clover.transformers

import clover.{Measurement, Util}
import org.apache.spark.sql.SparkSession
import org.scalatest.FunSuite
import org.apache.spark.sql.functions.col

class LinearRegressionTransformerTest extends FunSuite {

  val sparkSession: SparkSession = SparkSession
    .builder()
    .master("local")
    .appName("Clover")
    .getOrCreate()

  val linearRegressionTransformer = new LinearRegressionTransformer(sparkSession)

  import sparkSession.implicits._

  test("databaseName") {
    assert(linearRegressionTransformer.databaseName == "linear_regression_transformed")
  }

  test("transform - adds expected columns, grouping by the measurement partitions") {
    val df = List(
      (Util.timeStringToLong("2017-12-04T12:03:01Z"), "test_partition_1_name", "test_partition_2_name", 10001L),
      (Util.timeStringToLong("2017-12-04T12:03:01Z"), "test_partition_1_name2", "test_partition_2_name", 10001L),
      (Util.timeStringToLong("2017-12-04T12:03:02Z"), "test_partition_1_name", "test_partition_2_name", 20002L),
      (Util.timeStringToLong("2017-12-04T12:03:02Z"), "test_partition_1_name2", "test_partition_2_name", 20002L),
      (Util.timeStringToLong("2017-12-04T12:03:03Z"), "test_partition_1_name", "test_partition_2_name", 30003L),
      (Util.timeStringToLong("2017-12-04T12:03:03Z"), "test_partition_1_name2", "test_partition_2_name", 30003L),
      (Util.timeStringToLong("2017-12-04T12:03:04Z"), "test_partition_1_name", "test_partition_2_name", 40004L),
      (Util.timeStringToLong("2017-12-04T12:03:04Z"), "test_partition_1_name2", "test_partition_2_name", 40004L),
      (Util.timeStringToLong("2017-12-04T12:03:05Z"), "test_partition_1_name", "test_partition_2_name", 50005L),
      (Util.timeStringToLong("2017-12-04T12:03:05Z"), "test_partition_1_name2", "test_partition_2_name", 50005L)
    ).toDF("time", "test_partition_1", "test_partition_2", "test_value_field")

    val measurement = Measurement("test_measurement_name", List("test_partition_1", "test_partition_2"), "test_value_field")

    val transformedDF = linearRegressionTransformer.transform(df, measurement, "2017-12-04T12:03:02Z")
    
    val equalColumns = List(
      "time",
      measurement.valueField,
      "last1000STD",
      "last1000Mean",
      "last100STD",
      "last100Mean",
      "last10STD",
      "last10Mean",
      "last9STD",
      "last9Mean",
      "last8STD",
      "last8Mean",
      "last7STD",
      "last7Mean",
      "last6STD",
      "last6Mean",
      "last5STD",
      "last5Mean",
      "last4STD",
      "last4Mean",
      "last3STD",
      "last3Mean",
      "last2STD",
      "last2Mean",
      "last1STD",
      "last1Mean"
    )

    val allColumns = equalColumns ++ measurement.partitions
    val firstPartition = transformedDF.select(equalColumns.map(col):_*).where("test_partition_1 = 'test_partition_1_name' and test_partition_2 = 'test_partition_2_name'")
    val secondPartition = transformedDF.select(equalColumns.map(col):_*).where("test_partition_1 = 'test_partition_1_name2' and test_partition_2 = 'test_partition_2_name'")

    assert(firstPartition.collect.deep == secondPartition.collect.deep)

    assert(transformedDF.columns.toList.sorted == allColumns.sorted)
    assert(transformedDF.count == 8)
  }

}
