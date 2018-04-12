package clover

import java.util.{Date, TimeZone}

object Util {

  val format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
  format.setTimeZone(TimeZone.getTimeZone("UTC"))

  def timeStringToLong(timeString: String): Long = {
    format.parse(timeString).getTime
  }

  def timeLongToString(timeLong: Long): String = {
    format.format(new Date(timeLong))
  }

}
