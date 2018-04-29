package clover.datastores

import com.paulgoldbaum.influxdbclient.{QueryResult, Record, Series}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

class InfluxDBStoreTest extends FunSuite with BeforeAndAfter with MockitoSugar {

  val influxDB:InfluxDBStore = new InfluxDBStore("localhost", 8086)

  test("resultAsMap - list of maps when non-empty result") {
    val record1 = mock[Record]
    val record2 = mock[Record]
    when(record1.allValues).thenReturn(List("record_1_value_1", "record_1_value_2"))
    when(record2.allValues).thenReturn(List("record_2_value_1", "record_2_value_2"))

    val series = mock[Series]
    when(series.columns).thenReturn(List("key_1", "key_2"))
    when(series.records).thenReturn(List(record1, record2))

    val queryResult = mock[QueryResult]
    when(queryResult.series).thenReturn(List(series))

    assert(
      influxDB.resultAsMap(queryResult, List())
      ==
      List(
        Map(
          "key_1" -> "record_1_value_1",
          "key_2" -> "record_1_value_2"
        ),
        Map(
          "key_1" -> "record_2_value_1",
          "key_2" -> "record_2_value_2"
        )
      )
    )
  }

  test("resultAsMap - Empty list when empty result") {
    val queryResult = mock[QueryResult]
    when(queryResult.series).thenReturn(List())
    assert(influxDB.resultAsMap(queryResult, List()) == List())
  }

}
