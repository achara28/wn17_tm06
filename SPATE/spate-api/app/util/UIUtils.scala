package util

import java.sql.DriverManager
import java.text.SimpleDateFormat
import java.util.Date
import org.json.{CDL, JSONArray}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Random

/**
  * Copyright (c) 2015 Chrysovalantis Anastasiou (canast02) - http://dmsl.cs.ucy.ac.cy
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
  * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
  * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
  * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions
  * of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
  * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
  * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
  * IN THE SOFTWARE.
  *
  */
object UIUtils {

  val SOH = "\\|"

  def chekIfTableExistInHive(table: String): Boolean

  = {
    val driverName = "org.apache.hive.jdbc.HiveDriver"
    val sql = "show tables like '" + table + "'"
    try {
      Class.forName(driverName)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    //val con = DriverManager.getConnection("jdbc:hive2://192.168.14.30:10000/default")
    val con = DriverManager.getConnection(connectionstring)
    val stmt = con.createStatement()
    System.out.println("Running: " + sql)
    val res = stmt.executeQuery(sql)
    val ret = res.next();
    if (ret) {
      System.out.println(res.getString(1))
    }
    stmt.close()
    con.close()
    ret
  }


  /**
    * Generates a human readable string for the given duration
    *
    * @param milliseconds
    * @return
    */

  val basename = AppVariables.getConfig.getString("pythia.basename").getOrElse("pythia1")
  val connectionstring = "jdbc:hive2://" + basename + ":10000/default"
  val hdsfconnectionstring = "hdfs://" + basename + ":54310"

  def formatDuration(milliseconds: Long): String = {

    if (milliseconds <= 0) return "--"

    if (milliseconds < 100) {
      return "%d ms".format(milliseconds)
    }
    val seconds = milliseconds.toDouble / 1000
    if (seconds < 1) {
      return "%.1f s".format(seconds)
    }
    if (seconds < 60) {
      return "%.0f s".format(seconds)
    }
    val minutes = seconds / 60
    if (minutes < 60) {
      val secs = seconds % 60
      if (secs > 0) {
        return "%.0f min %.0f s".format(minutes, secs)
      }
      else {
        return "%.0f min".format(minutes)
      }
    }
    val hours = minutes / 60

    if (hours < 24) {
      val mins = minutes % 60

      if (mins == 0) {
        return "%.0f h".format(hours)
      }
      else {
        return "%.0f h %.0f m".format(hours, mins)
      }
    }

    val days = hours / 24
    val h = hours % 24
    val mins = minutes % 60

    if (mins == 0) {
      "%.0f d %.0f h".format(days, h)
    }
    else {
      "%.0f d %.0f h %.0f m".format(days, h, mins)
    }
  }


  def formatDate(milliseconds: Long): String = {
    val formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm")

    val date = new Date(milliseconds)

    formatter.format(date)
  }

  def selectFromHive(sql: String): JSONArray = {
    val driverName = "org.apache.hive.jdbc.HiveDriver"

    try {
      Class.forName(driverName)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    //val con = DriverManager.getConnection("jdbc:hive2://192.168.14.30:10000/default")
    val con = DriverManager.getConnection(connectionstring)
    val stmt = con.createStatement()
    System.out.println("Running: " + sql)
    val res = stmt.executeQuery(sql)
    if (res.next()) {
      System.out.println(res.getString(1))
    }
    val ret = ResultSetConverter.convert(res)
    stmt.close()
    con.close()
    ret
  }

  def selectCoverageFromHive(sql: String): JSONArray = {
    val driverName = "org.apache.hive.jdbc.HiveDriver"

    try {
      Class.forName(driverName)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    //val con = DriverManager.getConnection("jdbc:hive2://192.168.14.30:10000/default")
    val con = DriverManager.getConnection(connectionstring)
    val stmt = con.createStatement()
    System.out.println("Running: " + sql)
    val res = stmt.executeQuery(sql)
    if (res.next()) {
      System.out.println(res.getString(1))
    }
    val ret = ResultSetConverter.convertCoverageToArray(res)
    stmt.close()
    con.close()
    ret
  }

  def selectFromHDFSOLD(lines: Iterator[String], limit: Int): JSONArray = {
    val linesJson = new JSONArray()
    val rnd = new Random()

    val map = mutable.HashMap[String, ListBuffer[Double]]()
    for (line <- lines) {
      //      val rand = rnd.nextInt(10) //generate 10 numbers: 10% chance
      //      if (rand == 0) {
      val splits = line.split(",")
      val curv = toDouble(splits(2)).getOrElse(0.0)
      // if signal is great than -100
      /*
       * RSSI Signal Strength
       * > -70 dBm Excellent
       * -70 dBm to -85 dBm Good
       * -86 dBm to -100 dBm Poor
       */
      if (curv > limit) {
        val curx = toDouble(splits(0)).getOrElse(0.0)
        val cury = toDouble(splits(1)).getOrElse(0.0)


        var x: Double = (curx * 1000).toInt
        var y: Double = (cury * 1000).toInt
        x /= 1000
        y /= 1000

        if (!map.contains(x + "," + y)) {
          map.put(x + "," + y, new ListBuffer[Double])
        }
        val list = map.get(x + "," + y).get
        list += curv
      }


    }

    for (entry <- map) {
      val jo = new JSONArray()
      val splits = entry._1.split(",")
      jo.put(toDouble(splits(0)).getOrElse(0.0))
      jo.put(toDouble(splits(1)).getOrElse(0.0))
      var avgv = 0.0
      for (x <- entry._2.toList)
        avgv += x
      avgv /= entry._2.size
      jo.put(avgv)
      linesJson.put(jo)
    }


    //    }
    linesJson
  }


  def selectFromHDFS(lines: Iterator[String], limit: Int): JSONArray = {
    val linesJson = new JSONArray()
    for (line <- lines) {
      //      val rand = rnd.nextInt(10) //generate 10 numbers: 10% chance
      //      if (rand == 0) {
      val splits = line.split(",")
      val jo = new JSONArray()

      jo.put(toDouble(splits(0)).getOrElse(0.0))
      jo.put(toDouble(splits(1)).getOrElse(0.0))
      jo.put(toDouble(splits(2)).getOrElse(0.0))
      linesJson.put(jo)
    }
    //    }
    linesJson
  }

  //32.7343822 and the longitude is: -117.14412270000003.
  //     is 35.185566, and the longitude is 33.382275.

  def selectCoverageFromHDFS(iters_lines: List[Iterator[String]], kind: String): JSONArray = {
    val linesJson = new JSONArray()
    for (lines <- iters_lines)
      for (line <- lines) {
        //      val rand = rnd.nextInt(10) //generate 10 numbers: 10% chance
        //      if (rand == 0) {
        val splits = line.split("\\|")
        if (splits(3).equalsIgnoreCase(kind)) {


          val jo = new JSONArray()

          jo.put(toDouble(splits(0)).getOrElse(0.0))
          jo.put(toDouble(splits(1)).getOrElse(0.0))
          jo.put(toDouble(splits(2)).getOrElse(0.0))
          linesJson.put(jo)
        }
      }
    //    }
    linesJson
  }
  def selectCellsFromHDFS(iters_lines: List[Iterator[String]], kind: String): JSONArray = {
    val linesJson = new JSONArray()
    for (lines <- iters_lines)
      for (line <- lines) {
        //      val rand = rnd.nextInt(10) //generate 10 numbers: 10% chance
        //      if (rand == 0) {
        val splits = line.split("\\|")
        if (kind.isEmpty || splits(9).equalsIgnoreCase(kind)) {
          val jo = new JSONArray()
          jo.put(splits(0))
          jo.put(splits(1))
          jo.put(splits(2))
          jo.put(toInt(splits(3)).getOrElse(0.0))
          jo.put(toInt(splits(4)).getOrElse(0.0))
          jo.put(toInt(splits(5)).getOrElse(0.0))
          jo.put(toDouble(splits(6)).getOrElse(0.0))
          jo.put(toDouble(splits(7)).getOrElse(0.0))
          jo.put(splits(8))
          jo.put(splits(9))
          linesJson.put(jo)
        }
      }
    //    }
    linesJson
  }

  def selectCDRSFromHDFS(iters_lines: List[Iterator[String]], start: String, end: String, callingnumber: String, partynumber: String): JSONArray = {
    val linesJson = new JSONArray()
    for (lines <- iters_lines)
      for (line <- lines) {
        val splits = line.split(SOH)
        val callb = callingnumber.isEmpty || callingnumber.equals(splits(3))
        val partb = partynumber.isEmpty || partynumber.equals(splits(4))
        val startb = start.isEmpty || start.compareToIgnoreCase(splits(2)) <= 0
        val endb = end.isEmpty || end.compareToIgnoreCase(splits(2)) >= 0
        if (callb && partb && startb && endb) {
          val jo = new JSONArray()
          jo.put(toDouble(splits(0)).getOrElse(0.0))
          jo.put(toDouble(splits(1)).getOrElse(0.0))
          jo.put(splits(2))
          jo.put(splits(3))
          jo.put(splits(4))
          jo.put(toInt(splits(5)).getOrElse(0))
          jo.put(toInt(splits(6)).getOrElse(0))
          jo.put(toInt(splits(7)).getOrElse(0))
          linesJson.put(jo)
        }
      }
    //    }
    linesJson
  }

  def selectNMSFromHDFS(iters_lines: List[Iterator[String]], start: String, end: String): JSONArray = {
    val linesJson = new JSONArray()
    for (lines <- iters_lines)
      for (line <- lines) {
        val splits = line.split(SOH)
        val startb = start.isEmpty || start.compareToIgnoreCase(splits(5)) <= 0
        val endb = end.isEmpty || end.compareToIgnoreCase(splits(5)) >= 0
        if (startb && endb) {
          val jo = new JSONArray()
          jo.put(toDouble(splits(0)).getOrElse(0.0))
          jo.put(toDouble(splits(1)).getOrElse(0.0))
          jo.put(splits(2))
          jo.put(splits(3))
          jo.put(toInt(splits(4)).getOrElse(0))
          jo.put(splits(5))
          linesJson.put(jo)
        }
      }
    //    }
    linesJson
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
