package clover.datastores

import com.paulgoldbaum.influxdbclient.Parameter.Precision
import com.paulgoldbaum.influxdbclient._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.collection.breakOut

class InfluxDBStore(host: String = "", port: Integer = 8086) {

  var influxdb:InfluxDB = _
  var db: Database = _

  def connect(): InfluxDBStore = {
    influxdb = InfluxDB.connect(host, port)
    this
  }

  def setDB(database: String): InfluxDBStore = {
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

  def getLastProcessedTime(measurementName: String): String = {
    val future = db.query(s"select * from $measurementName order by time desc limit 1")
    val result = Await.result(future, 60.seconds)
    if(result.series.nonEmpty) {
      result.series.head.records.head.allValues.head.asInstanceOf[String]
    } else {
      "1970-01-01T00:00:00Z"
    }
  }

  def getSince(measurement: String, partitions: List[String], valueField: String, datetime: String, minutesPrevious: Int, limit: Int): List[Map[String, Any]] = {
    val future = db.query(s"select time, ${partitions.mkString(",")}, $valueField from $measurement where time > '$datetime' - ${minutesPrevious}m order by time asc limit $limit")
    val result = Await.result(future, 60.seconds)

    resultAsMap(result)
  }

  def getAllSince(measurement: String, datetime: String, limit: Int = 1000000): List[Map[String, Any]] = {
    val future = db.query(s"select * from $measurement where time > '$datetime' order by time asc limit $limit")
    val result = Await.result(future, 60.seconds)

    resultAsMap(result)
  }

  def getAllBetween(measurement: String, startTime: String, endTime: String): List[Map[String, Any]] = {
    val future = db.query(s"select * from $measurement where time >= '$startTime' and time <= '$endTime'")
    val result = Await.result(future, 60.seconds)

    resultAsMap(result)
  }

  def getRecent(measurement: String, partitions: List[String], valueField: String, count: Int): List[Map[String, Any]] = {
    val future = db.query(s"select time, ${partitions.mkString(",")}, $valueField from $measurement order by time desc limit $count")
    val result = Await.result(future, 60.seconds)

    resultAsMap(result)
  }

  def getAllRecent(measurement: String, count: Int): List[Map[String, Any]] = {
    val future = db.query(s"select * from $measurement order by time desc limit $count")
    val result = Await.result(future, 60.seconds)

    resultAsMap(result)
  }

  def write(measurementName: String, tags: Map[String, String], data: List[(String, Map[String, Any])]): Boolean = {
    val points = data.map(metricData => {
      Point(
        measurementName,
        clover.Util.timeStringToLong(metricData._1),
        tags.toSeq.map(x => {
          Tag(x._1, x._2)
        }),
        metricData._2.map(x => {
          x._2.getClass.toString match {
            case "class java.lang.Double" => DoubleField(x._1, x._2.asInstanceOf[Double])
            case "class java.lang.Integer" => LongField(x._1, x._2.asInstanceOf[Integer].longValue)
            case "class java.lang.Long" => LongField(x._1, x._2.asInstanceOf[Long])
            case _ => throw new Exception("Unsupported field type: " + x._2.getClass.toString)
          }
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
    val future = db.query(s"drop measurement $tableName")
    val result = Await.result(future, 60.seconds)
  }

}
