package controllers

import java.io.BufferedInputStream
import java.net.URI
import java.util.zip.GZIPInputStream

import _root_.util.{JsonReply, UIUtils}
import _root_.util.actions.{AdminJsonAction, AuthenticatedAction}
import com.couchbase.client.java.view.{Stale, ViewQuery}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.json.JSONArray
import play.api.libs.json._
import play.api.mvc.Controller
import pythia.db.CouchbaseHelper
import pythia.db.CouchbaseViews._
import pythia.models.recorder.RecorderInfo

import scala.collection.{Iterator, mutable}
import scala.io.Source

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
object RecorderController extends Controller {

  implicit val formats = Json.format[RecorderInfo]

  def getInfo() = AdminJsonAction {
    //        val recorderState = MasterRef.getRecorderState
    //        Ok(Json.toJson(recorderState.recorderInfo))
    Ok("TODO")
  }

  def start() = AdminJsonAction {
    //        val res = MasterRef.startRecorder("hdfs://sparknode1:54310/pythia/store")
    Ok(JsonReply.success("recorder started"))
  }

  def stop() = AdminJsonAction {
    //        val res = MasterRef.stopRecorder()
    Ok(JsonReply.success("recorder stopped"))
  }


  def fetchHistoryCDR(start: String, end: String) = AuthenticatedAction { request =>
    //select * from cdrs where mytimestamp>="2015" AND mytimestamp<="2016";
    // select cells from table
    val tableName = "cdrs cd"
    val sql = "select cd.mytimestamp,cd.callingpartynumber,cd.productid,cd.downflux,cd.upflux,cd.elapseduration,cd.callingcellid from " + tableName + " where cd.mytimestamp>=\"" + start + "\" AND cd.mytimestamp<=\"" + end + "\""
    Ok(JsonReply.replyWithJSON("success", UIUtils.selectFromHive(sql)))

  }


  def fetchHistoryNMS(start: String, end: String) = AuthenticatedAction { request =>
    //select * from cdrs where mytimestamp>="2015" AND mytimestamp<="2016";
    // select cells from table
    val tableName = "cdrs cd"
    val sql = "select n.sitename,n.counter,n.value,c.celltype,c.cellid from nms n,cells c where n.counter=\"67194793\" AND n.sitename=c.sitename AND n.mytimestamp>=\"" + start + "\" AND n.mytimestamp<=\"" + end + "\""
    Ok(JsonReply.replyWithJSON("success", UIUtils.selectFromHive(sql)))

  }

  def fetchMovingObj() = AuthenticatedAction { request =>
    //select callingpartyimsi,productid,mytimestamp from cdrs where (callingpartyimsi+productid) in (select cd1.callingpartyimsi+cd1.productid from cdrs cd1, cdrs cd2 where cd1.callingpartyimsi = cd2.callingpartyimsi AND cd1.productid=cd2.productid AND cd1.callingcellid != cd2.callingcellid group by cd1.callingpartyimsi,cd1.productid) order by callingpartyimsi,productid,mytimestamp;

    val connection = CouchbaseHelper.historyBucket
    val vr = connection.query(
      ViewQuery.from(QUERIES, MOVING_IDS_VIEW).group(true).reduce(true).stale(Stale.FALSE)
    )
    val rows = vr.allRows()

    if (rows.size() == 0) {
      Ok(new JsArray())
    }
    else {
      val it = rows.iterator()
      val jsonList = mutable.MutableList[JsValue]()
      while (it.hasNext) {
        val row = it.next()
        val doc = row.value()
        var json = Json.parse(doc.toString)

        if (!(json \ "count").equals(new JsNumber(1))) {
          json = json.as[JsObject] + ("key" -> Json.parse(row.key().toString))
          jsonList += json
        }
      }
      val jsValue = Json.toJson(jsonList)
      Ok(jsValue)
    }
  }

  /**
    *
    * Using HIVE
    *
    *
    */

