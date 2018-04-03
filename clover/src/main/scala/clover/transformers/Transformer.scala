package clover.transformers

import clover.Measurement
import org.apache.spark.sql.DataFrame

trait Transformer {
  def databaseName(): String
  def transform(df: DataFrame, measurement: Measurement, lastProcessedTime: String): DataFrame
}
