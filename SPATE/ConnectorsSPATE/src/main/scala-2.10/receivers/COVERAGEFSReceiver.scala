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
class COVERAGEFSReceiver(filename: String, hostname:String, kind: String) {
  Queries.basename = hostname

  //val temp_dir = "C:\\Users\\costantinos\\Desktop\\MTNConnectors\\tmp"
  var temp_dir = "/pythia_tmp"

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

  def createCOVERAGEHiveFile(line: String,celltype:String): String = {
    val p = line.toString().split(",")
    val b = new StringBuilder()
    b.append(toDouble(p(0).trim).getOrElse(0.0)).append("|")
    b.append(toDouble(p(1).trim).getOrElse(0.0)).append("|")
    b.append(toDouble(p(2).trim).getOrElse(0.0)).append("|")
    b.append(celltype).append("\n")
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
        writer.write(createCOVERAGEHiveFile(line,kind))
      }
      }
      writer.close()

    }
    catch {
      case e: IOException => {
        sys.error(e.getLocalizedMessage)
      }
    }

    val cmd = "/usr/local/hadoop/bin/hadoop fs -put " + hive_filename + " " + hive_filename

    println(cmd)
    val exitCode = cmd !!

    println(exitCode)

    Queries.executeDDLSQLInHive(Queries.CREATE_COVERAGE)

    Queries.insertIntoTableInHive(Queries.COVERAGE_TABLE, hive_filename,"")

    //local delete
    deleteFile(hive_filename)
   // deleteHDFSFile(hive_filename)
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
