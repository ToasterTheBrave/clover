package clover.transformers

import clover.Metric
import org.apache.spark.sql.SparkSession
import org.scalatest.FunSuite

class LinearRegressionTransformerTest extends FunSuite {

  val sparkSession: SparkSession = SparkSession
    .builder()
    .master("local")
    .appName("Clover")
    .getOrCreate()

  val linearRegressionTransformer = new LinearRegressionTransformer(sparkSession)

  import sparkSession.implicits._

  test("tableName") {
    assert(linearRegressionTransformer.tableName == "linear_regression_transformed")
  }

  test("transform") {
    val df = List(
      Metric(1515208040000L, 350.0),
      Metric(1515208041000L, 360.0),
      Metric(1515208042000L, 350.0),
      Metric(1515208043000L, 340.0),
      Metric(1515208044000L, 354.0),
      Metric(1515208045000L, 356.0),
      Metric(1515208046000L, 361.0),
      Metric(1515208047000L, 335.0),
      Metric(1515208048000L, 353.0),
      Metric(1515208049000L, 348.0), // "2018-01-06T03:07:29Z"
      Metric(1515208050000L, 344.0),
      Metric(1515208051000L, 347.0),
      Metric(1515208052000L, 335.0)
    ).toDF

    val transformedDF = linearRegressionTransformer.transform(df, "2018-01-06T03:07:29Z")
    assert(transformedDF.columns.mkString(",") == "time,value,last1000STD,last1000Mean,last100STD,last100Mean,last10STD,last10Mean,last9STD,last9Mean,last8STD,last8Mean,last7STD,last7Mean,last6STD,last6Mean,last5STD,last5Mean,last4STD,last4Mean,last3STD,last3Mean,last2STD,last2Mean,last1STD,last1Mean")
    assert(transformedDF.count == 4)
  }

}
