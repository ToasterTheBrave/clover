package clover.transformers

import org.apache.spark.sql.DataFrame

trait Transformer {
  def tableName(): String
  def transform(df: DataFrame, lastProcessedTime: String): DataFrame
}
