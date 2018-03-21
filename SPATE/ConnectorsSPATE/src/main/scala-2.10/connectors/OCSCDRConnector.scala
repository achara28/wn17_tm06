package connectors

object OCSCDRConnector extends Connector {

  def parse(input:String): OCSCDRObject = {
    if (input.length <= 0)
      return null

    val arr = input.split("\\|")
    val ocs: OCSCDRObject = {
      new OCSCDRObject(arr(0).toLong,
        arr(2),
        arr(4),
        arr(7),
        arr(11),
        arr(12),
        arr(15),
        arr(16).toInt,
        arr(17).toInt,
        arr(18).toInt,
        arr(19).toInt,
        arr(21),
        arr(25).toInt,
        arr(32),
        arr(38),
        arr(41),
        arr(50).toInt,
        arr(55).toInt)
    }
    //Return the ocs-cdr object
    ocs
  }


def main(args: Array[String]) = {

  val input = "9097192334603278734|0|20150626152257|1|96890307|internet||96001827|213.207.137.11||28010|280100024124291|20120812000000|0||20100628000000|9594582|11892964|5605103|477|0|35708205816255||-1|-1|428||05-1b931f719697fe442b404000|2|ggsn4;3642837544;18794;1081|306|614429|96379993|2|20150601|1|0|0|357|1|1|357|1|1|||||||1559|3||0|1100000|305422137|0|28269246|||28269246|0|0|0|3419000|0|5695600|410210449|410210449|0|5007|335|28269246|435962064|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|||||||||||0|||0||0|2.01506E+13|2.01506E+13|0|0|0|2.01506E+13|1|||||||||||||0||||||0|0|0|0|0|0|0||0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0||||||0|410208446|0|410210053|410210053|||100035700"
  val ocs = parse(input)

  println(ocs.toString)
}
}