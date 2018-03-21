package controllers

import java.io.BufferedInputStream
import java.net.URI
import java.util.zip.GZIPInputStream

import _root_.util.{JsonReply, UIUtils}
import _root_.util.actions.AuthenticatedJsonAction
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}
import org.json.JSONArray
import play.api.mvc.Controller
import util.UIUtils.toDouble

import scala.collection.Iterator
import scala.io.Source
import scala.sys.process._

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
object VisaulizationController extends Controller {

  /**
    * USING HIVE
    *
    */

 def telco() = AuthenticatedJsonAction { request =>
   try {
     //run telco python script
     var cmd = "python3 telco_mdm.py"

     var result = cmd !!

     var splits = result.split(" ")

     val jo = new JSONArray()
     Ok(JsonReply.replyWithJSON("success", jo))
   } catch {
     case e: Exception => {
       e.printStackTrace()
       Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
     }
   }

 }

  def listCellTowers() = AuthenticatedJsonAction {
    // select cells from table
    val tableName = "cells"
    val sql = "select * from " + tableName
    Ok(JsonReply.replyWithJSON("success", UIUtils.selectFromHive(sql)))
  }

  def getCoverage4g() = AuthenticatedJsonAction {
    // select cells from table
    val tableName = "coverage"
    val sql = "select latitude as lat,longitude as lon, value as v from " + tableName + " where celltype=\"4G\""
    Ok(JsonReply.replyWithJSON("success", UIUtils.selectCoverageFromHive(sql)))
  }

  def getCoverage3g() = AuthenticatedJsonAction {
    // select cells from table
    val tableName = "coverage"
    val sql = "select latitude as lat,longitude as lon, value as v from " + tableName + " where celltype=\"3G\""
    Ok(JsonReply.replyWithJSON("success", UIUtils.selectCoverageFromHive(sql)))
  }

  def getCoverage2g() = AuthenticatedJsonAction {
    // select cells from table
    val tableName = "coverage"
    val sql = "select latitude as lat,longitude as lon, value as v from " + tableName + " where celltype=\"2G\""
    Ok(JsonReply.replyWithJSON("success", UIUtils.selectCoverageFromHive(sql)))
  }


  /**
    * USING HDFS
    *
    */

  def getSizeHDFS() = AuthenticatedJsonAction { request =>
    try {
      var cmd = "hadoop fs -du -s /user/hive/warehouse"
      var result = cmd !!
      //Get the bytes only
      var splits = result.split(" ")
      val jo = new JSONArray()

      jo.put(toDouble(splits(0)).getOrElse(0.0))
      jo.put(toDouble(splits(0)).getOrElse(0.0) * 9.06)
      Ok(JsonReply.replyWithJSON("success", jo))
    } catch {
      case e: Exception => {
        e.printStackTrace()
        Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
      }
    }

  }

  def getCellTowersHDFS() = AuthenticatedJsonAction { request =>
    try {
      val conf = new Configuration()
      conf.addResource("/usr/local/hadoop/etc/hadoop/core-site.xml")
      conf.addResource("/usr/local/hadoop/etc/hadoop/hdfs-site.xml")

      val fileSystem = FileSystem.get(new URI(UIUtils.hdsfconnectionstring), conf)
      val filename = "/user/hive/warehouse/cells"
      val path: Path = new Path(filename)
      var fileContents = List[Iterator[String]]()
      val status = fileSystem.listStatus(path)
      println("status:" + status.length)
      status.foreach(s => {
        val is = fileSystem.open(s.getPath)
        val gzInputStream = new GZIPInputStream(new BufferedInputStream(is))
        fileContents ::= Source.fromInputStream(gzInputStream).getLines
      })
      //    while()

      Ok(JsonReply.replyWithJSON("success", UIUtils.selectCellsFromHDFS(fileContents, "")))
    } catch {
      case e: Exception => {
        e.printStackTrace()
        Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
      }
    }

  }

  def getCoverage2gHDFS() = AuthenticatedJsonAction { request =>
    try {
      val conf = new Configuration()
      conf.addResource("/usr/local/hadoop/etc/hadoop/core-site.xml")
      conf.addResource("/usr/local/hadoop/etc/hadoop/hdfs-site.xml")
      val fileSystem = FileSystem.get(new URI(UIUtils.hdsfconnectionstring), conf)
      val filename = "/user/hive/warehouse/coverage"
      val path: Path = new Path(filename)
      var fileContents = List[Iterator[String]]()
      val status = fileSystem.listStatus(path)
      status.foreach(s => {
        val is = fileSystem.open(s.getPath)
        val gzInputStream = new GZIPInputStream(new BufferedInputStream(is))
        fileContents ::= Source.fromInputStream(gzInputStream).getLines
      })
      //    while()

      Ok(JsonReply.replyWithJSON("success", UIUtils.selectCoverageFromHDFS(fileContents, "2G")))
    } catch {
      case e: Exception => Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
    }

  }

