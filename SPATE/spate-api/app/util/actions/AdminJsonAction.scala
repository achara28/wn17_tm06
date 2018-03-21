package util.actions

import play.api.mvc.{ActionBuilder, Request, Result, Results}
import pythia.models.{Token, User}
import util.AuthUtils

import scala.concurrent.Future

/**
 * Created by canast02 on 21/3/15.
 */
object AdminJsonAction extends ActionBuilder[AuthenticatedRequest] {
    import scala.concurrent.ExecutionContext.Implicits.global

    def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {
        val token = AuthUtils.parseAuthTokenFromRequest(request)
        if (token != None) {
            val u = User.userInfo(token.get)
            if(u != None && u.get.role=="admin") {
                val user = u.get
                if (user.token == None)
                    user.setToken(Token(u.get.userId, token.get))
                block(AuthenticatedRequest(user, request))
            }
            else
                Future { Results.Unauthorized("Unauthorized").withNewSession }
        }
        else
            Future { Results.Unauthorized("Unauthorized").withNewSession }
    }
}