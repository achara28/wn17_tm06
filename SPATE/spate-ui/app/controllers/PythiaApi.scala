package controllers

import java.util.concurrent.TimeoutException

import controllers.UserManagement._
import play.api.mvc.Controller
import play.api.libs.json._
import utilities.actions.AuthenticatedJsonAction
import utilities.{WSHelper, JsonReply, Utils}
import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

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
object PythiaApi extends Controller {

  def CellTowers() = AuthenticatedJsonAction { authenticatedRequest =>
    try {
      val url = Utils.getPythiaUrl.get + "/api/cellTowers"
      val authHeader = authenticatedRequest.user.token.get.authToken
      val request = WSHelper.requestWithAuthHeader(url, authHeader)
      val futureResponse = request.get().map {
        response => response.json
      }
      val response = Await.result(futureResponse, 8.seconds)
      Ok(response)
    }
    catch {
      case te: TimeoutException =>
        RequestTimeout(JsonReply.error("request timed out"))
    }
  }


  def GetSize() = AuthenticatedJsonAction { authenticatedRequest =>
    try {
      val url = Utils.getPythiaUrl.get + "/api/getSize"
      val authHeader = authenticatedRequest.user.token.get.authToken
      val request = WSHelper.requestWithAuthHeader(url, authHeader)
      val futureResponse = request.get().map {
        response => response.json
      }
      val response = Await.result(futureResponse, 8.seconds)
      Ok(response)
    }
    catch {
      case te: TimeoutException =>
        RequestTimeout(JsonReply.error("request timed out"))
    }
  }


  def UserJobs() = AuthenticatedJsonAction { authenticatedRequest =>
    try {
      val url = Utils.getPythiaUrl.get + "/user/jobs"
      val authHeader = authenticatedRequest.user.token.get.authToken
      val request = WSHelper.requestWithAuthHeader(url, authHeader)
      val futureResponse = request.get().map {
        response => response.json
      }
      val response = Await.result(futureResponse, 5.seconds)
      Ok(response)
    }
    catch {
      case te: TimeoutException =>
        RequestTimeout(JsonReply.error("request timed out"))

    }
  }

  def JobsInfo() = AuthenticatedJsonAction { authenticatedRequest =>
    val pythiaUrl = Utils.getPythiaUrl
    if (pythiaUrl == None) {
      ServiceUnavailable(JsonReply.error("Pythia service is currently unavailable."))
    }
    else {
      authenticatedRequest.request.body.asJson.map { json =>
        json.validate[List[String]].map {
          case jobIds: List[String] =>
            try {
              val authHeader = authenticatedRequest.user.token.get.authToken
              val jobsInfo = mutable.MutableList[JsValue]()
              jobIds.foreach { jobId =>
                val url = pythiaUrl.get + "/api/job/" + jobId
                val req = WSHelper.requestWithAuthHeader(url, authHeader)
                val futureResponse = req.get()
                val resp = Await.result(futureResponse, 5.seconds)
                jobsInfo += resp.json
              }
              Ok(Json.toJson(jobsInfo))
            }
            catch {
              case te: TimeoutException =>
                RequestTimeout(JsonReply.error("request timed out"))
            }
        }.recoverTotal {
          case e => BadRequest(JsonReply.error("expected a json array"))
        }
      }.getOrElse {
        BadRequest(authenticatedRequest.request.toString())
      }
    }
  }


  def MovingBoxes() = AuthenticatedJsonAction { authenticatedRequest =>
    try {
      val url = Utils.getPythiaUrl.get + "/api/queries/moving"
      val authHeader = authenticatedRequest.user.token.get.authToken
      val request = WSHelper.requestWithAuthHeader(url, authHeader)
      val futureResponse = request.get().map {
        response => response.json
      }
      val response = Await.result(futureResponse, 8.seconds)
      Ok(response)
    }
    catch {
      case te: TimeoutException =>
        RequestTimeout(JsonReply.error("request timed out"))
    }
  }


