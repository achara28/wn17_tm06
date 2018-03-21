import java.sql.Timestamp
import java.text.SimpleDateFormat

/**
  * Created by costantinos on 19/5/2016.
  */
object TestingDates {
  def main(args: Array[String]) = {

    var beginTime="2016-01-15T13:00:00+02:00"
    var endTime="2016-01-02T20:00:00+02:00"
    val sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    val sdfDate = new SimpleDateFormat("yyyyMMddHHmm")
    // val bd = new Date(sdf.parse(beginTime).getTime)
    val bts = new Timestamp(sdf.parse(beginTime).getTime)
    //     val ed = new Date(sdf.parse(endTime).getTime)
    val ets = new Timestamp(sdf.parse(endTime).getTime)
    //20150610235951
    beginTime = sdfDate.format(bts)
    endTime = sdfDate.format(ets)
    println(beginTime)
    println(endTime)
  }
}
