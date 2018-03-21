package receivers

import java.io.{File, IOException, PrintWriter}
import java.net.URI

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import utils.Queries

import scala.io.Source
import scala.sys.process._

/**
  * Created by costantinos on 28/3/2016.
  */
class CELLSFSReceiver(filename: String, hostname:String, kind: String) {
  Queries.basename = hostname

  //val temp_dir = "C:\\Users\\costantinos\\Desktop\\MTNConnectors\\tmp"
  var temp_dir ="/pythia_tmp"

  var FILE_DIVIDER = "/"
  val os = System.getProperty("os.name")


  if (os.contains("Windows")) {
    temp_dir = "C:\\Users\\costantinos\\Desktop\\MTNConnectors\\tmp"
    FILE_DIVIDER = "\\"
  }

  def start() {
    download()
  }

  //
  def stop() {
    // There is nothing much to do as the thread calling receive()
    // is designed to stop by itself isStopped() returns false
  }

  private def download() {

    println("Connecting to server with ssh ...")
    parseAndStore(filename, kind)
    //parseAndStore(temp_dir + "/" + kind + "/" + filename, kind)


    println("Disconnecting from server  ...")
    sys.exit(1)
  }
  /*
  *
  *
  *
   */

  val mlat = 0.0
  val mlon = 0.0

  def createCELLSHiveFile(line: String): String = {
    val p = line.toString().split(",")
    val b = new StringBuilder()
    b.append(Queries.md5withEncryption(p(0).trim)).append("|")
    b.append(p(1).trim).append("|")
    b.append(Queries.md5withEncryption(p(2).trim)).append("|")
    b.append(p(3).trim).append("|")
    b.append(p(4).trim).append("|")
    b.append(p(5).trim).append("|")
    b.append(toDouble(p(6)).getOrElse(0.0) + mlat).append("|")
    b.append(toDouble(p(7)).getOrElse(0.0) + mlon).append("|")
    b.append(p(8).trim).append("|")
    b.append(p(9).trim).append("\n")
    b.toString()
  }

  private def parseAndStore(filename: String, kind: String): Unit = {

    val conf = new Configuration()
    conf.addResource("/usr/local/hadoop/etc/hadoop/core-site.xml")
    conf.addResource("/usr/local/hadoop/etc/hadoop/hdfs-site.xml")
    val fileSystem = FileSystem.get(new URI(Queries.hdsfconnectionstring), conf)
    val path: Path = new Path(filename)
    val is = fileSystem.open(path)
    val fileContents = Source.fromInputStream(is).getLines

    val hive_filename = temp_dir + FILE_DIVIDER + "hive_" + kind + "_" + System.currentTimeMillis() + ".csv"
    val writer = new PrintWriter(new File(hive_filename))

    try {
      fileContents.foreach { line => {
        writer.write(createCELLSHiveFile(line))
      }
      }
      writer.close()

    }
    catch {
      case e: IOException => {
        sys.error(e.getLocalizedMessage)
      }
    }
    var cmd = "gzip " + hive_filename
    println(cmd)
    var exitCode = cmd !

    cmd = "/usr/local/hadoop/bin/hadoop fs -put " + hive_filename+ ".gz" + " " + hive_filename+ ".gz"

    println(cmd)
    exitCode = cmd !

    println(exitCode)

    Queries.executeDDLSQLInHive(Queries.CREATE_CELLS)

    Queries.insertIntoTableInHive(Queries.CELLS_TABLE, hive_filename+ ".gz","")

    //local delete
    deleteFile(hive_filename)
  //  deleteHDFSFile(hive_filename)
  }

  def deleteHDFSFile(filename: String) = {
    val cmd = "/usr/local/hadoop/bin/hadoop fs -rm " + filename

    println(cmd)
    val exitCode = cmd !!

    println(exitCode)
  }

  def deleteFile(filename: String) = {
    new File(filename).delete()
  }

  def toDouble(s: String): Option[Double] = {
    try {
      Some(s.toDouble)
    } catch {
      case e: Exception => None
    }
  }

  def toInt(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case e: Exception => None
    }
  }
}
