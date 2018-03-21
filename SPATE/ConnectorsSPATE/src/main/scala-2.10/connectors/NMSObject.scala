package connectors

import java.sql.Timestamp
import java.text.SimpleDateFormat

import org.apache.commons.lang.StringUtils
import utils.Queries

import scala.collection.immutable.HashSet
import scala.collection.immutable.HashSet.HashSet1
import scala.collection.mutable.ListBuffer

/**
  * Created by Costantinos on 29/6/2015.
  */
class NMSObject() extends ConnectorObject {
  val measInfoList = new ListBuffer[MeasInfo]()
  private var neName = ""
  private var beginTime = ""
  private var endTime = ""

  def setNeNameAndTime(neName: String, beginTime: String, endTime: String) {
    //2015-03-26T00:00:00+02:00
    this.neName = neName
    val sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    val sdfDate = new SimpleDateFormat("yyyyMMddHHmm")
    // val bd = new Date(sdf.parse(beginTime).getTime)
    val bts = new Timestamp(sdf.parse(beginTime).getTime)
    //     val ed = new Date(sdf.parse(endTime).getTime)
    val ets = new Timestamp(sdf.parse(endTime).getTime)
    //20150610235951
    this.beginTime = sdfDate.format(bts)
    this.endTime = sdfDate.format(ets)
  }

  def getTimeStamp: String = {
    this.beginTime
  }

  def addMeasInfo(measInfo: MeasInfo): Unit = {
    measInfoList += measInfo
  }

  override def toString: String = {
    var str = ""
    val list = toHiveRecords
    list.foreach(x => str += x + "\n")
    str
  }

  //val NMS_SCHEMA = "ID:INT,Counter:STRING,Description:STRING,CellID:INT,CellSite:STRING,Value:INT,MyTimeStamp:STRING"


  def toHiveRecords(): ListBuffer[String] = {


    val records = new ListBuffer[String]()
    measInfoList.foreach(x => {
      for ((key, value) <- x.valueMap)
        for (i <- x.keys.indices) {
          // str = key.siteName + "|" + key.cellName + "|" + key.iD + "|" + ":\t" + value.deep.mkString("\t")
          records += (neName + "|" + key.measInfoId + "|" + x.keys(i) + "|" + key.siteName + "|" + key.cellID + "|" + key.cellName + "|" + value.deep(i) + "|" + beginTime + "\n").toString
        }
    }
    )
    records
  }

  def toHiveRecords(date: String): ListBuffer[String] = {
    val ystr = date.substring(0, 4)
    val ymstr = date.substring(0, 6)
    val ymdstr = date

    val records = new ListBuffer[String]()
    measInfoList.foreach(x => {
      for ((key, value) <- x.valueMap)
        for (i <- x.keys.indices) {
          // str = key.siteName + "|" + key.cellName + "|" + key.iD + "|" + ":\t" + value.deep.mkString("\t")
          records += (Queries.md5withEncryption(neName) + "|" + key.measInfoId + "|" + x.keys(i) + "|" + Queries.md5withEncryption(key.siteName) + "|" + key.cellID + "|" + Queries.md5withEncryption(key.cellName) + "|" + value.deep(i) + "|" + beginTime + "|" + ystr + "|" + ymstr + "|" + ymdstr + "\n").toString
        }
    }
    )
    records
  }

  def toHiveUIRecords(cells: scala.collection.mutable.Map[String, String], set: HashSet[String]): ListBuffer[String] = {
    val records = new ListBuffer[String]()
    measInfoList.foreach(x => {
      for ((key, value) <- x.valueMap)
        for (i <- x.keys.indices) {
          if (set.contains(x.keys(i))) {
            // str = key.siteName + "|" + key.cellName + "|" + key.iD + "|" + ":\t" + value.deep.mkString("\t")
            //          nename      	measinfoid	            counter         	sitename            	cellid	        cellname	            value           	mytimestamp
            //" where n.cellname=c.cellname and n.nename=c.nename "
            // " select c.latitude,c.longitude,c.cellid,n.counter,n.value,n.mytimestamp" +
            val loc = cells.getOrElse(Queries.md5withEncryption(key.cellName) + Queries.md5withEncryption(neName), null)
            if (loc != null)
              records += (loc + "|" + key.cellID + "|" + x.keys(i) + "|" + value.deep(i) + "|" + beginTime + "\n").toString
          }
        }
    }
    )
    records
  }
}


class MeasValueKey(var measInfoId: String, var siteName: String, var cellName: String, var cellID: Int) {
}

class MeasInfo(private var measInfoId: String) {

  var keys = Array[String]()
  var valueMap = Map[MeasValueKey, Array[String]]()
  var SiteName = ""
  var CellName = ""
  var CellID = 0

  def getValueMap: Map[MeasValueKey, Array[String]] = {
    valueMap
  }

  /*
   * Remove everthing relate to PRIMETEL
   */
  def addToValueMap(entry: (String, Array[String])): Unit = {

    if (StringUtils.containsIgnoreCase(entry._1, "Label")) {
      if (StringUtils.containsIgnoreCase(entry._1, "IP:Label")) {
        SiteName = entry._1.split("(?i)Label")(1).split("=")(1).split(",")(0)
        if (SiteName.contains("PTL_"))
          return
      }
      else {
        CellName = entry._1.split("(?i)Label")(1).split("=")(1).split(",")(0)
        if (CellName.contains("PTL_"))
          return
      }
    }

    if (entry._1.contains("CellID")) {
      CellID = entry._1.split("CellID")(1).split("=")(1).split("/")(0).toInt
    }

    valueMap += (new MeasValueKey(measInfoId, SiteName, CellName, CellID) -> entry._2)
  }

  def setKeyArray(array: Array[String]): Unit = {
    keys = array
  }

  override def toString: String = {
    var str = "Keys:\t" + keys.deep.mkString("\t") + "\n"
    for ((key, value) <- valueMap)
    //      str += value.toString
      str += key.measInfoId + "|" + key.siteName + "|" + key.cellName + "|" + key.cellID + "|" + ":\t" + value.deep.mkString("\t") + "\n"
    str
  }

}