  def MovedCDR() = AuthenticatedJsonAction { authenticatedRequest =>
    val pythiaUrl = Utils.getPythiaUrl

    if (pythiaUrl == None) {
      ServiceUnavailable(JsonReply.error("Pythia service is currently unavailable."))
    }
    else {
      authenticatedRequest.request.body.asJson.map { json =>
        json.validate[Map[String, String]].map {
          case startend: Map[String, String] =>
            try {
              val authHeader = authenticatedRequest.user.token.get.authToken
              val jobsInfo = mutable.MutableList[JsValue]()

              val start = startend.getOrElse("start", "")
              val end = startend.getOrElse("end", "")

              if (start.isEmpty || end.isEmpty) {
                BadRequest(JsonReply.error("expected a start and end value"))
              } else {
                val url = pythiaUrl.get + "/api/queries/cdrmoved/" + start.toString + "/" + end.toString
                val req = WSHelper.requestWithAuthHeader(url, authHeader)
                val futureResponse = req.get()
                val resp = Await.result(futureResponse, 5.seconds)
                Ok(resp.json)
              }
            }
            catch {
              case te: TimeoutException =>
                RequestTimeout(JsonReply.error("expected timed out"))
            }
        }.recoverTotal {
          case e => BadRequest(JsonReply.error("Except two values"))
        }
      }.getOrElse {
        BadRequest(authenticatedRequest.request.toString())
      }
    }
  }


  def HistoryCDR() = AuthenticatedJsonAction { authenticatedRequest =>
    val pythiaUrl = Utils.getPythiaUrl

    if (pythiaUrl == None) {
      ServiceUnavailable(JsonReply.error("Pythia service is currently unavailable."))
    }
    else {
      authenticatedRequest.request.body.asJson.map { json =>
        json.validate[Map[String, String]].map {
          case startend: Map[String, String] =>
            try {
              val authHeader = authenticatedRequest.user.token.get.authToken
              val jobsInfo = mutable.MutableList[JsValue]()

              val start = startend.getOrElse("start", "")
              val end = startend.getOrElse("end", "")

              if (start.isEmpty || end.isEmpty) {
                BadRequest(JsonReply.error("expected a start and end value"))
              } else {
                val url = pythiaUrl.get + "/api/cdr/" + start.toString + "/" + end.toString
                val req = WSHelper.requestWithAuthHeader(url, authHeader)
                val futureResponse = req.get()
                val resp = Await.result(futureResponse, 30.seconds)
                Ok(resp.json)
              }
            }
            catch {
              case te: TimeoutException =>
                RequestTimeout(JsonReply.error("expected timed out"))
            }
        }.recoverTotal {
          case e => BadRequest(JsonReply.error("Except two values"))
        }
      }.getOrElse {
        BadRequest(authenticatedRequest.request.toString())
      }
    }
  }


  def HistoryNMS() = AuthenticatedJsonAction { authenticatedRequest =>
    val pythiaUrl = Utils.getPythiaUrl

    if (pythiaUrl == None) {
      ServiceUnavailable(JsonReply.error("Pythia service is currently unavailable."))
    }
    else {
      authenticatedRequest.request.body.asJson.map { json =>
        json.validate[Map[String, String]].map {
          case startend: Map[String, String] =>
            try {
              val authHeader = authenticatedRequest.user.token.get.authToken
              val jobsInfo = mutable.MutableList[JsValue]()

              val start = startend.getOrElse("start", "")
              val end = startend.getOrElse("end", "")

              if (start.isEmpty || end.isEmpty) {
                BadRequest(JsonReply.error("expected a start and end value"))
              } else {
                val url = pythiaUrl.get + "/api/nms/" + start.toString + "/" + end.toString
                val req = WSHelper.requestWithAuthHeader(url, authHeader)
                val futureResponse = req.get()
                val resp = Await.result(futureResponse, 5.seconds)
                Ok(resp.json)
              }
            }
            catch {
              case te: TimeoutException =>
                RequestTimeout(JsonReply.error("expected timed out"))
            }
        }.recoverTotal {
          case e => BadRequest(JsonReply.error("Except two values"))
        }
      }.getOrElse {
        BadRequest(authenticatedRequest.request.toString())
      }
    }
  }