  def fetchMovedCDR(start: String, end: String) = AuthenticatedAction { request =>
    // select cd.callingpartyimsi,cd.productid,cd.mytimestamp,cast(c.latitude as decimal),c.longitude from cdrs cd,cells c  where cd.callingcellid=c.cellid AND (callingpartyimsi+productid) in
    // (select cd1.callingpartyimsi+cd1.productid from cdrs cd1, cdrs cd2 where cd1.callingpartyimsi = cd2.callingpartyimsi AND cd1.productid=cd2.productid AND cd1.callingcellid != cd2.callingcellid group by cd1.callingpartyimsi,cd1.productid)
    // order by callingpartyimsi,productid,mytimestamp;
    // select cells from table
    //    val tableName = "cdrs"
    //    val in = "(select cd1.callingpartyimsi+cd1.productid from " + tableName + " cd1, cdrs cd2 where cd1.callingpartyimsi = cd2.callingpartyimsi AND cd1.productid=cd2.productid AND cd1.callingcellid != cd2.callingcellid AND cd1.mytimestamp>=\"" + start + "\" AND cd2.mytimestamp<=\"" + end + "\" group by cd1.callingpartyimsi,cd1.productid )"
    //    val orderby = "order by cd.callingpartyimsi,cd.productid,cd.mytimestamp"
    val sql = "select distinct  cd.callingpartyimsi,cd.mytimestamp,cd.callingpartynumber,cd.productid,cd.downflux,cd.upflux,cd.elapseduration,cd.callingcellid from cdrs cd1, cdrs cd where cd1.callingpartyimsi = cd.callingpartyimsi AND cd1.productid=cd.productid AND cd1.callingcellid != cd.callingcellid AND cd1.mytimestamp>=\"" + start + "\" AND cd1.mytimestamp<=\"" + end + "\" AND  cd.mytimestamp>=\"" + start + "\" AND cd.mytimestamp<=\"" + end + "\" order by cd.callingpartyimsi, cd.productid, cd.mytimestamp"
    val tableName = "cdrs cd"
    // val sql = "select cd.mytimestamp,cd.callingpartynumber,cd.productid,cd.downflux,cd.upflux,cd.elapseduration,cd.callingcellid from " + tableName + " where cd.mytimestamp>=\"" + start + "\" AND cd.mytimestamp<=\"" + end + "\""


    Ok(JsonReply.replyWithJSON("success", UIUtils.selectFromHive(sql)))
  }

  def fetchDCDR(date: String) = AuthenticatedAction { request =>
    //select * from cdrs where mytimestamp>="2015" AND mytimestamp<="2016";
    // select cells from table
    val tableName = "cdrs cd"
    val sql = "select latitude,longitude,mytimestamp,callingpartynumber,productid,downflux,upflux from cdr_" + date + ""
    Ok(JsonReply.replyWithJSON("success", UIUtils.selectFromHive(sql)))

  }

  def fetchTCDR(date: String, start: String, end: String) = AuthenticatedAction { request =>
    //select * from cdrs where mytimestamp>="2015" AND mytimestamp<="2016";
    // select cells from table

    val exist = UIUtils.chekIfTableExistInHive("cdr_" + date)
    if (!exist)
      Ok(JsonReply.replyWithJSON("success", new JSONArray))
    else {
      val sql = "select latitude,longitude,mytimestamp,callingpartynumber,productid,downflux,upflux,elapseduration from cdr_" + date + " where mytimestamp >= '" + start + "' and mytimestamp <= '" + end + "'"
      Ok(JsonReply.replyWithJSON("success", UIUtils.selectFromHive(sql)))
    }
  }

  def fetchTSCDR(date: String, start: String, end: String, callingpartynumber: String) = AuthenticatedAction { request =>
    //select * from cdrs where mytimestamp>="2015" AND mytimestamp<="2016";
    // select cells from table

    val exist = UIUtils.chekIfTableExistInHive("cdr_" + date)
    if (!exist)
      Ok(JsonReply.replyWithJSON("success", new JSONArray))
    else {
      val sql = "select latitude,longitude,mytimestamp,callingpartynumber,productid,downflux,upflux,elapseduration" +
        " from cdr_" + date + " where mytimestamp >= '" + start + "' and mytimestamp <= '" + end + "'" +
        " and callingpartynumber = '" + callingpartynumber + "'"
      Ok(JsonReply.replyWithJSON("success", UIUtils.selectFromHive(sql)))
    }
  }

