package clover.algorithms

import clover.Measurement
import org.apache.spark.ml.regression.LinearRegressionModel
import org.apache.spark.sql.DataFrame

trait Algorithm {
  def databaseName(): String
  def modelLocation(): String
  def train(measurement: Measurement, df: DataFrame): LinearRegressionModel
}
