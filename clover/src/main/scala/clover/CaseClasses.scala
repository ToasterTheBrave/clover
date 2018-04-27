package clover

case class Measurement(name: String, partitions: List[String], valueField: String)

case class MetricSource(host: String, port: Int, db: String, measurements: List[Measurement])

case class CloverConfig(reportLocation: String, cloverStore: CloverStore, metricSources: List[MetricSource])

case class CloverStore(host: String, port: Int)

