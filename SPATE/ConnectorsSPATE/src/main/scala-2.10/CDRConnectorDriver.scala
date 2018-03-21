import receivers.CDRSshReceiver

//import receivers.SshReceiver

/**
  * Created by costantinos on 25/3/2016.
  */
object CDRConnectorDriver {

  def main(args: Array[String]) = {

    if (args.length < 3) {
      println(s"usage: ${this.getClass.getSimpleName} <hostname> <data_dir> <kind>")
      println(s"example: ${this.getClass.getSimpleName} pythia1.mtn.com.cy cdr_data cdrs")
      sys.exit(-1)
    }

    val hostname = args(0)
    val dir_path = args(1)
    val kind = args(2)
    val ssh = new CDRSshReceiver(hostname, dir_path, kind)
    ssh.start()
  }
}
