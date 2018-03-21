import java.time.YearMonth

import utils.Queries

//import receivers.SshReceiver

/**
  * Created by costantinos on 25/3/2016.
  */
object CDRMONTHLYConnectorDriver {

  def main(args: Array[String]) = {

    if (args.length < 2) {
      println(s"usage: ${this.getClass.getSimpleName} <year> <month>")
      println(s"example: ${this.getClass.getSimpleName} 2016 01")
      sys.exit(-1)
    }

    val year = args(0)
    val month = args(1)
    val date = year + month
    if (date.length != 6) {
      println(s"usage: ${this.getClass.getSimpleName} <year> <month>")
      println(s"example: ${this.getClass.getSimpleName} 2016 01")
      sys.exit(-1)
    }

    val yearMonthObject = YearMonth.of(year.toInt, month.toInt)
    val daysInMonth = yearMonthObject.lengthOfMonth()
    for (f <- 1 to daysInMonth) {
      var sql = Queries.dropDailyCDRS(date + "%02d".format(f))
      Queries.executeDDLSQLInHive(sql)
      sql = Queries.createDailyCDRS(date + "%02d".format(f))
      Queries.executeDDLSQLInHive(sql)
    }
  }
}
