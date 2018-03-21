package controllers

import _root_.util.actions.{AuthenticatedJsonAction, AuthenticatedAction}
import _root_.util.{AuthUtils, JsonReply}
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import pythia.models.{Token, User}

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
object UserController extends Controller {

  case class Login(username: String, password: String)

  implicit val loginReads = Json.reads[Login]

  implicit val tokenFormats = Json.format[Token]
  implicit val userFormats = Json.format[User]

  def authenticate() = Action { request =>
    //        val tokenOpt = AuthUtils.parseAuthTokenFromRequest
    //        if(tokenOpt == None) {
    //            //TODO: authenticate
    //        }
    //        else {
    //            //return same authtoken
    //        }
    request.body.asJson.map { json =>
      json.validate[Login].map {
        case l: Login =>
          val u = User.login(l.username, l.password)
          if (u.isDefined)
            Ok(JsonReply.replyWith("success", u.get))
              .withCookies(AuthUtils.getAuthorizationCookie(u.get.token.get.authToken))
          else
            Ok(JsonReply.error("invalid username/password")).withNewSession
      } recoverTotal {
        e => BadRequest(JsonReply.error("you must provide both username and password")).withNewSession
      }
    } getOrElse {
      BadRequest(JsonReply.error("expected content-type json")).withNewSession
    }
  }

  def logout() = {
    AuthenticatedAction { request =>
      request.user.logout()

      Redirect(routes.Application.loginPage())
    }

  }

  def userInfo() = AuthenticatedJsonAction { request =>
    Ok(JsonReply.replyWith("success", request.user))
  }

  def createUser() = play.mvc.Results.TODO

  def saveCode() = AuthenticatedAction { request =>
    request.body.asJson.map { json =>
      (json \ "data").asOpt[String].map { data =>
        //val jsonr=JsonReply.replyWith("success",  data)
        Ok("success")
      }.getOrElse {
        BadRequest("Missing parameter [data]")
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }
}