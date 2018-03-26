package clover.service

import clover.transformers.{LinearRegressionTransformer, Transformer}
import clover._
import org.apache.spark.sql.functions.desc
import org.apache.spark.sql.{DataFrame, SparkSession}

object TransformMetrics {

  val sparkSession:SparkSession = SparkSession
    .builder()
    .master("local")
    .appName("Clover")
    .getOrCreate()

  import sparkSession.implicits._

  val metricSources:List[MetricSource] = Config.metricSources()
  val cloverStore = Config.cloverStore()

  def main(args: Array[String]) {
    run(List(new LinearRegressionTransformer(sparkSession)))
  }

  def run(transformers: List[Transformer]): Unit = {
    while(true) {
      metricSources.foreach(metricSource => {
        metricSource.measurements.foreach(measurement => {
          var metricsDF = getInitialMetrics(metricSource, measurement)
            metricsDF = reloadMetrics(metricSource, measurement, metricsDF)
            runTransformers(transformers, measurement, metricsDF)
        })
      })
      Thread.sleep(1000)
    }
  }

  def runTransformers(transformers: List[Transformer], measurement: Measurement, metricsDF: DataFrame): Unit = {
    transformers.foreach(transformer => {
      val lastProcessedTime = cloverStore.getLastProcessedTime(transformer.tableName(), measurement.name)
      val transformedMetrics = transformer.transform(metricsDF, lastProcessedTime)
      writeTransformations(transformer, measurement, transformedMetrics)
    })
  }

  def getInitialMetrics(metricSource: MetricSource, measurement: Measurement): DataFrame = {
    metricSource.database
      .getRecent(measurement.name, measurement.valueField, 10000)
      .map(x => {
        val timeString = x.getOrElse("time", "").asInstanceOf[String]
        val value = x.getOrElse(measurement.valueField, 0).asInstanceOf[BigDecimal]
        Metric(Util.timeStringToLong(timeString), value)
    }).toDF()
  }

  def reloadMetrics(metricSource: MetricSource, measurement: Measurement, df: DataFrame): DataFrame = {
    val lastTime = df.orderBy(desc("time")).take(1).head(0).asInstanceOf[Long]

    val lastTimeString = Util.timeLongToString(lastTime)

    metricSource.database
      .getSince(measurement.name, measurement.valueField, lastTimeString)
      .map(x => {
        val timeString = x.getOrElse("time", "").asInstanceOf[String]
        val value = x.getOrElse(measurement.valueField, 0).asInstanceOf[BigDecimal]
        Metric(Util.timeStringToLong(timeString), value)
      })
      .toDF()
      .union(df)
      .orderBy(desc("time"))
      .limit(2000)
  }

  def writeTransformations(transformer: Transformer, measurement: Measurement, metricsDF: DataFrame): Unit = {
    val columns = metricsDF.columns

    val data = metricsDF.collect.map(metric => {
      val values = metric.getValuesMap(columns)
      val timeString = Util.timeLongToString(values.getOrElse("time", 0L))
      val valueMap = values.filterKeys(_ != "time")
        .map(x => {
          val doubleValue = x._2.getClass.toString match {
            case "class java.math.BigDecimal" => x._2.asInstanceOf[java.math.BigDecimal].doubleValue
            case "class java.lang.Double" => x._2.asInstanceOf[java.lang.Double].doubleValue()
          }
          (x._1, (doubleValue * 10000).toLong)
        })
      (
        timeString,
        valueMap
      )
    }).toList

    cloverStore.write(
      transformer.tableName(),
      measurement.name,
      measurement.valueField,
      data
    )
  }

}
