package clover.algorithms

import clover.Measurement
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

class LinearRegressionAlgorithm(sparkSession: SparkSession) extends Algorithm {

  import sparkSession.implicits._

  def transformedDatabaseName(): String = {
    "linear_regression_transformed"
  }

  def evaluatedDatabaseName(): String = {
    "linear_regression_evaluated"
  }

  def modelLocation(): String = {
    "/home/truppert/projects/master-project/clover/data/models/linear-regression/"
  }

  def colsToVector(measurement: Measurement, df: DataFrame): DataFrame = {
    val inputCols = df.columns.filter(x => {
      !(measurement.partitions ++ Array("time", measurement.valueField)).contains(x)
    })
    val assembler = new VectorAssembler()
      .setInputCols(inputCols)
      .setOutputCol("features")

    val measurementColumns = df.columns.filter(x => {
      (List("time", measurement.valueField) ++ measurement.partitions).contains(x)
    })
    val selectColumns = (measurementColumns ++ List("features")).map(x => {
      col(x)
    })


    assembler.transform(df)
      .select(selectColumns: _*)
  }

  def train(measurement: Measurement, df: DataFrame): LinearRegressionModel = {
    val filtered = df.filter($"last1STD" < 500)
    val vector = colsToVector(measurement, filtered)

    val dfSplit = vector.randomSplit(Array(.8, .2))

    val lr = new LinearRegression()
      .setMaxIter(100)
      .setRegParam(1)
      .setElasticNetParam(0.8)
      .setLabelCol(measurement.valueField)

    val model = lr.fit(dfSplit(0))

    val evaluations = model.evaluate(dfSplit(1))

    println("r2: " + evaluations.r2)
    println("meanAbsoluteError: " + evaluations.meanAbsoluteError)

    model
  }

  def loadModel(measurement: Measurement): LinearRegressionModel = {
    LinearRegressionModel.load(modelLocation() + measurement.name.replaceAll("\\.", "_") + "-" + measurement.valueField)
  }

  def evaluate(measurement: Measurement, model: LinearRegressionModel, df: DataFrame): DataFrame = {
    val vector = colsToVector(measurement, df)

    val evaluation = model.evaluate(vector)

    buildEvaluatedDF(measurement, evaluation.predictions, evaluation.meanAbsoluteError)
  }

  def buildEvaluatedDF(measurement: Measurement, predictions: DataFrame, meanAbsoluteError: Double): DataFrame = {
    val columns = List("time", measurement.valueField, "prediction", "meanAbsoluteError") ++ measurement.partitions
    val schema = columns.map(x => {
      x match {
        case "prediction" => StructField(x, DoubleType)
        case "meanAbsoluteError" => StructField(x, DoubleType)
        case measurement.valueField => StructField(x, DoubleType)
        case _ => StructField(x, StringType)
      }
    })

    val rows = predictions.collect.map(row => {
      val valuesMap = row.getValuesMap(List("time", measurement.valueField, "prediction") ++ measurement.partitions)

      val partitionValues = measurement.partitions.map(partition => {
        valuesMap(partition).asInstanceOf[String]
      })

      val mergedList = List(
        valuesMap("time").asInstanceOf[String],
        valuesMap(measurement.valueField),
        valuesMap("prediction"),
        meanAbsoluteError
      ) ++ partitionValues

      Row(mergedList: _*)
    })

    sparkSession.createDataFrame(sparkSession.sparkContext.parallelize(rows), StructType(schema))
  }

}
