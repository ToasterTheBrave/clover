package clover.datastores

import com.paulgoldbaum.influxdbclient.Parameter.Precision
import com.paulgoldbaum.influxdbclient._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.collection.breakOut

class InfluxDB(host: String = "", port: Integer = 8086, database: String) {

  var influxdb:com.paulgoldbaum.influxdbclient.InfluxDB = _
  var db: Database = _

  def connect(): InfluxDB = {
    influxdb = com.paulgoldbaum.influxdbclient.InfluxDB.connect(host, port)
    db = influxdb.selectDatabase(database)
    this
  }


  def resultAsMap(result: QueryResult): List[Map[String, Any]] = {
    if(result.series.nonEmpty) {
      val columns = result.series.head.columns
      val records = result.series.head.records
      records.map(x => {
        (columns zip x.allValues) (breakOut): Map[String, Any]
      })
    } else {
      List()
    }
  }

  def getLastProcessedTime(tableName: String, measurementName: String): String = {
    val future = db.query(s"select * from $tableName where metric='$measurementName' order by time desc limit 1")
    val result = Await.result(future, 60.seconds)
    if(result.series.nonEmpty) {
      result.series.head.records.head.allValues.head.asInstanceOf[String]
    } else {
      "1970-01-01T00:00:00Z"
    }
  }

  def getSince(measurement: String, valueField: String, datetime: String): List[Map[String, Any]] = {
    val future = db.query(s"select $valueField from $measurement where time > '$datetime'")
    val result = Await.result(future, 60.seconds)

    resultAsMap(result)
  }

  def getRecent(measurement: String, valueField: String, count: Int): List[Map[String, Any]] = {
    val future = db.query(s"select time, $valueField from $measurement order by time desc limit $count")
    val result = Await.result(future, 60.seconds)

    resultAsMap(result)
  }

  def write(tableName: String, metricName: String, valueFieldName: String, data: List[(String, Map[String, Long])]): Boolean = {
    val points = data.map(metricData => {
      Point(
        tableName,
        clover.Util.timeStringToLong(metricData._1),
        List(
          Tag("metric", metricName),
          Tag("valueField", valueFieldName)),
        metricData._2.map(x => {
          LongField(x._1, x._2)
        }).toSeq
      )
    })
    try {
      val future = db.bulkWrite(points, precision = Precision.MILLISECONDS)
      val result = Await.result(future, 60.seconds)
      result
    } catch {
      case ea: MalformedRequestException => {
        println(ea.getMessage)
        throw ea
      }
      case e: Exception => {
        println(e)
        throw e
        false
      }
    }
  }

  def deleteTable(tableName: String): Unit = {
    val future = db.query(s"delete from $tableName")
    val result = Await.result(future, 60.seconds)
  }

}