  def get2g() = AuthenticatedJsonAction { authenticatedRequest =>
    try {
      val url = Utils.getPythiaUrl.get + "/api/2g"
      val authHeader = authenticatedRequest.user.token.get.authToken
      val request = WSHelper.requestWithAuthHeader(url, authHeader)
      val futureResponse = request.get().map {
        response => response.json
      }
      val response = Await.result(futureResponse, 8.seconds)
      Ok(response)
    }
    catch {
      case te: TimeoutException =>
        RequestTimeout(JsonReply.error("request timed out"))
    }
  }

  def get4g() = AuthenticatedJsonAction { authenticatedRequest =>
    try {
      val url = Utils.getPythiaUrl.get + "/api/4g"
      val authHeader = authenticatedRequest.user.token.get.authToken
      val request = WSHelper.requestWithAuthHeader(url, authHeader)
      val futureResponse = request.get().map {
        response => response.json
      }
      val response = Await.result(futureResponse, 8.seconds)
      Ok(response)
    }
    catch {
      case te: TimeoutException =>
        RequestTimeout(JsonReply.error("request timed out"))
    }
  }

  def get3g() = AuthenticatedJsonAction { authenticatedRequest =>
    try {
      val url = Utils.getPythiaUrl.get + "/api/3g"
      val authHeader = authenticatedRequest.user.token.get.authToken
      val request = WSHelper.requestWithAuthHeader(url, authHeader)
      val futureResponse = request.get().map {
        response => response.json
      }
      val response = Await.result(futureResponse, 8.seconds)
      Ok(response)
    }
    catch {
      case te: TimeoutException =>
        RequestTimeout(JsonReply.error("request timed out"))
    }
  }

  def get4gl(x: String, y: String, z: String) = AuthenticatedJsonAction { authenticatedRequest =>
    try {
      val url = Utils.getPythiaUrl.get + "/api/4gl/" + x + "/" + y + "/" + z
      val authHeader = authenticatedRequest.user.token.get.authToken
      val request = WSHelper.requestWithAuthHeader(url, authHeader)
      val futureResponse = request.get().map {
        response => response.json
      }
      val response = Await.result(futureResponse, 8.seconds)
      Ok(response)
    }
    catch {
      case te: TimeoutException =>
        RequestTimeout(JsonReply.error("request timed out"))
    }
  }

  def get3gl(x: String, y: String, z: String) = AuthenticatedJsonAction { authenticatedRequest =>
    try {
      val url = Utils.getPythiaUrl.get + "/api/3gl/" + x + "/" + y + "/" + z
      val authHeader = authenticatedRequest.user.token.get.authToken
      val request = WSHelper.requestWithAuthHeader(url, authHeader)
      val futureResponse = request.get().map {
        response => response.json
      }
      val response = Await.result(futureResponse, 8.seconds)
      Ok(response)
    }
    catch {
      case te: TimeoutException =>
        RequestTimeout(JsonReply.error("request timed out"))
    }
  }

  def get2gl(x: String, y: String, z: String) = AuthenticatedJsonAction { authenticatedRequest =>
    try {
      val url = Utils.getPythiaUrl.get + "/api/2gl/" + x + "/" + y + "/" + z
      val authHeader = authenticatedRequest.user.token.get.authToken
      val request = WSHelper.requestWithAuthHeader(url, authHeader)
      val futureResponse = request.get().map {
        response => response.json
      }
      val response = Await.result(futureResponse, 8.seconds)
      Ok(response)
    }
    catch {
      case te: TimeoutException =>
        RequestTimeout(JsonReply.error("request timed out"))
    }
  }

