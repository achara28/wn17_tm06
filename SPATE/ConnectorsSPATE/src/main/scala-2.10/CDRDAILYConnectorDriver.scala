import java.time.YearMonth

import org.joda.time.{Days, Period, ReadablePeriod}
import receivers.NMSSshXMLReceiver
import utils.Queries

//import receivers.SshReceiver

/**
  * Created by costantinos on 25/3/2016.
  */
object CDRDAILYConnectorDriver {

  def main(args: Array[String]) = {

    if (args.length < 3) {
      println(s"usage: ${this.getClass.getSimpleName} <year> <month> <day>")
      println(s"example: ${this.getClass.getSimpleName} 2016 01 09")
      sys.exit(-1)
    }

    val year = args(0)
    val month = args(1)
    val day = args(2)
    val date = year + month + day

    if (date.length != 8) {
      println(s"usage: ${this.getClass.getSimpleName} <year> <month> <day>")
      println(s"example: ${this.getClass.getSimpleName} 2016 01 09")
      sys.exit(-1)
    }


    var sql = Queries.dropDailyCDRS(date)
    Queries.executeDDLSQLInHive(sql)
    sql = Queries.createDailyCDRS(date)
    var x=Queries.executeDDLSQLInHive(sql)

  }
}