  def getCoverage3gHDFS() = AuthenticatedJsonAction { request =>
    try {
      val conf = new Configuration()
      conf.addResource("/usr/local/hadoop/etc/hadoop/core-site.xml")
      conf.addResource("/usr/local/hadoop/etc/hadoop/hdfs-site.xml")
      val fileSystem = FileSystem.get(new URI(UIUtils.hdsfconnectionstring), conf)
      val filename = "/user/hive/warehouse/coverage"
      val path: Path = new Path(filename)
      var fileContents = List[Iterator[String]]()
      val status = fileSystem.listStatus(path)
      status.foreach(s => {
        val is = fileSystem.open(s.getPath)
        fileContents ::= Source.fromInputStream(is).getLines
      })
      //    while()

      Ok(JsonReply.replyWithJSON("success", UIUtils.selectCoverageFromHDFS(fileContents, "3G")))
    } catch {
      case e: Exception => Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
    }

  }

  def getCoverage4gHDFS() = AuthenticatedJsonAction { request =>
    try {
      val conf = new Configuration()
      conf.addResource("/usr/local/hadoop/etc/hadoop/core-site.xml")
      conf.addResource("/usr/local/hadoop/etc/hadoop/hdfs-site.xml")
      val fileSystem = FileSystem.get(new URI(UIUtils.hdsfconnectionstring), conf)
      val filename = "/user/hive/warehouse/coverage"
      val path: Path = new Path(filename)
      var fileContents = List[Iterator[String]]()
      val status = fileSystem.listStatus(path)
      status.foreach(s => {
        val is = fileSystem.open(s.getPath)
        fileContents ::= Source.fromInputStream(is).getLines
      })
      //    while()

      Ok(JsonReply.replyWithJSON("success", UIUtils.selectCoverageFromHDFS(fileContents, "4G")))
    } catch {
      case e: Exception => Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
    }

  }

  def getCoverage4gl(x: String, y: String, z: String) = AuthenticatedJsonAction { request =>

    if (!z.equalsIgnoreCase("18")) {
      Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
    }
    try {
      val conf = new Configuration()
      conf.addResource("/usr/local/hadoop/etc/hadoop/core-site.xml")
      conf.addResource("/usr/local/hadoop/etc/hadoop/hdfs-site.xml")
      val fileSystem = FileSystem.get(new URI(UIUtils.hdsfconnectionstring), conf)
      val filename = "/pythia/data/4g/" + x + "-" + y + "-" + z + ".heat"
      val path: Path = new Path(filename)
      val is = fileSystem.open(path)
      //    while()
      val fileContents = Source.fromInputStream(is).getLines
      Ok(JsonReply.replyWithJSON("success", UIUtils.selectFromHDFS(fileContents, -170)))
    } catch {
      case e: Exception => Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
    }

  }

  def getCoverage3gl(x: String, y: String, z: String) = AuthenticatedJsonAction { request =>
    if (!z.equalsIgnoreCase("18")) {
      Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
    }
    try {
      val conf = new Configuration()
      conf.addResource("/usr/local/hadoop/etc/hadoop/core-site.xml")
      conf.addResource("/usr/local/hadoop/etc/hadoop/hdfs-site.xml")
      val fileSystem = FileSystem.get(new URI(UIUtils.hdsfconnectionstring), conf)
      val filename = "/pythia/data/3g/" + x + "-" + y + "-" + z + ".heat"
      val path: Path = new Path(filename)
      val is = fileSystem.open(path)
      //    while()
      val fileContents = Source.fromInputStream(is).getLines
      Ok(JsonReply.replyWithJSON("success", UIUtils.selectFromHDFS(fileContents, -170)))
    } catch {
      case e: Exception => Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
    }
  }

  def getCoverage2gl(x: String, y: String, z: String) = AuthenticatedJsonAction { request =>
    if (!z.equalsIgnoreCase("18")) {
      Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
    }
    try {
      val conf = new Configuration()
      conf.addResource("/usr/local/hadoop/etc/hadoop/core-site.xml")
      conf.addResource("/usr/local/hadoop/etc/hadoop/hdfs-site.xml")
      val fileSystem = FileSystem.get(new URI(UIUtils.hdsfconnectionstring), conf)
      val filename = "/pythia/data/2g/" + x + "-" + y + "-" + z + ".heat"
      val path: Path = new Path(filename)
      val is = fileSystem.open(path)
      //    while()
      val fileContents = Source.fromInputStream(is).getLines
      Ok(JsonReply.replyWithJSON("success", UIUtils.selectFromHDFS(fileContents, -100)))
    } catch {
      case e: Exception => Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
    }

  }
}