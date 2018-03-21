import org.joda.time.LocalDate
import receivers.tmp.{CDRUISshReceiver, NMSUISshXMLReceiver}
import utils.Queries

//import receivers.SshReceiver

/**
  * Created by costantinos on 25/3/2016.
  */
object MTNConnectorDriver {

  def main(args: Array[String]) = {

    if (args.length < 6) {
      println(s"usage: ${this.getClass.getSimpleName} <hostname> <cdr_data_dir> <nms_data_dir> <hostid> <number_of_nodes> <number_of_files>")
      println(s"example: ${this.getClass.getSimpleName} pythia1.mtn.com.cy cdr_data nms_data_dir mix 100")
      sys.exit(-1)
    }

    val hostname = args(0)
    val cdr_dir_path = args(1)
    val nms_dir_path = args(2)
    val hostid = Queries.toInt(args(3)).getOrElse(0)
    val number_of_nodes = Queries.toInt(args(4)).getOrElse(4)
    val number = args(5)


    /*
     * Create the UI DB
     */
    var sql = Queries.CREATE_UI_DB
    Queries.executeDDLSQLInHive(sql)

    sql = Queries.CREATE_CDRS
    Queries.executeDDLSQLInHive(sql)

    //Put the cdr files into a temp table
    val ssh = new CDRUISshReceiver(hostname, cdr_dir_path, hostid, number_of_nodes, number)
    ssh.start()



    sql = Queries.CREATE_NMS
    Queries.executeDDLSQLInHive(sql)
    //Put the nms files into a temp table
    //test commit
    val nssh = new NMSUISshXMLReceiver(hostname, nms_dir_path, hostid, number_of_nodes, number)
    nssh.start()

    println(sys.exit(1))
  }

  def dayIterator(start: LocalDate, end: LocalDate) = Iterator.iterate(start)(_ plusDays 1) takeWhile (_ isBefore end)


}
