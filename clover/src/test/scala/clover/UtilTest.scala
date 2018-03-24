package clover

import org.scalatest.FunSuite

class UtilTest extends FunSuite {

  test("timeStringToLong") {
    assert(Util.timeStringToLong("2018-01-06T03:07:29Z") == 1515208049000L)
  }

  test("timeLongToString") {
    assert(Util.timeLongToString(1515208049000L) == "2018-01-06T03:07:29Z")
  }
}