  def fetchTPCDR(date: String, start: String, end: String, productid: String) = AuthenticatedAction { request =>
    //select * from cdrs where mytimestamp>="2015" AND mytimestamp<="2016";
    // select cells from table

    val exist = UIUtils.chekIfTableExistInHive("cdr_" + date)
    if (!exist)
      Ok(JsonReply.replyWithJSON("success", new JSONArray))
    else {
      val sql = "select latitude,longitude,mytimestamp,callingpartynumber,productid,downflux,upflux,elapseduration" +
        " from cdr_" + date + " where mytimestamp >= '" + start + "' and mytimestamp <= '" + end + "'" +
        " and productid = '" + productid + "'"
      Ok(JsonReply.replyWithJSON("success", UIUtils.selectFromHive(sql)))
    }
  }

  def fetchTSPCDR(date: String, start: String, end: String, callingpartynumber: String, productid: String) = AuthenticatedAction { request =>
    //select * from cdrs where mytimestamp>="2015" AND mytimestamp<="2016";
    // select cells from table

    val exist = UIUtils.chekIfTableExistInHive("cdr_" + date)
    if (!exist)
      Ok(JsonReply.replyWithJSON("success", new JSONArray))
    else {
      val sql = "select latitude,longitude,mytimestamp,callingpartynumber,productid,downflux,upflux,elapseduration" +
        " from cdr_" + date + " where mytimestamp >= '" + start + "' and mytimestamp <= '" + end + "'" +
        " and callingpartynumber = '" + callingpartynumber + "'" + " and productid = '" + productid + "'"
      Ok(JsonReply.replyWithJSON("success", UIUtils.selectFromHive(sql)))
    }
  }

  def fetchTNMS3G(date: String, start: String, end: String) = AuthenticatedAction { request =>
    //select * from cdrs where mytimestamp>="2015" AND mytimestamp<="2016";
    // select cells from table

    val exist = UIUtils.chekIfTableExistInHive("nms_" + "3g_" + date)
    if (!exist)
      Ok(JsonReply.replyWithJSON("success", new JSONArray))
    else {
      val sql = "select latitude,longitude,mytimestamp,cellid,value,counter from nms_3g_" + date + " where mytimestamp >= '" + start + "' and mytimestamp <= '" + end + "'"
      Ok(JsonReply.replyWithJSON("success", UIUtils.selectFromHive(sql)))
    }
  }

  def fetchTNMS2G(date: String, start: String, end: String) = AuthenticatedAction { request =>
    //select * from cdrs where mytimestamp>="2015" AND mytimestamp<="2016";
    // select cells from table

    val exist = UIUtils.chekIfTableExistInHive("nms_" + "2g_" + date)
    if (!exist)
      Ok(JsonReply.replyWithJSON("success", new JSONArray))
    else {
      val sql = "select latitude,longitude,mytimestamp,cellid,value,counter from nms_2g_" + date + " where mytimestamp >= '" + start + "' and mytimestamp <= '" + end + "'"
      Ok(JsonReply.replyWithJSON("success", UIUtils.selectFromHive(sql)))
    }
  }

  /**
    *
    * Using HDFS
    *
    *
    */

  def fetchDCDRHDFS(date: String) = AuthenticatedAction { request =>
    try {
      val conf = new Configuration()
      conf.addResource("/usr/local/hadoop/etc/hadoop/core-site.xml")
      conf.addResource("/usr/local/hadoop/etc/hadoop/hdfs-site.xml")
      val fileSystem = FileSystem.get(new URI(UIUtils.hdsfconnectionstring), conf)
      val filename = "/user/hive/warehouse/ui.db/cdr_" + date
      val path: Path = new Path(filename)
      var fileContents = List[Iterator[String]]()
      val status = fileSystem.listStatus(path)
      status.foreach(s => {
        val is = fileSystem.open(s.getPath)
        val gzInputStream = new GZIPInputStream(new BufferedInputStream(is))
        fileContents ::= Source.fromInputStream(gzInputStream).getLines
      })
      //    while()

      Ok(JsonReply.replyWithJSON("success", UIUtils.selectCellsFromHDFS(fileContents, "")))
    } catch {
      case e: Exception => Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
    }
  }

