package clover

import java.time.Instant
import java.util.Date

object Util {

  val timeZoneDiff: Integer = 7 * 60 * 60 * 1000
  val format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

  def timeStringToLong(timeString: String): Long = {
    format.parse(timeString).getTime - timeZoneDiff
  }

  def timeLongToString(timeLong: Long): String = {
    format.format(new Date(timeLong + timeZoneDiff))
  }

}
