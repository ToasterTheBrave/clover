package clover.algorithms

import clover.Measurement
import org.apache.spark.ml.regression.LinearRegressionModel
import org.apache.spark.sql.DataFrame

trait Algorithm {
  def transformedDatabaseName(): String
  def evaluatedDatabaseName(): String
  def modelLocation(): String
  def train(measurement: Measurement, df: DataFrame): LinearRegressionModel
  def loadModel(measurement: Measurement): LinearRegressionModel
  def evaluate(measurement: Measurement, model: LinearRegressionModel, df: DataFrame): DataFrame
}
