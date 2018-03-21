package connectors

import java.io.{File}


import scala.collection.immutable.HashMap
import scala.xml.XML

/**
 * Created by Costantinos on 29/6/2015.
 */
object NMSConnector extends Connector {

  final val TABS = 1
  final val LIMIT = 100000000
  var map: HashMap[Int, Array[String]] = new HashMap[Int, Array[String]]

  def isAllDigits(x: String) = x forall Character.isDigit

  def clearCHMs() = {
    map = new HashMap[Int, Array[String]]
  }

  def parse(input:String): NMSObject = {

    val xml = XML.loadFile(input)
    val beginTime = xml \\ "fileHeader" \ "measCollec" \"@beginTime"
    val endTime = xml \\ "fileFooter" \ "measCollec" \"@endTime"
    val measInfo = xml \\ "measCollecFile" \ "measData" \ "measInfo"

    //Create a hashmap with ids and values counter in order to get

    val nMSObject: NMSObject = new NMSObject()

    nMSObject.setNeNameAndTime((xml \\ "measCollecFile" \ "measData" \ "managedElement" \ "@userLabel").text,beginTime.text,endTime.text)


    measInfo.foreach(x => {
      val measInfoObj = new MeasInfo((x\"@measInfoId").text)

      measInfoObj.setKeyArray((x \ "measTypes").text.split(" "))

      val measValue = {
        x \ "measValue"
      }
      //get the site id measObjLdn

      measValue.foreach(y => {
        measInfoObj.addToValueMap((y \ "@measObjLdn").text -> (y \ "measResults").text.split(" "))

      })


      nMSObject.addMeasInfo(measInfoObj)
     // println(measInfoObj)
    })
    //Return the ocs-cdr object
    nMSObject
  }

  def parseString(input:String): NMSObject = {

    val xml = XML.loadString(input)
    val beginTime = xml \\ "fileHeader" \ "measCollec" \"@beginTime"
    val endTime = xml \\ "fileFooter" \ "measCollec" \"@endTime"
    val measInfo = xml \\ "measCollecFile" \ "measData" \ "measInfo"

    //Create a hashmap with ids and values counter in order to get

    val nMSObject: NMSObject = new NMSObject()

    nMSObject.setNeNameAndTime((xml \\ "measCollecFile" \ "measData" \ "managedElement" \ "@userLabel").text,beginTime.text,endTime.text)


    measInfo.foreach(x => {
      val measInfoObj = new MeasInfo((x\"@measInfoId").text)

      measInfoObj.setKeyArray((x \ "measTypes").text.split(" "))

      val measValue = {
        x \ "measValue"
      }
      //get the site id measObjLdn

      measValue.foreach(y => {
        measInfoObj.addToValueMap((y \ "@measObjLdn").text -> (y \ "measResults").text.split
        (" "))

      })


      nMSObject.addMeasInfo(measInfoObj)
      println(measInfoObj)
    })
    //Return the ocs-cdr object
    nMSObject
  }


  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def printList(args: TraversableOnce[_]): Unit = {
    args.foreach(println)
  }

  def main(args: Array[String]) = {
    val o=parse("C:\\Users\\costantinos\\Desktop\\todisk\\UCY\\PhD\\mtn\\DATA\\NMS\\UCY Samples\\A20150326.2045+0200-2100+0200_KEN_BSC_001.xml")
    println(o)
  }
}
