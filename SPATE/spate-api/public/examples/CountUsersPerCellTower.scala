package pythia.examples.streaming

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import pythia.examples.utils.LogLevels
import pythia.logging.PythiaLogging
import pythia.{PythiaConf, PythiaContext}

/**
 * Copyright (c) 2015 Chrysovalantis Anastasiou (canast02) - http://dmsl.cs.ucy.ac.cy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions
 *  of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 */
object CountUsersPerCellTower extends PythiaLogging {

    case class CellUserCount(cellId: Long, count: Int) extends Serializable

    def main(args: Array[String]) {

        if (args.length < 2) {
            println("usage: " + this.getClass.getSimpleName + " <cdrHost> <cdrPort> [<window>]")
            sys.exit(1)
        }

        LogLevels.setStreamingLogLevels()

        val conf = new PythiaConf()

        conf.setAppName("Users per cell tower")
        if(conf.getPythiaMaster == null)
            conf.setPythiaMaster("pythia://10.16.20.6:4444")
//            conf.setPythiaMaster("pythia://chrysfunifi.in.cs.ucy.ac.cy:4444")
        if(conf.get("spark.master","").isEmpty)
            conf.setMaster("local[4]")

        conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
        conf.registerKryoClasses(Seq(classOf[CellUserCount]).toArray)

        val cdrHost = args(0)
        val cdrPort = args(1).toInt
        val winDuration = if (args.length == 3) {
            args(2).toInt
        } else {
            10
        }

        val pc = new PythiaContext(conf, pythia.SHARED_USER_API_KEY)

        val ssc = new StreamingContext(pc.sparkContext, Seconds(winDuration))

        val recordStrings = ssc.socketTextStream(cdrHost, cdrPort, StorageLevel.MEMORY_ONLY)
        val records = recordStrings.map(line => line.split("\\|"))
        val usersPerCell = records.map(rec => (rec(11), 1)).reduceByKey((a, b) => a + b)
        val results = usersPerCell.map(tuple => CellUserCount(tuple._1.toLong, tuple._2))

        results.foreachRDD(rdd => {
            if(rdd.count() > 0) {
                pc.persist(Map(
                    "points" -> rdd.collect(),
                    "uiCategory" -> "map"
                ))
            }
        })

        sys.addShutdownHook {
            if (ssc != null)
                ssc.stop(stopSparkContext = false, stopGracefully = true)
            if(pc != null)
                pc.stop()
        }

        ssc.start()
        ssc.awaitTermination()
    }
}