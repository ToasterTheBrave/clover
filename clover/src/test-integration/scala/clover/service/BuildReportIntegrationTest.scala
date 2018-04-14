package clover.service

import java.io.File

import clover.Config
import org.scalatest.FunSuite

import scala.io.Source

class BuildReportIntegrationTest extends FunSuite {

  test("writeReportData - writes to a file") {
    val reportData = Map(
      "columns" -> Array(
        "'test_measurement_2 - value_field_1 - partition_3: partition_3_value_1, partition_4: partition_4_value_1'",
        "'test_measurement_2 - value_field_1 - partition_3: partition_3_value_3, partition_4: partition_4_value_3'",
        "'test_measurement_2 - value_field_1 - partition_3: partition_3_value_2, partition_4: partition_4_value_2'"
      ),
      "rows" -> Array(
        "[new Date('2018-04-03T17:57:00Z'), null, ``, 0.09, `test_measurement_2 - value_field_1\npartition_3: partition_3_value_3, partition_4: partition_4_value_3\nError: 0.09\nValue: 300.3\nExpected: 300.003 +/- 0.3`, null, ``]",
        "[new Date('2018-04-03T17:55:00Z'), 0.07, `test_measurement_2 - value_field_1\npartition_3: partition_3_value_1, partition_4: partition_4_value_1\nError: 0.07\nValue: 100.1\nExpected: 100.001 +/- 0.1`, null, ``, null, ``]",
        "[new Date('2018-04-03T17:56:00Z'), null, ``, null, ``, 0.08, `test_measurement_2 - value_field_1\npartition_3: partition_3_value_2, partition_4: partition_4_value_2\nError: 0.08\nValue: 200.2\nExpected: 200.002 +/- 0.2`]"
      )
    )

    val location = Config.reportLocation() + "/test_report.js"

    BuildReport.writeReportData(reportData, location)

    val fileContents = Source.fromFile(location).getLines.mkString("\n")

    val expectedContents = "columns = [\n  'test_measurement_2 - value_field_1 - partition_3: partition_3_value_1, partition_4: partition_4_value_1',\n  'test_measurement_2 - value_field_1 - partition_3: partition_3_value_3, partition_4: partition_4_value_3',\n  'test_measurement_2 - value_field_1 - partition_3: partition_3_value_2, partition_4: partition_4_value_2'\n];\n\nrows = [\n  [new Date('2018-04-03T17:57:00Z'), null, ``, 0.09, `test_measurement_2 - value_field_1\npartition_3: partition_3_value_3, partition_4: partition_4_value_3\nError: 0.09\nValue: 300.3\nExpected: 300.003 +/- 0.3`, null, ``],\n  [new Date('2018-04-03T17:55:00Z'), 0.07, `test_measurement_2 - value_field_1\npartition_3: partition_3_value_1, partition_4: partition_4_value_1\nError: 0.07\nValue: 100.1\nExpected: 100.001 +/- 0.1`, null, ``, null, ``],\n  [new Date('2018-04-03T17:56:00Z'), null, ``, null, ``, 0.08, `test_measurement_2 - value_field_1\npartition_3: partition_3_value_2, partition_4: partition_4_value_2\nError: 0.08\nValue: 200.2\nExpected: 200.002 +/- 0.2`]\n];"

    val file = new File(location)

    assert(file.exists())
    assert(fileContents == expectedContents)

    if(file.exists) {
      file.delete
    }
  }
}
