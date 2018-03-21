package util.actions

import play.api.mvc._
import pythia.models.{Token, User}
import util.AuthUtils

import scala.concurrent.Future

/**
 * Created by canast02 on 14/2/15.
 */
object AuthenticatedAction extends ActionBuilder[AuthenticatedRequest] {
    import scala.concurrent.ExecutionContext.Implicits.global

    def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {
        val token = AuthUtils.parseAuthTokenFromRequest(request)
        if (token != None) {
            val user = User.userInfo(token.get)
            if(user != None) {
                val userr = user.get
                if (userr.token == None)
                    userr.setToken(Token(user.get.userId, token.get))
                block(AuthenticatedRequest(userr, request))
            }
            else
                Future { Results.Redirect(controllers.routes.Application.loginPage()).withNewSession }
        }
        else
            Future { Results.Redirect(controllers.routes.Application.loginPage()).withNewSession }
    }
}