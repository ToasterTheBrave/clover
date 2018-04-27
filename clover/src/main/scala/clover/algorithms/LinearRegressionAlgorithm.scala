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

  def colsToVector(measurement: Measurement, df: DataFrame, labelField: String): DataFrame = {
    val inputCols = df.columns.filter(x => {
      val pattern = if(labelField == "pctDiff") {
        "^pctDiffLast.*".r
      } else {
        "^last.*".r
      }
      x match {
        case pattern() => true
        case _ => false
      }
    })

    val assembler = new VectorAssembler()
      .setInputCols(inputCols)
      .setOutputCol("features")

    val measurementColumns = df.columns.filter(x => {
      (List("time", measurement.valueField, "pctDiff") ++ measurement.partitions).contains(x)
    })
    val selectColumns = (measurementColumns ++ List("features")).map(x => {
      col(x)
    })

    assembler.transform(df)
      .select(selectColumns: _*)
  }

  def train(measurement: Measurement, df: DataFrame): LinearRegressionModel = {
    val valueVector = colsToVector(measurement, df, measurement.valueField)
    val valueDfSplit = valueVector.randomSplit(Array(.8, .2))
    val valueLinearRegression = new LinearRegression()
      .setMaxIter(100)
      .setRegParam(1)
      .setElasticNetParam(0.8)
      .setLabelCol(measurement.valueField)
    val valueModel = valueLinearRegression.fit(valueDfSplit(0))
    val valueEvaluations = valueModel.evaluate(valueDfSplit(1))

    val pctDiffVector = colsToVector(measurement, df, "pctDiff")
    val pctDiffDfSplit = pctDiffVector.randomSplit(Array(.8, .2))
    val pctDiffLinearRegression = new LinearRegression()
      .setMaxIter(100)
      .setRegParam(1)
      .setElasticNetParam(0.8)
      .setLabelCol("pctDiff")
    val pctDiffModel = pctDiffLinearRegression.fit(pctDiffDfSplit(0))
    val pctDiffEvaluations = pctDiffModel.evaluate(pctDiffDfSplit(1))

    if(pctDiffEvaluations.r2adj > valueEvaluations.r2adj) {
      println(s"choosing pctDiff model : (pct r2 ${pctDiffEvaluations.r2adj}) > (value r2 ${valueEvaluations.r2adj})")
      pctDiffModel
    } else {
      println(s"choosing value model : (pct r2 ${pctDiffEvaluations.r2adj}) <= (value r2 ${valueEvaluations.r2adj})")
      valueModel
    }
  }

  def loadModel(measurement: Measurement): LinearRegressionModel = {
    LinearRegressionModel.load(modelLocation() + measurement.name.replaceAll("\\.", "_") + "-" + measurement.valueField)
  }

  def evaluate(measurement: Measurement, model: LinearRegressionModel, df: DataFrame): DataFrame = {
    val vector = colsToVector(measurement, df, model.getLabelCol)

    val evaluation = model.evaluate(vector)

    buildEvaluatedDF(measurement, evaluation.predictions, evaluation.meanAbsoluteError)
  }

  def buildEvaluatedDF(measurement: Measurement, predictions: DataFrame, meanAbsoluteError: Double): DataFrame = {
    val columns = List("time", measurement.valueField, "pctDiff", "prediction", "meanAbsoluteError") ++ measurement.partitions
    val schema = columns.map(x => {
      x match {
        case "prediction" => StructField(x, DoubleType)
        case "meanAbsoluteError" => StructField(x, DoubleType)
        case measurement.valueField => StructField(x, DoubleType)
        case "pctDiff" => StructField(x, DoubleType)
        case _ => StructField(x, StringType)
      }
    })

    val rows = predictions.collect.map(row => {
      val valuesMap = row.getValuesMap(List("time", measurement.valueField, "pctDiff", "prediction") ++ measurement.partitions)

      val partitionValues = measurement.partitions.map(partition => {
        valuesMap(partition).asInstanceOf[String]
      })

      val mergedList = List(
        valuesMap("time").asInstanceOf[String],
        valuesMap(measurement.valueField),
        valuesMap("pctDiff"),
        valuesMap("prediction"),
        meanAbsoluteError
      ) ++ partitionValues

      Row(mergedList: _*)
    })

    sparkSession.createDataFrame(sparkSession.sparkContext.parallelize(rows), StructType(schema))
  }

}
