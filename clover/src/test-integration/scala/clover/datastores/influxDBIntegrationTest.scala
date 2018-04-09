package clover.datastores

import org.scalatest.{BeforeAndAfterEach, FunSuite}

class influxDBIntegrationTest extends FunSuite with BeforeAndAfterEach {
  val influxDB:InfluxDBStore = new InfluxDBStore("localhost", 8086).connect().setDB("clover_test")

  override def beforeEach() {
    val inputData = List(
      ("2017-12-04T12:03:01Z", Map(
        "value1" -> 1L,
        "value2" -> 2L,
        "value3" -> 3L,
        "value4" -> 4.5,
        "value5" -> 5
      )),
      ("2017-12-04T12:03:02Z", Map(
        "value1" -> 1L,
        "value2" -> 2L,
        "value3" -> 3L,
        "value4" -> 4.5,
        "value5" -> 5
      )),
      ("2017-12-04T12:03:03Z", Map(
        "value1" -> 1L,
        "value2" -> 2L,
        "value3" -> 3L,
        "value4" -> 4.5,
        "value5" -> 5
      )),
      ("2017-12-04T12:03:04Z", Map(
        "value1" -> 1L,
        "value2" -> 2L,
        "value3" -> 3L,
        "value4" -> 4.5,
        "value5" -> 5
      )),
      ("2017-12-04T12:03:05Z", Map(
        "value1" -> 1L,
        "value2" -> 2L,
        "value3" -> 3L,
        "value4" -> 4.5,
        "value5" -> 5
      ))
    )
    influxDB.write(
      "test_measurement_1",
      Map(
        "partition_key_1" -> "partition_value_1",
        "partition_key_2" -> "partition_value_2"),
      inputData)
  }

  override def afterEach() {
    influxDB.deleteTable("test_measurement_1")
  }

  test("getLastProcessedTime") {
    // gets time from last metric when exists
    assert(influxDB.getLastProcessedTime("test_measurement_1") == "2017-12-04T12:03:05Z")

    // defaults to epoch when metric doesn't exist
    assert(influxDB.getLastProcessedTime("test_measurement_nonexistent") == "1970-01-01T00:00:00Z")
  }

  test("getSince") {
    // gets a value since a timestamp
    assert(
      influxDB.getSince("test_measurement_1", List("partition_key_1", "partition_key_2"), "value1", "2017-12-04T12:03:03Z")
      ==
      List(
        Map(
          "time" -> "2017-12-04T12:03:04Z",
          "partition_key_1" -> "partition_value_1",
          "partition_key_2" -> "partition_value_2",
          "value1" -> 1
        ),
        Map(
          "time" -> "2017-12-04T12:03:05Z",
          "partition_key_1" -> "partition_value_1",
          "partition_key_2" -> "partition_value_2",
          "value1" -> 1
        )
      )
    )
  }

  test("getRecent") {
    // Gets i most recent values
    assert(
      influxDB.getRecent("test_measurement_1", List("partition_key_1", "partition_key_2"), "value1", 2)
      ==
      List(
        Map(
          "time" -> "2017-12-04T12:03:05Z",
          "partition_key_1" -> "partition_value_1",
          "partition_key_2" -> "partition_value_2",
          "value1" -> 1
        ),
        Map(
          "time" -> "2017-12-04T12:03:04Z",
          "partition_key_1" -> "partition_value_1",
          "partition_key_2" -> "partition_value_2",
          "value1" -> 1
        )
      )
    )
  }

  test("getAllRecent") {
    assert(
      influxDB.getAllRecent("test_measurement_1", 4)
      ==
      List(
        Map(
          "time" -> "2017-12-04T12:03:05Z",
          "partition_key_1" -> "partition_value_1",
          "partition_key_2" -> "partition_value_2",
          "value1" -> 1,
          "value2" -> 2,
          "value3" -> 3,
          "value4" -> 4.5,
          "value5" -> 5
        ),
        Map(
          "time" -> "2017-12-04T12:03:04Z",
          "partition_key_1" -> "partition_value_1",
          "partition_key_2" -> "partition_value_2",
          "value1" -> 1,
          "value2" -> 2,
          "value3" -> 3,
          "value4" -> 4.5,
          "value5" -> 5
        ),
        Map(
          "time" -> "2017-12-04T12:03:03Z",
          "partition_key_1" -> "partition_value_1",
          "partition_key_2" -> "partition_value_2",
          "value1" -> 1,
          "value2" -> 2,
          "value3" -> 3,
          "value4" -> 4.5,
          "value5" -> 5
        ),
        Map(
          "time" -> "2017-12-04T12:03:02Z",
          "partition_key_1" -> "partition_value_1",
          "partition_key_2" -> "partition_value_2",
          "value1" -> 1,
          "value2" -> 2,
          "value3" -> 3,
          "value4" -> 4.5,
          "value5" -> 5
        )
      )
    )
  }

  test("write") {
    assert(
      influxDB.getRecent("test_measurement_1", List("partition_key_1", "partition_key_2"), "value1", 1)
      ==
      List(
        Map(
          "time" -> "2017-12-04T12:03:05Z",
          "partition_key_1" -> "partition_value_1",
          "partition_key_2" -> "partition_value_2",
          "value1" -> 1
        )
      )
    )

    val inputData = List(
      ("2017-12-04T12:03:06Z", Map(
        "value1" -> 6L,
        "value2" -> 7L,
        "value3" -> 8L,
        "value4" -> 9.5,
        "value5" -> 1
      ))
    )

    val result = influxDB.write(
      "test_measurement_1",
      Map(
        "partition_key_1" -> "partition_value_1",
        "partition_key_2" -> "partition_value_2"
      ),
      inputData)
    assert(result == true)
    assert(
      influxDB.getRecent("test_measurement_1", List("partition_key_1", "partition_key_2"), "value1", 1)
        ==
        List(
          Map(
            "time" -> "2017-12-04T12:03:06Z",
            "partition_key_1" -> "partition_value_1",
            "partition_key_2" -> "partition_value_2",
            "value1" -> 6
          )
        )
    )
    assert(
      influxDB.getRecent("test_measurement_1", List("partition_key_1", "partition_key_2"), "value4", 1)
        ==
        List(
          Map(
            "time" -> "2017-12-04T12:03:06Z",
            "partition_key_1" -> "partition_value_1",
            "partition_key_2" -> "partition_value_2",
            "value4" -> 9.5
          )
        )
    )
    assert(
      influxDB.getRecent("test_measurement_1", List("partition_key_1", "partition_key_2"), "value5", 1)
        ==
        List(
          Map(
            "time" -> "2017-12-04T12:03:06Z",
            "partition_key_1" -> "partition_value_1",
            "partition_key_2" -> "partition_value_2",
            "value5" -> 1
          )
        )
    )

  }

  test("deleteTable") {
    assert(
      influxDB.getRecent("test_measurement_1", List("partition_key_1", "partition_key_2"), "value1", 1)
        ==
        List(
          Map(
            "time" -> "2017-12-04T12:03:05Z",
            "partition_key_1" -> "partition_value_1",
            "partition_key_2" -> "partition_value_2",
            "value1" -> 1
          )
        )
    )

    influxDB.deleteTable("test_measurement_1")

    assert(influxDB.getRecent("test_measurement_1", List("partition_key_1", "partition_key_2"), "value1", 1) == List())
  }

}
