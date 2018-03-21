package util.actions

import play.api.mvc.{WrappedRequest, Request}
import pythia.models.User

/**
 * Created by canast02 on 27/4/15.
 */
case class AuthenticatedRequest[A](user: User, request: Request[A]) extends WrappedRequest(request)
