package receivers

import java.io.{File, IOException, PrintWriter}

import com.decodified.scalassh.{HostResourceConfig, SSH}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import utils.Queries

import scala.io.Source
import sys.process._

/**
  * Created by canast02 on 7/7/15.
  */
class CDRSshReceiver(hostname: String, dir_path: String, kind: String) {

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

  def createCDRHiveFile(line: String): String = {
    val p = line.toString().split("\\|")
    val b = new StringBuilder()
    b.append(toInt(p(0).trim).getOrElse(0)).append("|")
    b.append(toInt(p(1).trim).getOrElse(0)).append("|")
    b.append(p(2).trim).append("|")
    b.append(toInt(p(3).trim).getOrElse(0)).append("|")
    b.append(p(4).trim).append("|")
    b.append(p(5).trim).append("|")
    b.append(p(6).trim).append("|")
    b.append(p(7).trim).append("|")
    b.append(p(8).trim).append("|")
    b.append(p(9).trim).append("|")
    b.append(p(10).trim).append("|")
    b.append(p(11).trim).append("|")
    b.append(p(12).trim).append("|")
    b.append(toInt(p(13).trim).getOrElse(0)).append("|")
    b.append(p(14).trim).append("|")
    b.append(p(15).trim).append("|")
    b.append(toInt(p(16).trim).getOrElse(0)).append("|")
    b.append(toInt(p(17).trim).getOrElse(0)).append("|")
    b.append(toInt(p(18).trim).getOrElse(0)).append("|")
    b.append(toInt(p(19).trim).getOrElse(0)).append("|")
    b.append(toInt(p(20).trim).getOrElse(0)).append("|")
    b.append(p(21).trim).append("|")
    b.append(p(22).trim).append("|")
    b.append(p(23).trim).append("|")
    b.append(p(24).trim).append("|")
    b.append(p(25).trim).append("|")
    b.append(p(26).trim).append("|")
    b.append(p(27).trim).append("|")
    b.append(p(28).trim).append("|")
    b.append(p(29).trim).append("|")
    b.append(toInt(p(30).trim).getOrElse(0)).append("|")
    b.append(toInt(p(31).trim).getOrElse(0)).append("|")
    b.append(p(32).trim).append("|")
    b.append(p(33).trim).append("|")
    b.append(p(34).trim).append("|")
    b.append(p(35).trim).append("|")
    b.append(toInt(p(36).trim).getOrElse(0)).append("|")
    b.append(p(37).trim).append("|")
    b.append(p(38).trim.toInt).append("|")
    b.append(toInt(p(39).trim).getOrElse(0)).append("|")
    b.append(toInt(p(40).trim).getOrElse(0)).append("|")
    b.append(toInt(p(41).trim).getOrElse(0)).append("|")
    b.append(toInt(p(42).trim).getOrElse(0)).append("|")
    b.append(toInt(p(43).trim).getOrElse(0)).append("|")
    b.append(p(44).trim).append("|")
    b.append(p(45).trim).append("|")
    b.append(p(46).trim).append("|")
    b.append(p(47).trim).append("|")
    b.append(p(48).trim).append("|")
    b.append(p(49).trim).append("|")
    b.append(p(50).trim).append("|")
    b.append(p(51).trim).append("|")
    b.append(p(52).trim).append("|")
    b.append(toInt(p(53).trim).getOrElse(0)).append("|")
    b.append(p(54).trim).append("|")
    b.append(p(55).trim).append("|")
    b.append(toInt(p(56).trim).getOrElse(0)).append("|")
    b.append(toInt(p(57).trim).getOrElse(0)).append("|")
    b.append(p(58).trim).append("|")
    b.append(p(59).trim).append("|")
    b.append(toInt(p(60).trim).getOrElse(0)).append("|")
    b.append(toInt(p(61).trim).getOrElse(0)).append("|")
    b.append(toInt(p(62).trim).getOrElse(0)).append("|")
    b.append(toInt(p(63).trim).getOrElse(0)).append("|")
    b.append(toInt(p(64).trim).getOrElse(0)).append("|")
    b.append(toInt(p(65).trim).getOrElse(0)).append("|")
    b.append(toInt(p(66).trim).getOrElse(0)).append("|")
    b.append(p(67).trim).append("|")
    b.append(toInt(p(68).trim).getOrElse(0)).append("|")
    b.append(toInt(p(69).trim).getOrElse(0)).append("|")
    b.append(toInt(p(70).trim).getOrElse(0)).append("|")
    b.append(toInt(p(71).trim).getOrElse(0)).append("|")
    b.append(toInt(p(72).trim).getOrElse(0)).append("|")
    b.append(toInt(p(73).trim).getOrElse(0)).append("|")
    b.append(toInt(p(74).trim).getOrElse(0)).append("|")
    b.append(toInt(p(75).trim).getOrElse(0)).append("|")
    b.append(toInt(p(76).trim).getOrElse(0)).append("|")
    b.append(toInt(p(77).trim).getOrElse(0)).append("|")
    b.append(toInt(p(78).trim).getOrElse(0)).append("|")
    b.append(toInt(p(79).trim).getOrElse(0)).append("|")
    b.append(toInt(p(80).trim).getOrElse(0)).append("|")
    b.append(toInt(p(81).trim).getOrElse(0)).append("|")
    b.append(toInt(p(82).trim).getOrElse(0)).append("|")
    b.append(toInt(p(83).trim).getOrElse(0)).append("|")
    b.append(toInt(p(84).trim).getOrElse(0)).append("|")
    b.append(toInt(p(85).trim).getOrElse(0)).append("|")
    b.append(toInt(p(86).trim).getOrElse(0)).append("|")
    b.append(toInt(p(87).trim).getOrElse(0)).append("|")
    b.append(toInt(p(88).trim).getOrElse(0)).append("|")
    b.append(toInt(p(89).trim).getOrElse(0)).append("|")
    b.append(toInt(p(90).trim).getOrElse(0)).append("|")
    b.append(toInt(p(91).trim).getOrElse(0)).append("|")
    b.append(toInt(p(92).trim).getOrElse(0)).append("|")
    b.append(toInt(p(93).trim).getOrElse(0)).append("|")
    b.append(toInt(p(94).trim).getOrElse(0)).append("|")
    b.append(toInt(p(95).trim).getOrElse(0)).append("|")
    b.append(toInt(p(96).trim).getOrElse(0)).append("|")
    b.append(toInt(p(97).trim).getOrElse(0)).append("|")
    b.append(toInt(p(98).trim).getOrElse(0)).append("|")
    b.append(toInt(p(99).trim).getOrElse(0)).append("|")
    b.append(toInt(p(100).trim).getOrElse(0)).append("|")
    b.append(toInt(p(101).trim).getOrElse(0)).append("|")
    b.append(toInt(p(102).trim).getOrElse(0)).append("|")
    b.append(toInt(p(103).trim).getOrElse(0)).append("|")
    b.append(toInt(p(104).trim).getOrElse(0)).append("|")
    b.append(toInt(p(105).trim).getOrElse(0)).append("|")
    b.append(toInt(p(106).trim).getOrElse(0)).append("|")
    b.append(toInt(p(107).trim).getOrElse(0)).append("|")
    b.append(toInt(p(108).trim).getOrElse(0)).append("|")
    b.append(toInt(p(109).trim).getOrElse(0)).append("|")
    b.append(p(110).trim).append("|")
    b.append(p(111).trim).append("|")
    b.append(p(112).trim).append("|")
    b.append(p(113).trim).append("|")
    b.append(p(114).trim).append("|")
    b.append(p(115).trim).append("|")
    b.append(p(116).trim).append("|")
    b.append(p(117).trim).append("|")
    b.append(p(118).trim).append("|")
    b.append(p(119).trim).append("|")
    b.append(toInt(p(120).trim).getOrElse(0)).append("|")
    b.append(p(121).trim).append("|")
    b.append(p(122).trim).append("|")
    b.append(p(123).trim).append("|")
    b.append(p(124).trim).append("|")
    b.append(toInt(p(125).trim).getOrElse(0)).append("|")
    b.append(p(126).trim).append("|")
    b.append(p(127).trim).append("|")
    b.append(toInt(p(128).trim).getOrElse(0)).append("|")
    b.append(p(129).trim).append("|")
    b.append(toInt(p(130).trim).getOrElse(0)).append("|")
    b.append(p(131).trim).append("|")
    b.append(p(132).trim).append("|")
    b.append(p(133).trim).append("|")
    b.append(p(134).trim).append("|")
    b.append(p(135).trim).append("|")
    b.append(p(136).trim).append("|")
    b.append(p(137).trim).append("|")
    b.append(p(138).trim).append("|")
    b.append(p(139).trim).append("|")
    b.append(p(140).trim).append("|")
    b.append(p(141).trim).append("|")
    b.append(p(142).trim).append("|")
    b.append(p(143).trim).append("|")
    b.append(p(144).trim).append("|")
    b.append(p(145).trim).append("|")
    b.append(p(146).trim).append("|")
    b.append(p(147).trim).append("|")
    b.append(p(148).trim).append("|")
    b.append(p(149).trim).append("|")
    b.append(p(150).trim).append("|")
    b.append(toInt(p(151).trim).getOrElse(0)).append("|")
    b.append(toInt(p(152).trim).getOrElse(0)).append("|")
    b.append(toInt(p(153).trim).getOrElse(0)).append("|")
    b.append(toInt(p(154).trim).getOrElse(0)).append("|")
    b.append(toInt(p(155).trim).getOrElse(0)).append("|")
    b.append(toInt(p(156).trim).getOrElse(0)).append("|")
    b.append(toInt(p(157).trim).getOrElse(0)).append("|")
    b.append(p(158).trim).append("|")
    b.append(toInt(p(159).trim).getOrElse(0)).append("|")
    b.append(toInt(p(160).trim).getOrElse(0)).append("|")
    b.append(toInt(p(161).trim).getOrElse(0)).append("|")
    b.append(toInt(p(162).trim).getOrElse(0)).append("|")
    b.append(toInt(p(163).trim).getOrElse(0)).append("|")
    b.append(toInt(p(164).trim).getOrElse(0)).append("|")
    b.append(toInt(p(165).trim).getOrElse(0)).append("|")
    b.append(toInt(p(166).trim).getOrElse(0)).append("|")
    b.append(toInt(p(167).trim).getOrElse(0)).append("|")
    b.append(toInt(p(168).trim).getOrElse(0)).append("|")
    b.append(toInt(p(169).trim).getOrElse(0)).append("|")
    b.append(toInt(p(170).trim).getOrElse(0)).append("|")
    b.append(toInt(p(171).trim).getOrElse(0)).append("|")
    b.append(toInt(p(172).trim).getOrElse(0)).append("|")
    b.append(toInt(p(173).trim).getOrElse(0)).append("|")
    b.append(toInt(p(174).trim).getOrElse(0)).append("|")
    b.append(toInt(p(175).trim).getOrElse(0)).append("|")
    b.append(toInt(p(176).trim).getOrElse(0)).append("|")
    b.append(toInt(p(177).trim).getOrElse(0)).append("|")
    b.append(toInt(p(178).trim).getOrElse(0)).append("|")
    b.append(toInt(p(179).trim).getOrElse(0)).append("|")
    b.append(toInt(p(180).trim).getOrElse(0)).append("|")
    b.append(p(181).trim).append("|")
    b.append(p(182).trim).append("|")
    b.append(p(183).trim).append("|")
    b.append(p(184).trim).append("|")
    b.append(p(185).trim).append("|")
    b.append(p(186).trim).append("|")
    b.append(toInt(p(187).trim).getOrElse(0)).append("|")
    b.append(toInt(p(188).trim).getOrElse(0)).append("|")
    b.append(toInt(p(189).trim).getOrElse(0)).append("|")
    b.append(p(190).trim).append("|")
    b.append(toInt(p(191).trim).getOrElse(0)).append("|")
    b.append(p(192).trim).append("|")
    b.append(toInt(p(193).trim).getOrElse(0)).append("\n")
    b.toString()
  }

  private def parseAndStore(filename: String, kind: String): Unit = {
    val source = Source
      .fromFile(filename)
    val hive_filename = temp_dir + FILE_DIVIDER + "hive_" + kind + "_" + System.currentTimeMillis() + ".csv"
    val writer = new PrintWriter(new File(hive_filename))

    try {
      source.getLines
        .foreach { line => {
          writer.write(createCDRHiveFile(line))
        }
        }
      writer.close()
      source.close

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

    Queries.executeDDLSQLInHive(Queries.CREATE_CDRS)

    val partition=""
    Queries.insertIntoTableInHive(Queries.CDRS_TABLE, hive_filename, partition)


    //local delete
    deleteFile(filename)
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
