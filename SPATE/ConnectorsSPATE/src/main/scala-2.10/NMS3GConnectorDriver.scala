import utils.Queries

//import receivers.SshReceiver

/**
  * Created by costantinos on 25/3/2016.
  */
object NMS3GConnectorDriver {

  def main(args: Array[String]) = {


    var sql = Queries.dropNMS3G()
    Queries.executeDDLSQLInHive(sql)
    sql = Queries.createNMS3G()
    var x=Queries.executeDDLSQLInHive(sql)

  }
}
