package clover.algorithms

import clover.Measurement
import java.io._
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import scala.io.Source

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

  def saveModel(model: LinearRegressionModel, measurement: Measurement): Unit = {
    val modelFolderLocation = modelLocation() + measurement.name.replaceAll("\\.", "_") + "-" + measurement.valueField
    val r2 = model.summary.r2adj
    if(r2 >= .3) {
      model.save(modelFolderLocation)

      val meanAbsoluteErrorPW = new PrintWriter(new File(modelFolderLocation + "/mean-absolute-error.txt"))
      meanAbsoluteErrorPW.write(s"${model.summary.meanAbsoluteError}")
      meanAbsoluteErrorPW.close()

      val r2adjPW = new PrintWriter(new File(modelFolderLocation + "/r2adj.txt"))
      r2adjPW.write(s"$r2")
      r2adjPW.close()

    } else {
      println(s"WARNING - not saving model because of low r2: ${r2}")
    }

  }

  def colsToVector(measurement: Measurement, df: DataFrame, labelField: String): DataFrame = {
    val inputCols = df.columns.filter(x => {
      val pattern = if(labelField == "change") {
        "^change.*".r
      } else {
        "^value.*".r
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
      (List("time", measurement.valueField) ++ measurement.partitions).contains(x)
//      (List("time", measurement.valueField, "change") ++ measurement.partitions).contains(x)
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
    println(s"r2adj: ${valueEvaluations.r2adj}")

//    val changeVector = colsToVector(measurement, df, "change")
//    val changeDfSplit = changeVector.randomSplit(Array(.8, .2))
//    val changeLinearRegression = new LinearRegression()
//      .setMaxIter(100)
//      .setRegParam(1)
//      .setElasticNetParam(0.8)
//      .setLabelCol("change")
//    val changeModel = changeLinearRegression.fit(changeDfSplit(0))
//    val changeEvaluations = changeModel.evaluate(changeDfSplit(1))
//
//    if(changeEvaluations.r2adj > valueEvaluations.r2adj) {
//      println(s"choosing change model : (pct r2 ${changeEvaluations.r2adj}) > (value r2 ${valueEvaluations.r2adj})")
//      changeModel
//    } else {
//      println(s"choosing value model : (pct r2 ${changeEvaluations.r2adj}) <= (value r2 ${valueEvaluations.r2adj})")
      valueModel
//    }
  }

  def loadModel(measurement: Measurement): LinearRegressionModel = {
    LinearRegressionModel.load(modelLocation() + measurement.name.replaceAll("\\.", "_") + "-" + measurement.valueField)
  }

  def evaluate(measurement: Measurement, model: LinearRegressionModel, df: DataFrame): DataFrame = {
    val modelFolderLocation = modelLocation() + measurement.name.replaceAll("\\.", "_") + "-" + measurement.valueField

    val bufferedSource = Source.fromFile(modelFolderLocation + "/mean-absolute-error.txt")
    val contents = bufferedSource.getLines.mkString("")
    val meanAbsoluteError = contents.toDouble

    val vector = colsToVector(measurement, df, model.getLabelCol)

    val predictions = model.transform(vector)

    buildEvaluatedDF(measurement, predictions, meanAbsoluteError)
  }

  def buildEvaluatedDF(measurement: Measurement, predictions: DataFrame, meanAbsoluteError: Double): DataFrame = {
    val columns = List("time", measurement.valueField, "prediction", "meanAbsoluteError") ++ measurement.partitions
//    val columns = List("time", measurement.valueField, "change", "prediction", "meanAbsoluteError") ++ measurement.partitions
    val schema = columns.map(x => {
      x match {
        case "prediction" => StructField(x, DoubleType)
        case "meanAbsoluteError" => StructField(x, DoubleType)
        case measurement.valueField => StructField(x, DoubleType)
//        case "change" => StructField(x, DoubleType)
        case _ => StructField(x, StringType)
      }
    })

    val rows = predictions.collect.map(row => {
      val valuesMap = row.getValuesMap(List("time", measurement.valueField, "prediction") ++ measurement.partitions)
//      val valuesMap = row.getValuesMap(List("time", measurement.valueField, "change", "prediction") ++ measurement.partitions)

      val partitionValues = measurement.partitions.map(partition => {
        valuesMap(partition).asInstanceOf[String]
      })

      val mergedList = List(
        valuesMap("time").asInstanceOf[String],
        valuesMap(measurement.valueField),
//        valuesMap("change"),
        valuesMap("prediction"),
        meanAbsoluteError
      ) ++ partitionValues

      Row(mergedList: _*)
    })

    sparkSession.createDataFrame(sparkSession.sparkContext.parallelize(rows), StructType(schema))
  }

}
