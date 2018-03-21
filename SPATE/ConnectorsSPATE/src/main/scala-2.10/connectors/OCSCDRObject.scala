package connectors

/**
 * Created by Costantinos on 25/6/2015.
 */
class OCSCDRObject(var serialNo: Long,
                   var timeStamp: String,
                   var callingNumber: String,
                   var callingCell: String,
                   var callingPartyIMSI: String,
                   var timeStampOfSGSN: String,
                   var chargingTime: String,
                   //Get muximum total data
                   var totalFlux: Int, var upFlux: Int, var downFlux: Int,
                   var elapseDuration: Int, var imei: String, var categoryID: Int,
                   var chargingPartyNumber: String,
                   var callingHomeCountryCode: String, var callingRoamCountryCode: String,
                   var productID: Int,
                   var subscriberID: Int) extends  ConnectorObject{

  override def toString: String = {
    serialNo + ":" +
      timeStamp + ":" +
      callingNumber + ":" +
      callingPartyIMSI + ":" +
      callingCell + ":" +
      timeStampOfSGSN + ":" +
      chargingTime + ":" +
      //Get muximum total data
      totalFlux + ":" + upFlux + ":" + downFlux + ":" +
      elapseDuration + ":" + imei + ":" + categoryID + ":" +
      chargingPartyNumber + ":" +
      callingHomeCountryCode + ":" + callingRoamCountryCode + ":" +
      productID + ":" +
      subscriberID
  }
}