  def HistoryCDRPerDay() = AuthenticatedJsonAction { authenticatedRequest =>
    val pythiaUrl = Utils.getPythiaUrl

    if (pythiaUrl == None) {
      ServiceUnavailable(JsonReply.error("Pythia service is currently unavailable."))
    }
    else {
      authenticatedRequest.request.body.asJson.map { json =>
        json.validate[Map[String, String]].map {
          case dates: Map[String, String] =>
            try {
              val authHeader = authenticatedRequest.user.token.get.authToken
              val date = dates.getOrElse("date", "")

              if (date.isEmpty) {
                BadRequest(JsonReply.error("expected a date value"))
              } else {
                val url = pythiaUrl.get + "/api/cdr/" + date.toString
                //val url="http://date.jsontest.com/"
                val req = WSHelper.requestWithAuthHeader(url, authHeader)
                val futureResponse = req.get()
                val resp = Await.result(futureResponse, 300.seconds)

                Ok(resp.json)
              }
            }
            catch {
              case te: TimeoutException =>
                RequestTimeout(JsonReply.error("expected timed out:" + te.getLocalizedMessage))
            }
        }.recoverTotal {
          case e => BadRequest(JsonReply.error("Except two values"))
        }
      }.getOrElse {
        BadRequest(authenticatedRequest.request.toString())
      }
    }
  }


  def HistoryCDRDT() = AuthenticatedJsonAction { authenticatedRequest =>
    val pythiaUrl = Utils.getPythiaUrl

    if (pythiaUrl == None) {
      ServiceUnavailable(JsonReply.error("Pythia service is currently unavailable."))
    }
    else {
      authenticatedRequest.request.body.asJson.map { json =>
        json.validate[Map[String, String]].map {
          case dates: Map[String, String] =>
            try {
              val authHeader = authenticatedRequest.user.token.get.authToken
              val date = dates.getOrElse("date", "")
              val start = dates.getOrElse("start", "")
              val end = dates.getOrElse("end", "")
              if (date.isEmpty || start.isEmpty || end.isEmpty) {
                BadRequest(JsonReply.error("expected a date value"))
              } else {
                val url = pythiaUrl.get + "/api/tcdr/" + date.toString + "/" + start.toString + "/" + end.toString
                //val url="http://date.jsontest.com/"
                val req = WSHelper.requestWithAuthHeader(url, authHeader)
                val futureResponse = req.get()
                val resp = Await.result(futureResponse, 300.seconds)

                Ok(resp.json)
              }
            }
            catch {
              case te: TimeoutException =>
                RequestTimeout(JsonReply.error("expected timed out:" + te.getLocalizedMessage))
            }
        }.recoverTotal {
          case e => BadRequest(JsonReply.error("Except two values"))
        }
      }.getOrElse {
        BadRequest(authenticatedRequest.request.toString())
      }
    }
  }

  def HistoryCDRDTS() = AuthenticatedJsonAction { authenticatedRequest =>
    val pythiaUrl = Utils.getPythiaUrl

    if (pythiaUrl == None) {
      ServiceUnavailable(JsonReply.error("Pythia service is currently unavailable."))
    }
    else {
      authenticatedRequest.request.body.asJson.map { json =>
        json.validate[Map[String, String]].map {
          case dates: Map[String, String] =>
            try {
              val authHeader = authenticatedRequest.user.token.get.authToken
              val date = dates.getOrElse("date", "")
              val start = dates.getOrElse("start", "")
              val end = dates.getOrElse("end", "")
              val callingpartynumber = dates.getOrElse("callingpartynumber", "")
              if (date.isEmpty || start.isEmpty || end.isEmpty || callingpartynumber.isEmpty) {
                BadRequest(JsonReply.error("expected a date value"))
              } else {
                val url = pythiaUrl.get + "/api/tscdr/" + date.toString + "/" + start.toString + "/" + end.toString + "/" + callingpartynumber.toString
                //val url="http://date.jsontest.com/"
                val req = WSHelper.requestWithAuthHeader(url, authHeader)
                val futureResponse = req.get()
                val resp = Await.result(futureResponse, 300.seconds)

                Ok(resp.json)
              }
            }
            catch {
              case te: TimeoutException =>
                RequestTimeout(JsonReply.error("expected timed out:" + te.getLocalizedMessage))
            }
        }.recoverTotal {
          case e => BadRequest(JsonReply.error("Except two values"))
        }
      }.getOrElse {
        BadRequest(authenticatedRequest.request.toString())
      }
    }
  }


