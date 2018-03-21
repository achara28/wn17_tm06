import utils.Queries

//import receivers.SshReceiver

/**
  * Created by costantinos on 25/3/2016.
  */
object NMS2GConnectorDriver {

  def main(args: Array[String]) = {


    var sql = Queries.dropNMS2G()
    Queries.executeDDLSQLInHive(sql)
    sql = Queries.createNMS2G()
    var x=Queries.executeDDLSQLInHive(sql)

  }
}
