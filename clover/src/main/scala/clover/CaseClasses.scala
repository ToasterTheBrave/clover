package clover

import clover.datastores.InfluxDBStore

case class Measurement(name: String, partitions: List[String], valueField: String)

case class MetricSource(database: InfluxDBStore, measurements: List[Measurement])

case class Metric(time: Long, value: BigDecimal)
