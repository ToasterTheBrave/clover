package clover.transformers

import clover.{Measurement, Util}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{asc, desc, mean, stddev, col, isnan, lag}

class LinearRegressionTransformer(sparkSession: SparkSession) extends Transformer {

  def databaseName(): String = {
    "linear_regression_transformed"
  }

  def transform(df: DataFrame, measurement: Measurement, lastProcessedTime: String): DataFrame = {

    val fullWindow = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))

    val pctChangeDF = df.withColumn("pctDiff", (col(measurement.valueField) - lag(measurement.valueField, 1).over(fullWindow)) / lag(measurement.valueField, 1).over(fullWindow))

    val last1000Window = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-1000, 0)

    val last100Window = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-100, 0)

    val last10Window = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-10, 0)

    val last9Window = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-9, 0)

    val last8Window = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-8, 0)

    val last7Window = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-7, 0)

    val last6Window = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-6, 0)

    val last5Window = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-5, 0)

    val last4Window = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-4, 0)

    val last3Window = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-3, 0)

    val last2Window = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-2, 0)

    val last1Window = Window
      .partitionBy(measurement.partitions.map(col):_*)
      .orderBy(asc("time"))
      .rowsBetween(-1, 0)

    val last1000STD = stddev(measurement.valueField).over(last1000Window)
    val last1000Mean = mean(measurement.valueField).over(last1000Window)
    val last100STD = stddev(measurement.valueField).over(last100Window)
    val last100Mean = mean(measurement.valueField).over(last100Window)
    val last10STD = stddev(measurement.valueField).over(last10Window)
    val last10Mean = mean(measurement.valueField).over(last10Window)
    val last9STD = stddev(measurement.valueField).over(last9Window)
    val last9Mean = mean(measurement.valueField).over(last9Window)
    val last8STD = stddev(measurement.valueField).over(last8Window)
    val last8Mean = mean(measurement.valueField).over(last8Window)
    val last7STD = stddev(measurement.valueField).over(last7Window)
    val last7Mean = mean(measurement.valueField).over(last7Window)
    val last6STD = stddev(measurement.valueField).over(last6Window)
    val last6Mean = mean(measurement.valueField).over(last6Window)
    val last5STD = stddev(measurement.valueField).over(last5Window)
    val last5Mean = mean(measurement.valueField).over(last5Window)
    val last4STD = stddev(measurement.valueField).over(last4Window)
    val last4Mean = mean(measurement.valueField).over(last4Window)
    val last3STD = stddev(measurement.valueField).over(last3Window)
    val last3Mean = mean(measurement.valueField).over(last3Window)
    val last2STD = stddev(measurement.valueField).over(last2Window)
    val last2Mean = mean(measurement.valueField).over(last2Window)
    val last1STD = stddev(measurement.valueField).over(last1Window)
    val last1Mean = mean(measurement.valueField).over(last1Window)

    val pctDiffLast1000STD = stddev("pctDiff").over(last1000Window)
    val pctDiffLast1000Mean = mean("pctDiff").over(last1000Window)
    val pctDiffLast100STD = stddev("pctDiff").over(last100Window)
    val pctDiffLast100Mean = mean("pctDiff").over(last100Window)
    val pctDiffLast10STD = stddev("pctDiff").over(last10Window)
    val pctDiffLast10Mean = mean("pctDiff").over(last10Window)
    val pctDiffLast9STD = stddev("pctDiff").over(last9Window)
    val pctDiffLast9Mean = mean("pctDiff").over(last9Window)
    val pctDiffLast8STD = stddev("pctDiff").over(last8Window)
    val pctDiffLast8Mean = mean("pctDiff").over(last8Window)
    val pctDiffLast7STD = stddev("pctDiff").over(last7Window)
    val pctDiffLast7Mean = mean("pctDiff").over(last7Window)
    val pctDiffLast6STD = stddev("pctDiff").over(last6Window)
    val pctDiffLast6Mean = mean("pctDiff").over(last6Window)
    val pctDiffLast5STD = stddev("pctDiff").over(last5Window)
    val pctDiffLast5Mean = mean("pctDiff").over(last5Window)
    val pctDiffLast4STD = stddev("pctDiff").over(last4Window)
    val pctDiffLast4Mean = mean("pctDiff").over(last4Window)
    val pctDiffLast3STD = stddev("pctDiff").over(last3Window)
    val pctDiffLast3Mean = mean("pctDiff").over(last3Window)
    val pctDiffLast2STD = stddev("pctDiff").over(last2Window)
    val pctDiffLast2Mean = mean("pctDiff").over(last2Window)
    val pctDiffLast1STD = stddev("pctDiff").over(last1Window)
    val pctDiffLast1Mean = mean("pctDiff").over(last1Window)

    val columns = List(
        col("time"),
        col(measurement.valueField),
        col("pctDiff")
      ) ++
      measurement.partitions.map(col) ++
      List(
        last1000STD as "last1000STD",
        last1000Mean as "last1000Mean",
        last100STD as "last100STD",
        last100Mean as "last100Mean",
        last10STD as "last10STD",
        last10Mean as "last10Mean",
        last9STD as "last9STD",
        last9Mean as "last9Mean",
        last8STD as "last8STD",
        last8Mean as "last8Mean",
        last7STD as "last7STD",
        last7Mean as "last7Mean",
        last6STD as "last6STD",
        last6Mean as "last6Mean",
        last5STD as "last5STD",
        last5Mean as "last5Mean",
        last4STD as "last4STD",
        last4Mean as "last4Mean",
        last3STD as "last3STD",
        last3Mean as "last3Mean",
        last2STD as "last2STD",
        last2Mean as "last2Mean",
        last1STD as "last1STD",
        last1Mean as "last1Mean",
        pctDiffLast1000STD as "pctDiffLast1000STD",
        pctDiffLast1000Mean as "pctDiffLast1000Mean",
        pctDiffLast100STD as "pctDiffLast100STD",
        pctDiffLast100Mean as "pctDiffLast100Mean",
        pctDiffLast10STD as "pctDiffLast10STD",
        pctDiffLast10Mean as "pctDiffLast10Mean",
        pctDiffLast9STD as "pctDiffLast9STD",
        pctDiffLast9Mean as "pctDiffLast9Mean",
        pctDiffLast8STD as "pctDiffLast8STD",
        pctDiffLast8Mean as "pctDiffLast8Mean",
        pctDiffLast7STD as "pctDiffLast7STD",
        pctDiffLast7Mean as "pctDiffLast7Mean",
        pctDiffLast6STD as "pctDiffLast6STD",
        pctDiffLast6Mean as "pctDiffLast6Mean",
        pctDiffLast5STD as "pctDiffLast5STD",
        pctDiffLast5Mean as "pctDiffLast5Mean",
        pctDiffLast4STD as "pctDiffLast4STD",
        pctDiffLast4Mean as "pctDiffLast4Mean",
        pctDiffLast3STD as "pctDiffLast3STD",
        pctDiffLast3Mean as "pctDiffLast3Mean",
        pctDiffLast2STD as "pctDiffLast2STD",
        pctDiffLast2Mean as "pctDiffLast2Mean",
        pctDiffLast1STD as "pctDiffLast1STD",
        pctDiffLast1Mean as "pctDiffLast1Mean"
      )
    pctChangeDF.select(columns:_*)
      .where("time >= " + Util.timeStringToLong(lastProcessedTime))
      .na.fill(0)
      .orderBy(desc("time"))
  }
}