  def HistoryCDRDTP() = AuthenticatedJsonAction { authenticatedRequest =>
    val pythiaUrl = Utils.getPythiaUrl

    if (pythiaUrl == None) {
      ServiceUnavailable(JsonReply.error("Pythia service is currently unavailable."))
    }
    else {
      authenticatedRequest.request.body.asJson.map { json =>
        json.validate[Map[String, String]].map {
          case dates: Map[String, String] =>
            try {
              val authHeader = authenticatedRequest.user.token.get.authToken
              val date = dates.getOrElse("date", "")
              val start = dates.getOrElse("start", "")
              val end = dates.getOrElse("end", "")
              val productid = dates.getOrElse("productid", "")
              if (date.isEmpty || start.isEmpty || end.isEmpty || productid.isEmpty) {
                BadRequest(JsonReply.error("expected a date value"))
              } else {
                val url = pythiaUrl.get + "/api/tpcdr/" + date.toString + "/" + start.toString + "/" + end.toString + "/" + productid.toString
                //val url="http://date.jsontest.com/"
                val req = WSHelper.requestWithAuthHeader(url, authHeader)
                val futureResponse = req.get()
                val resp = Await.result(futureResponse, 300.seconds)

                Ok(resp.json)
              }
            }
            catch {
              case te: TimeoutException =>
                RequestTimeout(JsonReply.error("expected timed out:" + te.getLocalizedMessage))
            }
        }.recoverTotal {
          case e => BadRequest(JsonReply.error("Except two values"))
        }
      }.getOrElse {
        BadRequest(authenticatedRequest.request.toString())
      }
    }
  }


  def HistoryCDRDTSP() = AuthenticatedJsonAction { authenticatedRequest =>
    val pythiaUrl = Utils.getPythiaUrl

    if (pythiaUrl == None) {
      ServiceUnavailable(JsonReply.error("Pythia service is currently unavailable."))
    }
    else {
      authenticatedRequest.request.body.asJson.map { json =>
        json.validate[Map[String, String]].map {
          case dates: Map[String, String] =>
            try {
              val authHeader = authenticatedRequest.user.token.get.authToken
              val date = dates.getOrElse("date", "")
              val start = dates.getOrElse("start", "")
              val end = dates.getOrElse("end", "")
              val callingpartynumber = dates.getOrElse("callingpartynumber", "")
              val productid = dates.getOrElse("productid", "")
              if (date.isEmpty || start.isEmpty || end.isEmpty || callingpartynumber.isEmpty || productid.isEmpty) {
                BadRequest(JsonReply.error("expected a date value"))
              } else {
                val url = pythiaUrl.get + "/api/tspcdr/" + date.toString + "/" + start.toString + "/" + end.toString + "/" + callingpartynumber.toString + "/" + productid.toString
                //val url="http://date.jsontest.com/"
                val req = WSHelper.requestWithAuthHeader(url, authHeader)
                val futureResponse = req.get()
                val resp = Await.result(futureResponse, 300.seconds)

                Ok(resp.json)
              }
            }
            catch {
              case te: TimeoutException =>
                RequestTimeout(JsonReply.error("expected timed out:" + te.getLocalizedMessage))
            }
        }.recoverTotal {
          case e => BadRequest(JsonReply.error("Except two values"))
        }
      }.getOrElse {
        BadRequest(authenticatedRequest.request.toString())
      }
    }
  }

