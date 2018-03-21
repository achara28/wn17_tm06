package utils

import java.security.MessageDigest
import java.sql.{DriverManager, ResultSet}
import java.util
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

import sun.misc.BASE64Encoder

import scala.collection.mutable.ListBuffer
import scala.util.Random


/**
  * Created by costantinos on 12/1/2016.
  */
object Queries {


  var basename = "pythia1"
  val connectionstring = "jdbc:hive2://" + basename + ":10000/default"
  val hdsfconnectionstring = "hdfs://" + basename + ":54310"

  val usename = "pythia"
  val password = ""


  def encryption(s: String): Array[Byte] = {
    val SALT2 = "spate deliciously salty"
    var key = (SALT2 + s).getBytes("UTF-8")
    val sha = MessageDigest.getInstance("SHA-1")
    key = sha.digest(key)
    key = util.Arrays.copyOf(key, 16)
    // use only first 128 bit
    val secretKeySpec = new SecretKeySpec(key, "AES")
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
    cipher.doFinal(s.getBytes())
  }

  def md5withEncryption(s: String): String = {
    MessageDigest.getInstance("MD5").digest(encryption(s)).map(0xFF & _).map {
      "%02x".format(_)
    }.foldLeft("") {
      _ + _
    }
  }

  def md5WithRandom(s: String): String = {
    MessageDigest.getInstance("MD5").digest((s + Random.alphanumeric.take(10).mkString).getBytes()).map(0xFF & _).map {
      "%02x".format(_)
    }.foldLeft("") {
      _ + _
    }
  }


  def getHashCode(string: String): Int = {
    var hash = 7
    string.foreach(c => hash = hash * 31 + c)
    hash
  }

