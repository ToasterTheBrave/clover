package clover.datastores

import org.scalatest.{BeforeAndAfter, FunSuite}

class influxDBIntegrationTest extends FunSuite with BeforeAndAfter {
  val influxDB:InfluxDBStore = new InfluxDBStore("localhost", 8086, "clover_test").connect()

  before {
    val inputData = List(
      ("2017-12-04T12:03:01Z", Map(
        "value1" -> 1.toLong,
        "value2" -> 2.toLong,
        "value3" -> 3.toLong,
        "value4" -> 4.toLong,
        "value5" -> 5.toLong
      )),
      ("2017-12-04T12:03:02Z", Map(
        "value1" -> 1.toLong,
        "value2" -> 2.toLong,
        "value3" -> 3.toLong,
        "value4" -> 4.toLong,
        "value5" -> 5.toLong
      )),
      ("2017-12-04T12:03:03Z", Map(
        "value1" -> 1.toLong,
        "value2" -> 2.toLong,
        "value3" -> 3.toLong,
        "value4" -> 4.toLong,
        "value5" -> 5.toLong
      )),
      ("2017-12-04T12:03:04Z", Map(
        "value1" -> 1.toLong,
        "value2" -> 2.toLong,
        "value3" -> 3.toLong,
        "value4" -> 4.toLong,
        "value5" -> 5.toLong
      )),
      ("2017-12-04T12:03:05Z", Map(
        "value1" -> 1.toLong,
        "value2" -> 2.toLong,
        "value3" -> 3.toLong,
        "value4" -> 4.toLong,
        "value5" -> 5.toLong
      ))
    )
    influxDB.write("test_table", "test_metric_1", "test_value_field", inputData)
  }

  after {
    influxDB.deleteTable("test_table")
  }

  test("getLastProcessedTime") {
    // gets time from last metric when exists
    assert(influxDB.getLastProcessedTime("test_table", "test_metric_1") == "2017-12-04T12:03:05Z")

    // defaults to epoch when metric doesn't exist
    assert(influxDB.getLastProcessedTime("test_table", "test_metric_nonexistent") == "1970-01-01T00:00:00Z")
  }

  test("getSince") {
    // gets a value since a timestamp
    assert(
      influxDB.getSince("test_table", "value1", "2017-12-04T12:03:03Z")
      ==
      List(
        Map(
          "time" -> "2017-12-04T12:03:04Z",
          "value1" -> 1
        ),
        Map(
          "time" -> "2017-12-04T12:03:05Z",
          "value1" -> 1
        )
      )
    )
  }

  test("getRecent") {
    // Gets i most recent values
    assert(
      influxDB.getRecent("test_table", "value1", 2)
      ==
      List(
        Map(
          "time" -> "2017-12-04T12:03:05Z",
          "value1" -> 1
        ),
        Map(
          "time" -> "2017-12-04T12:03:04Z",
          "value1" -> 1
        )
      )
    )
  }

  test("write") {
    assert(
      influxDB.getRecent("test_table", "value1", 1)
      ==
      List(
        Map(
          "time" -> "2017-12-04T12:03:05Z",
          "value1" -> 1
        )
      )
    )

    val inputData = List(
      ("2017-12-04T12:03:06Z", Map(
        "value1" -> 6.toLong,
        "value2" -> 7.toLong,
        "value3" -> 8.toLong,
        "value4" -> 9.toLong,
        "value5" -> 1.toLong
      ))
    )

    val result = influxDB.write("test_table", "test_metric_1", "test_value_field", inputData)
    assert(result == true)
    assert(
      influxDB.getRecent("test_table", "value1", 1)
        ==
        List(
          Map(
            "time" -> "2017-12-04T12:03:06Z",
            "value1" -> 6
          )
        )
    )
  }

  test("deleteTable") {
    assert(
      influxDB.getRecent("test_table", "value1", 1)
        ==
        List(
          Map(
            "time" -> "2017-12-04T12:03:05Z",
            "value1" -> 1
          )
        )
    )

    influxDB.deleteTable("test_table")

    assert(influxDB.getRecent("test_table", "value1", 1) == List())
  }

}
