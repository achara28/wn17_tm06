package receivers

import java.io.{File, IOException, PrintWriter}

import com.decodified.scalassh.{HostResourceConfig, SSH}
import connectors.NMSConnector
import utils.Queries

import scala.collection.mutable
import scala.io.Source
import sys.process._

/**
  * Created by canast02 on 7/7/15.
  */
class NMSSshXMLReceiver(hostname: String, dir_path: String, kind: String) {
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
    try {
      /**
        * Connect via ssh to get the data
        */
      println("Connecting to server with ssh ...")

      SSH(hostname, HostResourceConfig()) { client =>


        var fileList = new scala.collection.mutable.MutableList[String]()

        println("Getting the file list ... ")
        client.exec(s"ls -1A $dir_path | head -1000").right.map { result =>
          result.stdOutAsString().split("\n").foreach(file => {
            if (!file.trim.isEmpty)
              fileList += file
          })
        }

        //Remove the last file that is incomplete
        if (fileList.nonEmpty)
          fileList = fileList.take(fileList.length)

        println("Downloading the file list ... ")
        fileList.foreach(filename => {
          client.download(dir_path + "/" + filename, temp_dir + FILE_DIVIDER + kind + "_" + filename)
          parseAndStore(temp_dir + FILE_DIVIDER + kind + "_" + filename, kind)
          //parseAndStore(temp_dir + "/" + kind + "/" + filename, kind)
          val rm_cmd = s"rm '$dir_path/$filename'";
          client.exec(rm_cmd)
        })

        fileList.clear()
      }

    } catch {
      case t: Throwable =>
        // restart if there is any other error
        t.printStackTrace()
    }

    println("Disconnecting from server  ...")
    sys.exit(1)
  }

  private def parseAndStore(filename: String, kind: String): Unit = {

    val hive_filename = temp_dir + FILE_DIVIDER + "hive_" + kind + "_" + System.currentTimeMillis() + ".csv"
    val writer = new PrintWriter(new File(hive_filename))

    try {
      NMSConnector.parse(filename).toHiveRecords.foreach(writer.write)
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

    Queries.executeDDLSQLInHive(Queries.CREATE_NMS)

    Queries.insertIntoTableInHive(Queries.NMS_TABLE, hive_filename,"")

    //local delete
    deleteFile(filename)
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
