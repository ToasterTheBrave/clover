package clover.transformers

import clover.{Measurement, Util}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{asc, desc, mean, stddev, col, lag}

class LinearRegressionTransformer(sparkSession: SparkSession) extends Transformer {

  def databaseName(): String = {
    "linear_regression_transformed"
  }

  def transform(df: DataFrame, measurement: Measurement, lastProcessedTime: String): DataFrame = {

//    val fullWindow = Window
//      .partitionBy(measurement.partitions.map(col):_*)
//      .orderBy(asc("time"))

//    val changeDF = df.withColumn("change", (col(measurement.valueField) - lag(measurement.valueField, 1).over(fullWindow)) / lag(measurement.valueField, 1).over(fullWindow))

    val window_1024_512 = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-1024, -512)

    val window_512_256 = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-512, -256)

    val window_256_128 = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-256, -128)

    val window_128_64 = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-128, -64)

    val window_64_32 = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-64, -32)

    val window_32_16 = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-32, -16)

    val window_16_8 = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-16, -8)

    val window_8_4 = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-8, -4)

    val window_4_2 = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-4, -2)

    val window_2_1 = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-2, -1)

    val value_1024_512_stddev = stddev(measurement.valueField).over(window_1024_512)
    val value_1024_512_mean = mean(measurement.valueField).over(window_1024_512)
    val value_512_256_stddev = stddev(measurement.valueField).over(window_512_256)
    val value_512_256_mean = mean(measurement.valueField).over(window_512_256)
    val value_256_128_stddev = stddev(measurement.valueField).over(window_256_128)
    val value_256_128_mean = mean(measurement.valueField).over(window_256_128)
    val value_128_64_stddev = stddev(measurement.valueField).over(window_128_64)
    val value_128_64_mean = mean(measurement.valueField).over(window_128_64)
    val value_64_32_stddev = stddev(measurement.valueField).over(window_64_32)
    val value_64_32_mean = mean(measurement.valueField).over(window_64_32)
    val value_32_16_stddev = stddev(measurement.valueField).over(window_32_16)
    val value_32_16_mean = mean(measurement.valueField).over(window_32_16)
    val value_16_8_stddev = stddev(measurement.valueField).over(window_16_8)
    val value_16_8_mean = mean(measurement.valueField).over(window_16_8)
    val value_8_4_stddev = stddev(measurement.valueField).over(window_8_4)
    val value_8_4_mean = mean(measurement.valueField).over(window_8_4)
    val value_4_2_stddev = stddev(measurement.valueField).over(window_4_2)
    val value_4_2_mean = mean(measurement.valueField).over(window_4_2)
    val value_2_1_stddev = stddev(measurement.valueField).over(window_2_1)
    val value_2_1_mean = mean(measurement.valueField).over(window_2_1)

//    val change_1024_512_stddev = stddev("change").over(window_1024_512)
//    val change_1024_512_mean = mean("change").over(window_1024_512)
//    val change_512_256_stddev = stddev("change").over(window_512_256)
//    val change_512_256_mean = mean("change").over(window_512_256)
//    val change_256_128_stddev = stddev("change").over(window_256_128)
//    val change_256_128_mean = mean("change").over(window_256_128)
//    val change_128_64_stddev = stddev("change").over(window_128_64)
//    val change_128_64_mean = mean("change").over(window_128_64)
//    val change_64_32_stddev = stddev("change").over(window_64_32)
//    val change_64_32_mean = mean("change").over(window_64_32)
//    val change_32_16_stddev = stddev("change").over(window_32_16)
//    val change_32_16_mean = mean("change").over(window_32_16)
//    val change_16_8_stddev = stddev("change").over(window_16_8)
//    val change_16_8_mean = mean("change").over(window_16_8)
//    val change_8_4_stddev = stddev("change").over(window_8_4)
//    val change_8_4_mean = mean("change").over(window_8_4)
//    val change_4_2_stddev = stddev("change").over(window_4_2)
//    val change_4_2_mean = mean("change").over(window_4_2)
//    val change_2_1_stddev = stddev("change").over(window_2_1)
//    val change_2_1_mean = mean("change").over(window_2_1)
//    val change_1_0_stddev = stddev("change").over(window_1_0)

    val columns = List(
        col("time"),
        col(measurement.valueField)
//        col("change")
      ) ++
      measurement.partitions.map(col) ++
      List(
        value_1024_512_stddev as "value_1024_512_stddev",
        value_1024_512_mean as "value_1024_512_mean",
        value_512_256_stddev as "value_512_256_stddev",
        value_512_256_mean as "value_512_256_mean",
        value_256_128_stddev as "value_256_128_stddev",
        value_256_128_mean as "value_256_128_mean",
        value_128_64_stddev as "value_128_64_stddev",
        value_128_64_mean as "value_128_64_mean",
        value_64_32_stddev as "value_64_32_stddev",
        value_64_32_mean as "value_64_32_mean",
        value_32_16_stddev as "value_32_16_stddev",
        value_32_16_mean as "value_32_16_mean",
        value_16_8_stddev as "value_16_8_stddev",
        value_16_8_mean as "value_16_8_mean",
        value_8_4_stddev as "value_8_4_stddev",
        value_8_4_mean as "value_8_4_mean",
        value_4_2_stddev as "value_4_2_stddev",
        value_4_2_mean as "value_4_2_mean",
        value_2_1_stddev as "value_2_1_stddev",
        value_2_1_mean as "value_2_1_mean"
//        change_1024_512_stddev as "change_1024_512_stddev",
//        change_1024_512_mean as "change_1024_512_mean",
//        change_512_256_stddev as "change_512_256_stddev",
//        change_512_256_mean as "change_512_256_mean",
//        change_256_128_stddev as "change_256_128_stddev",
//        change_256_128_mean as "change_256_128_mean",
//        change_128_64_stddev as "change_128_64_stddev",
//        change_128_64_mean as "change_128_64_mean",
//        change_64_32_stddev as "change_64_32_stddev",
//        change_64_32_mean as "change_64_32_mean",
//        change_32_16_stddev as "change_32_16_stddev",
//        change_32_16_mean as "change_32_16_mean",
//        change_16_8_stddev as "change_16_8_stddev",
//        change_16_8_mean as "change_16_8_mean",
//        change_8_4_stddev as "change_8_4_stddev",
//        change_8_4_mean as "change_8_4_mean",
//        change_4_2_stddev as "change_4_2_stddev",
//        change_4_2_mean as "change_4_2_mean",
//        change_2_1_stddev as "change_2_1_stddev",
//        change_2_1_mean as "change_2_1_mean",
      )
    df.select(columns:_*)
      .where("time >= " + Util.timeStringToLong(lastProcessedTime))
      .na.fill(0)
  }
}