  def fetchTCDRHDFS(date: String, start: String, end: String) = AuthenticatedAction { request =>
    try {
      val conf = new Configuration()
      conf.addResource("/usr/local/hadoop/etc/hadoop/core-site.xml")
      conf.addResource("/usr/local/hadoop/etc/hadoop/hdfs-site.xml")
      val fileSystem = FileSystem.get(new URI(UIUtils.hdsfconnectionstring), conf)
      val filename = "/user/hive/warehouse/ui.db/cdr_" + date
      val path: Path = new Path(filename)
      var fileContents = List[Iterator[String]]()
      val status = fileSystem.listStatus(path)
      status.foreach(s => {
        val is = fileSystem.open(s.getPath)
        val gzInputStream = new GZIPInputStream(new BufferedInputStream(is))
        fileContents ::= Source.fromInputStream(gzInputStream).getLines
      })
      //    while()

      Ok(JsonReply.replyWithJSON("success", UIUtils.selectCDRSFromHDFS(fileContents, start, end, "", "")))
    } catch {
      case e: Exception =>{
        e.printStackTrace()
        Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
      }
    }
  }

  def fetchTSCDRHDFS(date: String, start: String, end: String, callingpartynumber: String) = AuthenticatedAction { request =>
    try {
      val conf = new Configuration()
      conf.addResource("/usr/local/hadoop/etc/hadoop/core-site.xml")
      conf.addResource("/usr/local/hadoop/etc/hadoop/hdfs-site.xml")
      val fileSystem = FileSystem.get(new URI(UIUtils.hdsfconnectionstring), conf)
      val filename = "/user/hive/warehouse/ui.db/cdr_" + date
      val path: Path = new Path(filename)
      var fileContents = List[Iterator[String]]()
      val status = fileSystem.listStatus(path)
      status.foreach(s => {
        val is = fileSystem.open(s.getPath)
        val gzInputStream = new GZIPInputStream(new BufferedInputStream(is))
        fileContents ::= Source.fromInputStream(gzInputStream).getLines
      })
      //    while()

      Ok(JsonReply.replyWithJSON("success", UIUtils.selectCDRSFromHDFS(fileContents, start, end, callingpartynumber, "")))
    } catch {
      case e: Exception => Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
    }
  }

  def fetchTPCDRHDFS(date: String, start: String, end: String, productid: String) = AuthenticatedAction { request =>
    try {
      val conf = new Configuration()
      conf.addResource("/usr/local/hadoop/etc/hadoop/core-site.xml")
      conf.addResource("/usr/local/hadoop/etc/hadoop/hdfs-site.xml")
      val fileSystem = FileSystem.get(new URI(UIUtils.hdsfconnectionstring), conf)
      val filename = "/user/hive/warehouse/ui.db/cdr_" + date
      val path: Path = new Path(filename)
      var fileContents = List[Iterator[String]]()
      val status = fileSystem.listStatus(path)
      status.foreach(s => {
        val is = fileSystem.open(s.getPath)
        val gzInputStream = new GZIPInputStream(new BufferedInputStream(is))
        fileContents ::= Source.fromInputStream(gzInputStream).getLines
      })
      //    while()

      Ok(JsonReply.replyWithJSON("success", UIUtils.selectCDRSFromHDFS(fileContents, start, end, "", productid)))
    } catch {
      case e: Exception => Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
    }
  }

  def fetchTSPCDRHDFS(date: String, start: String, end: String, callingpartynumber: String, productid: String) = AuthenticatedAction { request =>
    try {
      val conf = new Configuration()
      conf.addResource("/usr/local/hadoop/etc/hadoop/core-site.xml")
      conf.addResource("/usr/local/hadoop/etc/hadoop/hdfs-site.xml")
      val fileSystem = FileSystem.get(new URI(UIUtils.hdsfconnectionstring), conf)
      val filename = "/user/hive/warehouse/ui.db/cdr_" + date
      val path: Path = new Path(filename)
      var fileContents = List[Iterator[String]]()
      val status = fileSystem.listStatus(path)
      status.foreach(s => {
        val is = fileSystem.open(s.getPath)
        val gzInputStream = new GZIPInputStream(new BufferedInputStream(is))
        fileContents ::= Source.fromInputStream(gzInputStream).getLines
      })
      //    while()

      Ok(JsonReply.replyWithJSON("success", UIUtils.selectCDRSFromHDFS(fileContents, start, end, callingpartynumber, productid)))
    } catch {
      case e: Exception => Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
    }
  }

