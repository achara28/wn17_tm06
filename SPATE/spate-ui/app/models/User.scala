package models

import java.util.concurrent.TimeoutException

import play.api.libs.json._
import utilities.{WSHelper, Utils}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

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
case class Token(userId: String, authToken:String)
case class User(userId: String, username: String, role: String, pythiaKey: Option[String], var token: Option[Token])

object User {
    implicit val tokenFormat = Json.format[Token]
    implicit val userFormat = Json.format[User]
    def userInfo(authToken: String): Option[User] = {
        val pythiaUrl = Utils.getPythiaUrl
        if(pythiaUrl!=None) {
            try {
                val futureResponse = WSHelper.requestWithAuthHeader(pythiaUrl.get + "/user", authToken).get().map {
                    response => response
                }
                val response = Await.result(futureResponse, 30.seconds)
                if (response.status == 200) {
                    response.json.validate[User].map {
                        case user: User => Some(user)
                    }.recoverTotal {
                        case e => None
                    }
                }
                else {
                    None
                }
            }
            catch {
                case te: TimeoutException => None
            }
        }
        else {
            None
        }
    }

    def logout(user: User) = {
        val pythiaUrl = Utils.getPythiaUrl
        if(pythiaUrl!=None) {
            try {
                val futureResponse = WSHelper.requestWithAuthHeader(pythiaUrl.get + "/user", user.token.get.authToken).delete().map {
                    response => response
                }
                val response = Await.result(futureResponse, 5.seconds)
                if (response.status == 200) {
                    val status = (response.json \ "status").as[String]
                    if(status == "success") {
                        true
                    }
                    else {
                        false
                    }
                }
                else {
                    false
                }
            }
            catch {
                case te: TimeoutException => false
            }
        }
        else {
            true
        }
    }

//    User("admin", "d033e22ae348aeb5660fc2140aec35850c4da997", "75499bfa2c8348a19b3637239ccec54e233a6b083bc02ef6884e80ee7506cfd9") // pass: admin
//    User("test", "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3") // pass: test
//    User("user", "9d4e1e23bd5b727046a9e3b4b7db57bd8d6ee684") // pass: pass

}