  def HistoryNMS2GDT() = AuthenticatedJsonAction { authenticatedRequest =>
    val pythiaUrl = Utils.getPythiaUrl

    if (pythiaUrl == None) {
      ServiceUnavailable(JsonReply.error("Pythia service is currently unavailable."))
    }
    else {
      authenticatedRequest.request.body.asJson.map { json =>
        json.validate[Map[String, String]].map {
          case dates: Map[String, String] =>
            try {
              val authHeader = authenticatedRequest.user.token.get.authToken
              val date = dates.getOrElse("date", "")
              val start = dates.getOrElse("start", "")
              val end = dates.getOrElse("end", "")
              if (date.isEmpty || start.isEmpty || end.isEmpty) {
                BadRequest(JsonReply.error("expected a date value"))
              } else {
                val url = pythiaUrl.get + "/api/tnms2g/" + date.toString + "/" + start.toString + "/" + end.toString + ""
                //val url="http://date.jsontest.com/"
                val req = WSHelper.requestWithAuthHeader(url, authHeader)
                val futureResponse = req.get()
                val resp = Await.result(futureResponse, 300.seconds)

                Ok(resp.json)
              }
            }
            catch {
              case te: TimeoutException =>
                RequestTimeout(JsonReply.error("expected timed out:" + te.getLocalizedMessage))
            }
        }.recoverTotal {
          case e => BadRequest(JsonReply.error("Except two values"))
        }
      }.getOrElse {
        BadRequest(authenticatedRequest.request.toString())
      }
    }
  }

  def HistoryNMS3GDT() = AuthenticatedJsonAction { authenticatedRequest =>
    val pythiaUrl = Utils.getPythiaUrl

    if (pythiaUrl == None) {
      ServiceUnavailable(JsonReply.error("Pythia service is currently unavailable."))
    }
    else {
      authenticatedRequest.request.body.asJson.map { json =>
        json.validate[Map[String, String]].map {
          case dates: Map[String, String] =>
            try {
              val authHeader = authenticatedRequest.user.token.get.authToken
              val date = dates.getOrElse("date", "")
              val start = dates.getOrElse("start", "")
              val end = dates.getOrElse("end", "")
              if (date.isEmpty || start.isEmpty || end.isEmpty) {
                BadRequest(JsonReply.error("expected a date value"))
              } else {
                val url = pythiaUrl.get + "/api/tnms3g/" + date.toString + "/" + start.toString + "/" + end.toString + ""
                //val url="http://date.jsontest.com/"
                val req = WSHelper.requestWithAuthHeader(url, authHeader)
                val futureResponse = req.get()
                val resp = Await.result(futureResponse, 300.seconds)

                Ok(resp.json)
              }
            }
            catch {
              case te: TimeoutException =>
                RequestTimeout(JsonReply.error("expected timed out:" + te.getLocalizedMessage))
            }
        }.recoverTotal {
          case e => BadRequest(JsonReply.error("Except two values"))
        }
      }.getOrElse {
        BadRequest(authenticatedRequest.request.toString())
      }
    }
  }

  def HistoryTHNMS3GDT() = AuthenticatedJsonAction { authenticatedRequest =>
    val pythiaUrl = Utils.getPythiaUrl

    if (pythiaUrl == None) {
      ServiceUnavailable(JsonReply.error("Pythia service is currently unavailable."))
    }
    else {
      authenticatedRequest.request.body.asJson.map { json =>
        json.validate[Map[String, String]].map {
          case dates: Map[String, String] =>
            try {
              val authHeader = authenticatedRequest.user.token.get.authToken
              val date = dates.getOrElse("date", "")
              val start = dates.getOrElse("start", "")
              val end = dates.getOrElse("end", "")
              if (date.isEmpty || start.isEmpty || end.isEmpty) {
                BadRequest(JsonReply.error("expected a date value"))
              } else {
                val url = pythiaUrl.get + "/api/thnms3g/" + date.toString + "/" + start.toString + "/" + end.toString + ""
                //val url="http://date.jsontest.com/"
                val req = WSHelper.requestWithAuthHeader(url, authHeader)
                val futureResponse = req.get()
                val resp = Await.result(futureResponse, 300.seconds)

                Ok(resp.json)
              }
            }
            catch {
              case te: TimeoutException =>
                RequestTimeout(JsonReply.error("expected timed out:" + te.getLocalizedMessage))
            }
        }.recoverTotal {
          case e => BadRequest(JsonReply.error("Except two values"))
        }
      }.getOrElse {
        BadRequest(authenticatedRequest.request.toString())
      }
    }
  }
}
