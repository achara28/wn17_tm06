package controllers

import java.util.concurrent.TimeoutException

import models.User
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.{DiscardingCookie, Action, Controller}
import utilities.actions.{AuthenticatedJsonAction, AuthenticatedAction}
import utilities.{AuthUtils, WSHelper, JsonReply, Utils}

import scala.concurrent.Await
import scala.concurrent.duration._

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
object UserManagement extends Controller {

    def login = Action { implicit request =>
        val authToken = AuthUtils.parseAuthTokenFromRequest
        if(authToken == None)
            Ok(views.html.index("MTN app v 1.0",Utils.getPythiaBaseName.get)).withNewSession.discardingCookies(DiscardingCookie("Authorization"))
        else {
            val user = User.userInfo(authToken.get)
            if(user == None) {
                Ok(views.html.index("MTN app v 1.0",Utils.getPythiaBaseName.get)).withNewSession.discardingCookies(DiscardingCookie("Authorization"))

            }
            else {
                Redirect("/")
            }
        }
    }

    case class Login(username: String, password: String)
    implicit val LoginFormat = Json.format[Login]
    import models.User._
    def authenticate() = Action { request =>
        val pythiaUrl = Utils.getPythiaUrl
        if(pythiaUrl == None) {
            ServiceUnavailable(JsonReply.error("Pythia service is currently unavailable."))
        }
        else {
            request.body.asJson.map { json =>
                json.validate[Login].map {
                    case login: Login =>
                        if(login.username.isEmpty || login.password.isEmpty) {
                            BadRequest(JsonReply.error("username and password must not be empty"))
                        }
                        else {
                            val json = Json.toJson(login)
                            try {
                                val futureResponse = WSHelper.post(pythiaUrl.get + "/user/login", json).map {
                                    response => response.json
                                }
                                val response = Await.result(futureResponse, 2.seconds)
                                response.validate[User].map {
                                    case user: User =>
                                        val token = user.token.get
                                        val authResp = Json.obj(
                                            "status" -> "success",
                                            "authToken" -> token.authToken
                                        )
                                        Ok(authResp).withCookies(AuthUtils.getAuthorizationCookie(token.authToken))
                                }.recoverTotal {
                                    case e => Ok(JsonReply.error("invalid username/password"))
                                }
                            }
                            catch {
                                case te: TimeoutException => RequestTimeout(JsonReply.error("request timed out"))
                            }
                        }
                }.recoverTotal {
                    case e => BadRequest(JsError.toFlatJson(e))
                }
            }.getOrElse {
                BadRequest(request.toString())
            }
        }
    }

    def userInfo() = AuthenticatedJsonAction { authenticatedRequest =>
        val user = authenticatedRequest.user
        //user.token = None
        Ok(Json.toJson(user))
    }

    def logout() = AuthenticatedAction { authenticatedRequest =>
        User.logout(authenticatedRequest.user)
        Redirect("/").withNewSession.discardingCookies(DiscardingCookie("Authorization"))
    }
}
