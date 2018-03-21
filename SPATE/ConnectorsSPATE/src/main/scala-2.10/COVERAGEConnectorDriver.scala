import receivers.COVERAGEFSReceiver

//import receivers.SshReceiver

/**
  * Created by costantinos on 25/3/2016.
  */
object COVERAGEConnectorDriver {

  def main(args: Array[String]) = {

    if (args.length < 3) {
      println(s"usage: ${this.getClass.getSimpleName} <filename on hdfs> <hostname>  <kind>")
      println(s"example: ${this.getClass.getSimpleName} hdfs://pythia1:54310/pythia/data/2g.heat pythia1 2G")
      sys.exit(-1)
    }
    val hostname = args(1)
    val filename = args(0)
    val kind = args(2)
    val ssh = new COVERAGEFSReceiver(filename,hostname, kind)
    ssh.start()
  }
}