  def fetchTNMS3GHDFS(date: String, start: String, end: String) = AuthenticatedAction { request =>
    try {
      val conf = new Configuration()
      conf.addResource("/usr/local/hadoop/etc/hadoop/core-site.xml")
      conf.addResource("/usr/local/hadoop/etc/hadoop/hdfs-site.xml")
      val fileSystem = FileSystem.get(new URI(UIUtils.hdsfconnectionstring), conf)
      val filename = "/user/hive/warehouse/ui.db/nms_3g_" + date
      val path: Path = new Path(filename)
      var fileContents = List[Iterator[String]]()
      val status = fileSystem.listStatus(path)
      status.foreach(s => {
        val is = fileSystem.open(s.getPath)
        val gzInputStream = new GZIPInputStream(new BufferedInputStream(is))
        fileContents ::= Source.fromInputStream(gzInputStream).getLines
      })
      //    while()

      Ok(JsonReply.replyWithJSON("success", UIUtils.selectNMSFromHDFS(fileContents, start, end)))
    } catch {
      case e: Exception => Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
    }
  }

  def fetchTNMS2GHDFS(date: String, start: String, end: String) = AuthenticatedAction { request =>
    try {
      val conf = new Configuration()
      conf.addResource("/usr/local/hadoop/etc/hadoop/core-site.xml")
      conf.addResource("/usr/local/hadoop/etc/hadoop/hdfs-site.xml")
      val fileSystem = FileSystem.get(new URI(UIUtils.hdsfconnectionstring), conf)
      val filename = "/user/hive/warehouse/ui.db/nms_2g_" + date
      val path: Path = new Path(filename)
      var fileContents = List[Iterator[String]]()
      val status = fileSystem.listStatus(path)
      status.foreach(s => {
        val is = fileSystem.open(s.getPath)
        val gzInputStream = new GZIPInputStream(new BufferedInputStream(is))
        fileContents ::= Source.fromInputStream(gzInputStream).getLines
      })
      //    while()

      Ok(JsonReply.replyWithJSON("success", UIUtils.selectNMSFromHDFS(fileContents, start, end)))
    } catch {
      case e: Exception => Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
    }
  }

  def createTableForDate(date: String) = AuthenticatedAction { request =>
    //select * from cdrs where mytimestamp>="2015" AND mytimestamp<="2016";
    // select cells from table
    val tableName = "cdrs cd"
    val sql = "CREATE TABLE cdr_" + date + " AS\nselect c.latitude,c.longitude,cd.mytimestamp,cd.callingpartynumber,cd.productid,cd.downflux,cd.upflux,cd.elapseduration \nfrom cdrs cd,cells c\nwhere cd.callingcellid=c.cellid and cd.mytimestamp like '" + date + "%'"
    Ok(JsonReply.replyWithJSON("success", UIUtils.selectFromHive(sql)))

  }


  def fetchTHNMS3GHDFS(date: String, start: String, end: String) = AuthenticatedAction { request =>
    try {
      val conf = new Configuration()
      conf.addResource("/usr/local/hadoop/etc/hadoop/core-site.xml")
      conf.addResource("/usr/local/hadoop/etc/hadoop/hdfs-site.xml")
      val fileSystem = FileSystem.get(new URI(UIUtils.hdsfconnectionstring), conf)
      val filename = "/user/hive/warehouse/ui.db/nms_3g_th_" + date
      val path: Path = new Path(filename)
      var fileContents = List[Iterator[String]]()
      val status = fileSystem.listStatus(path)
      status.foreach(s => {
        val is = fileSystem.open(s.getPath)
        val gzInputStream = new GZIPInputStream(new BufferedInputStream(is))
        fileContents ::= Source.fromInputStream(gzInputStream).getLines
      })
      //    while()

      Ok(JsonReply.replyWithJSON("success", UIUtils.selectNMSFromHDFS(fileContents, start, end)))
    } catch {
      case e: Exception => Ok(JsonReply.replyWithJSON("success", new org.json.JSONArray()))
    }
  }
}
