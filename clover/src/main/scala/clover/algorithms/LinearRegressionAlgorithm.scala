package clover.algorithms

import clover.Measurement
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel}
import org.apache.spark.sql.functions._

class LinearRegressionAlgorithm(sparkSession: SparkSession) extends Algorithm {

  import sparkSession.implicits._

  def databaseName(): String = {
    "linear_regression_transformed"
  }

  def modelLocation(): String = {
    "/home/truppert/projects/master-project/clover/data/models/linear-regression"
  }

  def colsToVector(measurement: Measurement, df: DataFrame): DataFrame = {
    val inputCols = df.columns.filter(x => {
      !(measurement.partitions ++ Array("time", measurement.valueField)).contains(x)
    })
    val assembler = new VectorAssembler()
      .setInputCols(inputCols)
      .setOutputCol("features")

    assembler.transform(df)
      .select(col(measurement.valueField) as "label", $"features")
  }

  def train(measurement: Measurement, df: DataFrame): LinearRegressionModel = {
    val filtered = df.filter($"last1STD" < 500)
    val vector = colsToVector(measurement, filtered)

    val dfSplit = vector.randomSplit(Array(.8, .2))

    val lr = new LinearRegression()
      .setMaxIter(100)
      .setRegParam(1)
      .setElasticNetParam(0.8)

    val model = lr.fit(dfSplit(0))

    val evaluations = model.evaluate(dfSplit(1))
    println("test predictions:")
    evaluations.predictions.show(5)
    println

    println("test residuals")
    evaluations.residuals.show(5)

    println("r2: " + evaluations.r2)
    println("rootMeanSquaredError: " + evaluations.rootMeanSquaredError)
    println("meanAbsoluteError: " + evaluations.meanAbsoluteError)
    println("meanSquaredError: " + evaluations.meanSquaredError)

    model
  }
}
