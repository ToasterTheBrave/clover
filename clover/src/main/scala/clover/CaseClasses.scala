package clover

import clover.datastores.InfluxDB

case class Measurement(name: String, valueField: String)

case class MetricSource(database: InfluxDB, measurements: List[Measurement])

case class Metric(time: Long, value: BigDecimal)