  def toInt(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case e: Exception => None
    }
  }

  def insertIntoTableInHive(table: String, filename: String, partition: String): Boolean
  = {
    val driverName = "org.apache.hive.jdbc.HiveDriver"
    val sql = "LOAD DATA INPATH '" + filename + "' INTO TABLE " + table + " " + partition
    try {
      Class.forName(driverName)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    //val con = DriverManager.getConnection("jdbc:hive2://192.168.14.30:10000/default")
    val con = DriverManager.getConnection(connectionstring, usename, password)
    val stmt = con.createStatement()
    print(sql)
    val res = stmt.execute(sql)
    stmt.close()
    con.close()
    res
  }

  def executeDDLSQLInHive(sql: String): Int
  = {
    val driverName = "org.apache.hive.jdbc.HiveDriver"
    try {
      Class.forName(driverName)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    val con = DriverManager.getConnection(connectionstring, usename, password)
    val stmt = con.createStatement()
    val res = stmt.executeUpdate(sql)
    stmt.close()
    con.close()
    res
  }

  def getTimestampsFromHive(sql: String): util.ArrayList[String] = {
    val driverName = "org.apache.hive.jdbc.HiveDriver"

    try {
      Class.forName(driverName)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    //val con = DriverManager.getConnection("jdbc:hive2://192.168.14.30:10000/default")
    val con = DriverManager.getConnection(connectionstring, usename, password)
    val stmt = con.createStatement()
    System.out.println("Running: " + sql)
    val res = stmt.executeQuery(sql)
    val dates = new util.ArrayList[String]()
    if (res.next()) {
      do {
        val date = res.getString(1)
        dates.add(date)
      } while (res.next)
    }

    stmt.close()
    con.close()
    res.close()
    dates
  }

  val CDRS_TABLE = "CDRS"
  val CDRS_TABLE_TMP = "CDRS_TMP"
  val NMS_TABLE = "NMS"
  val NMS_TABLE_TMP = "NMS_TMP"
  val CELLS_TABLE = "CELLS"
  val COVERAGE_TABLE = "COVERAGE"


  val CREATE_UI_DB = "CREATE DATABASE IF NOT EXISTS ui"

  val CREATE_CELLS = "CREATE TABLE IF NOT EXISTS  CELLS(NEName STRING,SiteName STRING,CellName STRING,ID STRING,LAC STRING,RAC STRING,LATITUDE DECIMAL(9,7),LONGITUDE DECIMAL(9,7),CELLID STRING,CELLTYPE STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY \"|\""
  val CREATE_CDRS = "CREATE TABLE IF NOT EXISTS CDRS (SerialNo INT,SubSequence INT,MyTimeStamp STRING,ServiceKey INT,CallingPartyNumber STRING,APN STRING,URL STRING,CallingPartyIMSI STRING,AccessNetworkAddress STRING,GGSNAddress STRING,CallingRoamInfo STRING,CallingCellID STRING,TimeStampOfSGSN STRING,TimeZoneOfSGSN INT,Reserved0 STRING,ChargingTime STRING,TotalFlux INT,UpFlux INT,DownFlux INT,ElapseDuration INT,TerminationReason INT,IMEI STRING,TransitionID STRING,ServiceID STRING,SPID STRING,CategoryID STRING,ContentID STRING,QoS STRING,OriginalNetworkType STRING,DiameterSessionID STRING,BrandID INT,SubCOSID INT,ChargingPartyNumber STRING,PayType STRING,BillCycleID STRING,ChargingType STRING,ResultCode INT,RoamState STRING,CallingHomeCountryCode INT,CallingHomeAreaNumber INT,CallingHomeNetworkCode INT,CallingRoamCountryCode INT,CallingRoamAreaNumber INT,CallingRoamNetworkCode INT,Reserved1 STRING,Reserved2 STRING,Reserved3 STRING,Reserved4 STRING,Reserved5 STRING,Reserved6 STRING,ProductID STRING,ServiceType STRING,Reserved7 STRING,HomeZoneID INT,UserState STRING,SubscriberID STRING,ChargeDuration INT,TotalChargeFlux INT,Reserved8 STRING,Reserved9 STRING,ChargeOfFluxAccounts INT,ChargeOfDurationAccounts INT,ChargeOfFundAccounts INT,ChargeFromPrepaid INT,PrepaidBalance INT,ChargeFromPostpaid INT,PostPaidBalance INT,AccountID STRING,AccountKey INT,CurrencyCode INT,AccountType1 INT,FeeType1 INT,ChargeAmount1 INT,CurrentAcctAmount1 INT,AccountType2 INT,FeeType2 INT,ChargeAmount2 INT,CurrentAcctAmount2 INT,AccountType3 INT,FeeType3 INT,ChargeAmount3 INT,CurrentAcctAmount3 INT,AccountType4 INT,FeeType4 INT,ChargeAmount4 INT,CurrentAcctAmount4 INT,AccountType5 INT,FeeType5 INT,ChargeAmount5 INT,CurrentAcctAmount5 INT,AccountType6 INT,FeeType6 INT,ChargeAmount6 INT,CurrentAcctAmount6 INT,AccountType7 INT,FeeType7 INT,ChargeAmount7 INT,CurrentAcctAmount7 INT,AccountType8 INT,FeeType8 INT,ChargeAmount8 INT,CurrentAcctAmount8 INT,AccountType9 INT,FeeType9 INT,ChargeAmount9 INT,CurrentAcctAmount9 INT,AccountType10 INT,FeeType10 INT,ChargeAmount10 INT,CurrentAcctAmount10 INT,BonusValidity1 STRING,BonusValidity2 STRING,BonusValidity3 STRING,BonusValidity4 STRING,BonusValidity5 STRING,BonusValidity6 STRING,BonusValidity7 STRING,BonusValidity8 STRING,BonusValidity9 STRING,BonusValidity10 STRING,OnlineType INT,Reserved10 STRING,Reserved11 STRING,Reserved12 STRING,AddtionalInfo STRING,BearerType INT,StartTime STRING,StopTime STRING,BearerProtocolType INT,ChargingID STRING,ServiceLevel INT,StartTimeOfBillCycle STRING,ChargePartyIndicator STRING,SecondChargingPartyNumber STRING,SecondChargingPartyPayType STRING,SecondChargingPartyBillCycleID STRING,CallingVPNTopGroupNumber STRING,CallingVPNGroupNumber STRING,CallingVPNShortNumber STRING,CalledVPNTopGroupNumber STRING,CalledVPNGroupNumber STRING,CalledVPNShortNumber STRING,CallingNetworkType STRING,CalledNetworkType STRING,GroupCallType STRING,GroupPayFlag STRING,Reserved13 STRING,Reserved14 STRING,Reserved15 STRING,Reserved16 STRING,Reserved17 STRING,ChargeOfFluxSecondAccounts INT,ChargeOfDurationSecondAccounts INT,ChargeOfFundSecondAccounts INT,ChargeFromSecondPrepaidAccount INT,SecondPrepaidAccountBalance INT,ChargeFromSecondPostpaidAccount INT,SecondPostpaidAccountBalance INT,SecondAccountID STRING,SecondAccountKey INT,SecondAccountCurrencyCode INT,SecondAccountType1 INT,SecondFeeType1 INT,SecondChargeAmount1 INT,SecondCurrentAcctAmount1 INT,SecondAccountType2 INT,SecondFeeType2 INT,SecondChargeAmount2 INT,SecondCurrentAcctAmount2 INT,SecondAccountType3 INT,SecondFeeType3 INT,SecondChargeAmount3 INT,SecondCurrentAcctAmount3 INT,SecondAccountType4 INT,SecondFeeType4 INT,SecondChargeAmount4 INT,SecondCurrentAcctAmount4 INT,SecondAccountType5 INT,SecondFeeType5 INT,SecondChargeAmount5 INT,SecondCurrentAcctAmount5 INT,SecondBonusValidity1 STRING,SecondBonusValidity2 STRING,SecondBonusValidity3 STRING,SecondBonusValidity4 STRING,SecondBonusValidity5 STRING,SubscriberIDType STRING,subscriberkey INT,SecondSubscriberKey INT,CustomerKey1 INT,CustomerCode1 STRING,CustomerKey2 INT,CustomerCode2 STRING,TenantID INT )" + Queries.addPartition() + " ROW FORMAT DELIMITED FIELDS TERMINATED BY \"|\""
  val CREATE_NMS = "CREATE TABLE IF NOT EXISTS NMS (NEName STRING,MeasInfoId INT,Counter STRING,SiteName STRING,CellID INT,CellName STRING,Value INT,MyTimeStamp STRING)" + Queries.addPartition() + " ROW FORMAT DELIMITED FIELDS TERMINATED BY \"|\""
  val CREATE_CDRS_TMP = "CREATE TABLE IF NOT EXISTS CDRS_TMP (SerialNo INT,SubSequence INT,MyTimeStamp STRING,ServiceKey INT,CallingPartyNumber STRING,APN STRING,URL STRING,CallingPartyIMSI STRING,AccessNetworkAddress STRING,GGSNAddress STRING,CallingRoamInfo STRING,CallingCellID STRING,TimeStampOfSGSN STRING,TimeZoneOfSGSN INT,Reserved0 STRING,ChargingTime STRING,TotalFlux INT,UpFlux INT,DownFlux INT,ElapseDuration INT,TerminationReason INT,IMEI STRING,TransitionID STRING,ServiceID STRING,SPID STRING,CategoryID STRING,ContentID STRING,QoS STRING,OriginalNetworkType STRING,DiameterSessionID STRING,BrandID INT,SubCOSID INT,ChargingPartyNumber STRING,PayType STRING,BillCycleID STRING,ChargingType STRING,ResultCode INT,RoamState STRING,CallingHomeCountryCode INT,CallingHomeAreaNumber INT,CallingHomeNetworkCode INT,CallingRoamCountryCode INT,CallingRoamAreaNumber INT,CallingRoamNetworkCode INT,Reserved1 STRING,Reserved2 STRING,Reserved3 STRING,Reserved4 STRING,Reserved5 STRING,Reserved6 STRING,ProductID STRING,ServiceType STRING,Reserved7 STRING,HomeZoneID INT,UserState STRING,SubscriberID STRING,ChargeDuration INT,TotalChargeFlux INT,Reserved8 STRING,Reserved9 STRING,ChargeOfFluxAccounts INT,ChargeOfDurationAccounts INT,ChargeOfFundAccounts INT,ChargeFromPrepaid INT,PrepaidBalance INT,ChargeFromPostpaid INT,PostPaidBalance INT,AccountID STRING,AccountKey INT,CurrencyCode INT,AccountType1 INT,FeeType1 INT,ChargeAmount1 INT,CurrentAcctAmount1 INT,AccountType2 INT,FeeType2 INT,ChargeAmount2 INT,CurrentAcctAmount2 INT,AccountType3 INT,FeeType3 INT,ChargeAmount3 INT,CurrentAcctAmount3 INT,AccountType4 INT,FeeType4 INT,ChargeAmount4 INT,CurrentAcctAmount4 INT,AccountType5 INT,FeeType5 INT,ChargeAmount5 INT,CurrentAcctAmount5 INT,AccountType6 INT,FeeType6 INT,ChargeAmount6 INT,CurrentAcctAmount6 INT,AccountType7 INT,FeeType7 INT,ChargeAmount7 INT,CurrentAcctAmount7 INT,AccountType8 INT,FeeType8 INT,ChargeAmount8 INT,CurrentAcctAmount8 INT,AccountType9 INT,FeeType9 INT,ChargeAmount9 INT,CurrentAcctAmount9 INT,AccountType10 INT,FeeType10 INT,ChargeAmount10 INT,CurrentAcctAmount10 INT,BonusValidity1 STRING,BonusValidity2 STRING,BonusValidity3 STRING,BonusValidity4 STRING,BonusValidity5 STRING,BonusValidity6 STRING,BonusValidity7 STRING,BonusValidity8 STRING,BonusValidity9 STRING,BonusValidity10 STRING,OnlineType INT,Reserved10 STRING,Reserved11 STRING,Reserved12 STRING,AddtionalInfo STRING,BearerType INT,StartTime STRING,StopTime STRING,BearerProtocolType INT,ChargingID STRING,ServiceLevel INT,StartTimeOfBillCycle STRING,ChargePartyIndicator STRING,SecondChargingPartyNumber STRING,SecondChargingPartyPayType STRING,SecondChargingPartyBillCycleID STRING,CallingVPNTopGroupNumber STRING,CallingVPNGroupNumber STRING,CallingVPNShortNumber STRING,CalledVPNTopGroupNumber STRING,CalledVPNGroupNumber STRING,CalledVPNShortNumber STRING,CallingNetworkType STRING,CalledNetworkType STRING,GroupCallType STRING,GroupPayFlag STRING,Reserved13 STRING,Reserved14 STRING,Reserved15 STRING,Reserved16 STRING,Reserved17 STRING,ChargeOfFluxSecondAccounts INT,ChargeOfDurationSecondAccounts INT,ChargeOfFundSecondAccounts INT,ChargeFromSecondPrepaidAccount INT,SecondPrepaidAccountBalance INT,ChargeFromSecondPostpaidAccount INT,SecondPostpaidAccountBalance INT,SecondAccountID STRING,SecondAccountKey INT,SecondAccountCurrencyCode INT,SecondAccountType1 INT,SecondFeeType1 INT,SecondChargeAmount1 INT,SecondCurrentAcctAmount1 INT,SecondAccountType2 INT,SecondFeeType2 INT,SecondChargeAmount2 INT,SecondCurrentAcctAmount2 INT,SecondAccountType3 INT,SecondFeeType3 INT,SecondChargeAmount3 INT,SecondCurrentAcctAmount3 INT,SecondAccountType4 INT,SecondFeeType4 INT,SecondChargeAmount4 INT,SecondCurrentAcctAmount4 INT,SecondAccountType5 INT,SecondFeeType5 INT,SecondChargeAmount5 INT,SecondCurrentAcctAmount5 INT,SecondBonusValidity1 STRING,SecondBonusValidity2 STRING,SecondBonusValidity3 STRING,SecondBonusValidity4 STRING,SecondBonusValidity5 STRING,SubscriberIDType STRING,subscriberkey INT,SecondSubscriberKey INT,CustomerKey1 INT,CustomerCode1 STRING,CustomerKey2 INT,CustomerCode2 STRING,TenantID INT ) ROW FORMAT DELIMITED FIELDS TERMINATED BY \"|\""
  val CREATE_NMS_TMP = "CREATE TABLE IF NOT EXISTS NMS_TMP (NEName STRING,MeasInfoId INT,Counter STRING,SiteName STRING,CellID INT,CellName STRING,Value INT,MyTimeStamp STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY \"|\""
  val CDRS_RECENT_SCHEMA = "SerialNo:INT,SubSequence:INT,MyTimeStamp:STRING,ServiceKey:INT,CallingPartyNumber:STRING,APN:STRING,URL:STRING,CallingPartyIMSI:STRING,AccessNetworkAddress:STRING,GGSNAddress:STRING,CallingRoamInfo:STRING,CallingCellID:STRING,TimeStampOfSGSN:STRING,TimeZoneOfSGSN:INT,Reserved0:STRING,ChargingTime:STRING,TotalFlux:INT,UpFlux:INT,DownFlux:INT,ElapseDuration:INT,TerminationReason:INT,IMEI:STRING,TransitionID:STRING,ServiceID:STRING,SPID:STRING,CategoryID:STRING,ContentID:STRING,QoS:STRING,OriginalNetworkType:STRING,DiameterSessionID:STRING,BrandID:INT,SubCOSID:INT,ChargingPartyNumber:STRING,PayType:STRING,BillCycleID:STRING,ChargingType:STRING,ResultCode:INT,RoamState:STRING,CallingHomeCountryCode:INT,CallingHomeAreaNumber:INT,CallingHomeNetworkCode:INT,CallingRoamCountryCode:INT,CallingRoamAreaNumber:INT,CallingRoamNetworkCode:INT,Reserved1:STRING,Reserved2:STRING,Reserved3:STRING,Reserved4:STRING,Reserved5:STRING,Reserved6:STRING,ProductID:STRING,ServiceType:STRING,Reserved7:STRING,HomeZoneID:INT,UserState:STRING,SubscriberID:STRING,ChargeDuration:INT,TotalChargeFlux:INT,Reserved8:STRING,Reserved9:STRING,ChargeOfFluxAccounts:INT,ChargeOfDurationAccounts:INT,ChargeOfFundAccounts:INT,ChargeFromPrepaid:INT,PrepaidBalance:INT,ChargeFromPostpaid:INT,PostPaidBalance:INT,AccountID:STRING,AccountKey:INT,CurrencyCode:INT,AccountType1:INT,FeeType1:INT,ChargeAmount1:INT,CurrentAcctAmount1:INT,AccountType2:INT,FeeType2:INT,ChargeAmount2:INT,CurrentAcctAmount2:INT,AccountType3:INT,FeeType3:INT,ChargeAmount3:INT,CurrentAcctAmount3:INT,AccountType4:INT,FeeType4:INT,ChargeAmount4:INT,CurrentAcctAmount4:INT,AccountType5:INT,FeeType5:INT,ChargeAmount5:INT,CurrentAcctAmount5:INT,AccountType6:INT,FeeType6:INT,ChargeAmount6:INT,CurrentAcctAmount6:INT,AccountType7:INT,FeeType7:INT,ChargeAmount7:INT,CurrentAcctAmount7:INT,AccountType8:INT,FeeType8:INT,ChargeAmount8:INT,CurrentAcctAmount8:INT,AccountType9:INT,FeeType9:INT,ChargeAmount9:INT,CurrentAcctAmount9:INT,AccountType10:INT,FeeType10:INT,ChargeAmount10:INT,CurrentAcctAmount10:INT,BonusValidity1:STRING,BonusValidity2:STRING,BonusValidity3:STRING,BonusValidity4:STRING,BonusValidity5:STRING,BonusValidity6:STRING,BonusValidity7:STRING,BonusValidity8:STRING,BonusValidity9:STRING,BonusValidity10:STRING,OnlineType:INT,Reserved10:STRING,Reserved11:STRING,Reserved12:STRING,AddtionalInfo:STRING,BearerType:INT,StartTime:STRING,StopTime:STRING,BearerProtocolType:INT,ChargingID:STRING,ServiceLevel:INT,StartTimeOfBillCycle:STRING,ChargePartyIndicator:STRING,SecondChargingPartyNumber:STRING,SecondChargingPartyPayType:STRING,SecondChargingPartyBillCycleID:STRING,CallingVPNTopGroupNumber:STRING,CallingVPNGroupNumber:STRING,CallingVPNShortNumber:STRING,CalledVPNTopGroupNumber:STRING,CalledVPNGroupNumber:STRING,CalledVPNShortNumber:STRING,CallingNetworkType:STRING,CalledNetworkType:STRING,GroupCallType:STRING,GroupPayFlag:STRING,Reserved13:STRING,Reserved14:STRING,Reserved15:STRING,Reserved16:STRING,Reserved17:STRING,ChargeOfFluxSecondAccounts:INT,ChargeOfDurationSecondAccounts:INT,ChargeOfFundSecondAccounts:INT,ChargeFromSecondPrepaidAccount:INT,SecondPrepaidAccountBalance:INT,ChargeFromSecondPostpaidAccount:INT,SecondPostpaidAccountBalance:INT,SecondAccountID:STRING,SecondAccountKey:INT,SecondAccountCurrencyCode:INT,SecondAccountType1:INT,SecondFeeType1:INT,SecondChargeAmount1:INT,SecondCurrentAcctAmount1:INT,SecondAccountType2:INT,SecondFeeType2:INT,SecondChargeAmount2:INT,SecondCurrentAcctAmount2:INT,SecondAccountType3:INT,SecondFeeType3:INT,SecondChargeAmount3:INT,SecondCurrentAcctAmount3:INT,SecondAccountType4:INT,SecondFeeType4:INT,SecondChargeAmount4:INT,SecondCurrentAcctAmount4:INT,SecondAccountType5:INT,SecondFeeType5:INT,SecondChargeAmount5:INT,SecondCurrentAcctAmount5:INT,SecondBonusValidity1:STRING,SecondBonusValidity2:STRING,SecondBonusValidity3:STRING,SecondBonusValidity4:STRING,SecondBonusValidity5:STRING,SubscriberIDType:STRING,subscriberkey:INT,SecondSubscriberKey:INT,CustomerKey1:INT,CustomerCode1:STRING,CustomerKey2:INT,CustomerCode2:STRING,TenantID:INT"
  val CREATE_COVERAGE = "CREATE TABLE IF NOT EXISTS  COVERAGE(LATITUDE DECIMAL(9,7),LONGITUDE DECIMAL(9,7),VALUE DECIMAL(9,3),CELLTYPE STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY \"|\""

  val COVERAGE_SCHEMA = "LATITUDE:DECIMAL,LONGITUDE:DECIMAL,VALUE:DECIMAL,CELLTYPE:STRING"
  val CDRS_SCHEMA = "SerialNo:INT,SubSequence:INT,MyTimeStamp:STRING,ServiceKey:INT,CallingPartyNumber:STRING,APN:STRING,URL:STRING,CallingPartyIMSI:STRING,AccessNetworkAddress:STRING,GGSNAddress:STRING,CallingRoamInfo:STRING,CallingCellID:STRING,TimeStampOfSGSN:STRING,TimeZoneOfSGSN:INT,Reserved0:STRING,ChargingTime:STRING,TotalFlux:INT,UpFlux:INT,DownFlux:INT,ElapseDuration:INT,TerminationReason:INT,IMEI:STRING,TransitionID:STRING,ServiceID:STRING,SPID:STRING,CategoryID:STRING,ContentID:STRING,QoS:STRING,OriginalNetworkType:STRING,DiameterSessionID:STRING,BrandID:INT,SubCOSID:INT,ChargingPartyNumber:STRING,PayType:STRING,BillCycleID:STRING,ChargingType:STRING,ResultCode:INT,RoamState:STRING,CallingHomeCountryCode:INT,CallingHomeAreaNumber:INT,CallingHomeNetworkCode:INT,CallingRoamCountryCode:INT,CallingRoamAreaNumber:INT,CallingRoamNetworkCode:INT,Reserved1:STRING,Reserved2:STRING,Reserved3:STRING,Reserved4:STRING,Reserved5:STRING,Reserved6:STRING,ProductID:STRING,ServiceType:STRING,Reserved7:STRING,HomeZoneID:INT,UserState:STRING,SubscriberID:STRING,ChargeDuration:INT,TotalChargeFlux:INT,Reserved8:STRING,Reserved9:STRING,ChargeOfFluxAccounts:INT,ChargeOfDurationAccounts:INT,ChargeOfFundAccounts:INT,ChargeFromPrepaid:INT,PrepaidBalance:INT,ChargeFromPostpaid:INT,PostPaidBalance:INT,AccountID:STRING,AccountKey:INT,CurrencyCode:INT,AccountType1:INT,FeeType1:INT,ChargeAmount1:INT,CurrentAcctAmount1:INT,AccountType2:INT,FeeType2:INT,ChargeAmount2:INT,CurrentAcctAmount2:INT,AccountType3:INT,FeeType3:INT,ChargeAmount3:INT,CurrentAcctAmount3:INT,AccountType4:INT,FeeType4:INT,ChargeAmount4:INT,CurrentAcctAmount4:INT,AccountType5:INT,FeeType5:INT,ChargeAmount5:INT,CurrentAcctAmount5:INT,AccountType6:INT,FeeType6:INT,ChargeAmount6:INT,CurrentAcctAmount6:INT,AccountType7:INT,FeeType7:INT,ChargeAmount7:INT,CurrentAcctAmount7:INT,AccountType8:INT,FeeType8:INT,ChargeAmount8:INT,CurrentAcctAmount8:INT,AccountType9:INT,FeeType9:INT,ChargeAmount9:INT,CurrentAcctAmount9:INT,AccountType10:INT,FeeType10:INT,ChargeAmount10:INT,CurrentAcctAmount10:INT,BonusValidity1:STRING,BonusValidity2:STRING,BonusValidity3:STRING,BonusValidity4:STRING,BonusValidity5:STRING,BonusValidity6:STRING,BonusValidity7:STRING,BonusValidity8:STRING,BonusValidity9:STRING,BonusValidity10:STRING,OnlineType:INT,Reserved10:STRING,Reserved11:STRING,Reserved12:STRING,AddtionalInfo:STRING,BearerType:INT,StartTime:STRING,StopTime:STRING,BearerProtocolType:INT,ChargingID:STRING,ServiceLevel:INT,StartTimeOfBillCycle:STRING,ChargePartyIndicator:STRING,SecondChargingPartyNumber:STRING,SecondChargingPartyPayType:STRING,SecondChargingPartyBillCycleID:STRING,CallingVPNTopGroupNumber:STRING,CallingVPNGroupNumber:STRING,CallingVPNShortNumber:STRING,CalledVPNTopGroupNumber:STRING,CalledVPNGroupNumber:STRING,CalledVPNShortNumber:STRING,CallingNetworkType:STRING,CalledNetworkType:STRING,GroupCallType:STRING,GroupPayFlag:STRING,Reserved13:STRING,Reserved14:STRING,Reserved15:STRING,Reserved16:STRING,Reserved17:STRING,ChargeOfFluxSecondAccounts:INT,ChargeOfDurationSecondAccounts:INT,ChargeOfFundSecondAccounts:INT,ChargeFromSecondPrepaidAccount:INT,SecondPrepaidAccountBalance:INT,ChargeFromSecondPostpaidAccount:INT,SecondPostpaidAccountBalance:INT,SecondAccountID:STRING,SecondAccountKey:INT,SecondAccountCurrencyCode:INT,SecondAccountType1:INT,SecondFeeType1:INT,SecondChargeAmount1:INT,SecondCurrentAcctAmount1:INT,SecondAccountType2:INT,SecondFeeType2:INT,SecondChargeAmount2:INT,SecondCurrentAcctAmount2:INT,SecondAccountType3:INT,SecondFeeType3:INT,SecondChargeAmount3:INT,SecondCurrentAcctAmount3:INT,SecondAccountType4:INT,SecondFeeType4:INT,SecondChargeAmount4:INT,SecondCurrentAcctAmount4:INT,SecondAccountType5:INT,SecondFeeType5:INT,SecondChargeAmount5:INT,SecondCurrentAcctAmount5:INT,SecondBonusValidity1:STRING,SecondBonusValidity2:STRING,SecondBonusValidity3:STRING,SecondBonusValidity4:STRING,SecondBonusValidity5:STRING,SubscriberIDType:STRING,subscriberkey:INT,SecondSubscriberKey:INT,CustomerKey1:INT,CustomerCode1:STRING,CustomerKey2:INT,CustomerCode2:STRING,TenantID:INT"
  val NMS_SCHEMA = "NEName:STRING,MeasInfoId:INT,Counter:STRING,SiteName:STRING,CellID:INT,CellName:STRING,Value:INT,MyTimeStamp:STRING"
  val CELLS_SCHEMA = "NEName:STRING,SiteName:STRING,CellName:STRING,ID:STRING,LAC:STRING,RAC:STRING,LATITUDE:DECIMAL,LONGITUDE:DECIMAL,CELLID:STRING,CELLTYPE:STRING"


  def addPartition(): String = {
    " PARTITIONED BY (ystr STRING, ymstr STRING, ymdstr STRING)"
  }

  def createUICDRS(date: String): String = {
    "CREATE TABLE IF NOT EXISTS ui.cdr_" + date + "(" +
      "  latitude decimal(9,7)," +
      "  longitude decimal(9,7)," +
      "  mytimestamp string," +
      "  callingpartynumber string," +
      "  productid string," +
      "  downflux int," +
      "  upflux int," +
      "  elapseduration int," +
      "  cellid string" +
      " ) ROW FORMAT DELIMITED FIELDS TERMINATED BY \"|\""
  }

  def createUINMS3G(date: String): String = {
    "CREATE TABLE IF NOT EXISTS ui.nms_3g_" + date + "(" +
      "  latitude decimal(9,7)," +
      "  longitude decimal(9,7), " +
      "  cellid string ," +
      "  counter string," +
      "  value int ," +
      "  mytimestamp string" +
      " ) ROW FORMAT DELIMITED FIELDS TERMINATED BY \"|\""
  }

  def createUINMS3GTH(date: String): String = {
    "CREATE TABLE IF NOT EXISTS ui.nms_3g_th_" + date + "(" +
      "  latitude decimal(9,7)," +
      "  longitude decimal(9,7), " +
      "  cellid string ," +
      "  counter string," +
      "  value int ," +
      "  mytimestamp string" +
      " ) ROW FORMAT DELIMITED FIELDS TERMINATED BY \"|\""
  }

  def createUINMS2G(date: String): String = {
    "CREATE TABLE IF NOT EXISTS ui.nms_2g_" + date + "(" +
      "  latitude decimal(9,7)," +
      "  longitude decimal(9,7), " +
      "  cellid string ," +
      "  counter string," +
      "  value int ," +
      "  mytimestamp string" +
      " ) ROW FORMAT DELIMITED FIELDS TERMINATED BY \"|\""
  }

  def createDailyCDRS(date: String): String = {
    "CREATE TABLE cdr_" + date + " AS " +
      "select c.latitude,c.longitude,cd.mytimestamp,cd.callingpartynumber,cd.productid,cd.downflux,cd.upflux,cd.elapseduration  " +
      "from cdrs cd,cells c  where cd.callingcellid=c.cellid and cd.mytimestamp like '" + date + "%'"
  }

  def createDailyNMS3G(date: String): String = {
    "CREATE TABLE nms_3g_" + date + " AS " +
      "select c.latitude,c.longitude,c.cellid,n.counter,n.value,n.mytimestamp " +
      "from nms_3g n,cells c  " +
      "where n.cellid=c.id  AND ( n.counter = '67179778' OR  n.counter = '67179779' ) " +
      "AND n.mytimestamp like '" + date + "%'"
  }


  def createDailyNMS2G(date: String): String = {
    "CREATE TABLE nms_2g_" + date + " AS " +
      "select c.latitude,c.longitude,c.cellid,n.counter,n.value,n.mytimestamp " +
      "from nms_2g n,cells c  " +
      "where n.cellname=c.cellname and n.nename=c.nename " +
      "AND (n.counter = '1278072498' OR  n.counter = '1278087432'" +
      " OR  n.counter = '1278078459' OR  n.counter = '1278080467'" +
      " OR  n.counter = '1278079528' OR  n.counter = '1278081557'" +
      " OR  n.counter = '1278082436')" +
      " AND n.mytimestamp like '" + date + "%'"
  }

  def createDailyTMPCDRS(date: String): String = {
    "CREATE TABLE IF NOT EXISTS cdr_" + date + "(" +
      "  latitude decimal(9,7)," +
      "  longitude decimal(9,7)," +
      "  mytimestamp string," +
      "  callingpartynumber string," +
      "  productid string," +
      "  downflux int," +
      "  upflux int," +
      "  elapseduration int" +
      " )"
  }

  def insertDailyTMPCDRS(date: String): String = {
    " INSERT INTO TABLE cdr_" + date +
      " select c.latitude,c.longitude,cd.mytimestamp,cd.callingpartynumber,cd.productid,cd.downflux,cd.upflux,cd.elapseduration" +
      " from cdrs_tmp cd,cells c" +
      " where cd.callingcellid=c.cellid" +
      " and cd.mytimestamp like '" + date + "%'"
  }

  def createDailyTMPNMS2G(date: String): String = {
    "CREATE TABLE IF NOT EXISTS nms_2g_" + date + "(" +
      "  latitude decimal(9,7)," +
      "  longitude decimal(9,7), " +
      "  cellid string ," +
      "  counter string," +
      "  value int ," +
      "  mytimestamp string" +
      " )"
  }

  def insertDailyTMPNMS2G(date: String): String = {
    "INSERT INTO TABLE nms_2g_" + date +
      " select c.latitude,c.longitude,c.cellid,n.counter,n.value,n.mytimestamp" +
      " from nms_tmp n,cells c " +
      " where n.cellname=c.cellname and n.nename=c.nename " +
      " AND (n.counter = '1278072498' " +
      " OR  n.counter = '1278087432' " +
      " OR  n.counter = '1278078459' " +
      " OR  n.counter = '1278080467' " +
      " OR  n.counter = '1278079528' " +
      " OR  n.counter = '1278081557' " +
      " OR  n.counter = '1278082436') " +
      "AND n.mytimestamp like '" + date + "%'"
  }

  def createDailyTMPNMS3G(date: String): String = {
    "CREATE TABLE IF NOT EXISTS nms_3g_" + date + "(" +
      "  latitude decimal(9,7)," +
      "  longitude decimal(9,7), " +
      "  cellid string ," +
      "  counter string," +
      "  value int ," +
      "  mytimestamp string" +
      " )"
  }

  def insertDailyTMPNMS3G(date: String): String = {

    "INSERT INTO TABLE nms_3g_" + date +
      " select c.latitude,c.longitude,c.cellid,n.counter,n.value,n.mytimestamp" +
      " from nms_tmp n,cells c " +
      " where counter='67179778'" +
      " OR counter='67179779'" +
      "AND n.mytimestamp like '" + date + "%'"
  }

  def createNMS3G(): String = {
    "create table nms_3g as" +
      " select *" +
      " from nms" +
      " where counter='67179778'" +
      " OR counter='67179779'"
  }

  def createNMS2G(): String = {
    "create table nms_2g as" +
      " select *" +
      " from nms" +
      " where counter = '1278072498'" +
      " OR  counter = '1278087432'" +
      " OR  counter = '1278078459'" +
      " OR  counter = '1278080467'" +
      " OR  counter = '1278079528'" +
      " OR  counter = '1278081557'" +
      " OR  counter = '1278082436'"
  }

  def dropDailyNMS2G(date: String): String = {
    "DROP TABLE IF EXISTS `default`.`nms_2g_" + date + "`"
  }

  def dropDailyNMS3G(date: String): String = {
    "DROP TABLE IF EXISTS `default`.`nms_3g_" + date + "`"
  }

  def dropDailyCDRS(date: String): String = {
    "DROP TABLE IF EXISTS `default`.`cdr_" + date + "`"
  }

  def dropNMSTMP(): String = {
    "DROP TABLE IF EXISTS `default`.`nms_tmp`"
  }

  def copyCRDTMPTOMAIN(): String = {
    "insert into table cdrs" +
      " select *  " +
      " from cdrs_tmp"
  }

  def copyNMSTMPTOMAIN(): String = {
    "insert into table nms" +
      " select *  " +
      " from nms_tmp"
  }

  def dropCDRTMP(): String = {
    "DROP TABLE IF EXISTS `default`.`cdrs_tmp`"
  }

  def dropNMS2G(): String = {
    "DROP TABLE IF EXISTS `default`.`nms_2g`"
  }

  def dropNMS3G(): String = {
    "DROP TABLE IF EXISTS `default`.`nms_3g`"
  }

}
