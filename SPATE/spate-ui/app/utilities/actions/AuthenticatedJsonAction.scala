package utilities.actions

import models.{Token, User}
import play.api.mvc.{Results, Result, Request, ActionBuilder}
import utilities.AuthUtils

import scala.concurrent.Future

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
object AuthenticatedJsonAction extends ActionBuilder[AuthenticatedRequest] {
    import scala.concurrent.ExecutionContext.Implicits.global

    def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {
        val token = AuthUtils.parseAuthTokenFromRequest(request)
        if (token != None) {
            val user = User.userInfo(token.get)
            if(user != None) {
                val userr = user.get
                if (userr.token == None)
                    userr.token = Some(Token(user.get.userId, token.get))
                block(AuthenticatedRequest(userr, request))
            }
            else
                Future { Results.Unauthorized.withNewSession }
        }
        else
            Future { Results.Unauthorized.withNewSession }
    }
}