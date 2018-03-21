import java.net.URI

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.joda.time.LocalDate
import receivers.tmp.{CDRUISshReceiver, NMSUISshReceiver}
import utils.Queries

import scala.collection.immutable.HashSet
import scala.collection.mutable
import scala.io.Source

//import receivers.SshReceiver

/**
  * Created by costantinos on 25/3/2016.
  */
object StatisticsDriver {
  var stats = scala.collection.mutable.MutableList[scala.collection.mutable.HashSet[String]]()

  def main(args: Array[String]) = {
    statsForTable("cells")
    statsForTable("cdrs")
    statsForTable("nms")
  }

  def statsForTable(table: String) {
    val conf = new Configuration()
    conf.addResource("/data/core-site.xml")
    conf.addResource("/data/hdfs-site.xml")
    val fileSystem = FileSystem.get(new URI(Queries.hdsfconnectionstring), conf)
    val filename = "/user/hive/warehouse/" + table
    val path: Path = new Path(filename)
    val status = fileSystem.listStatus(path)
    var cnt = 0
    status.foreach(s => {

      val is = fileSystem.open(s.getPath)
      for (line <- Source.fromInputStream(is).getLines) {
        val splits = line.split("\\|")

        if (stats.isEmpty)
          for (i <- 0 until splits.length - 1)
            stats += scala.collection.mutable.HashSet[String]()
        for (i <- 0 until splits.length - 1) {
          stats(i) += splits(i)
        }
        cnt += 1
      }
    })
    stats.foreach(col => {
      print(col.size + ",")
    })
    print(cnt)
    println()
    stats.clear()
  }

}
