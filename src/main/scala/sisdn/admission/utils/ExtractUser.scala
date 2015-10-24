package sisdn.admission.utils

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{OAuth2BearerToken, Authorization}
import akka.http.scaladsl.server.{RouteResult, RequestContext}
import sisdn.admission.model.User
import spray.json.JsonParser
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.language.postfixOps

object ExtractUser extends (String => Future[User]) with JsonProtocol {
  def apply(token: String) = {
    Future {
      JsonParser(token).convertTo[User]
    }.
      recover {
        case _ => throw IllegalRequestException(ErrorInfo("Error while parsing token", ""),
          StatusCodes.Unauthorized)
      }
  }
}

class ExtractUserDirective extends (RequestContext => Future[RouteResult]) with JsonProtocol{
  def apply(ctx: RequestContext) = ctx.complete{
    ctx.request.headers.collectFirst {
      case Authorization(OAuth2BearerToken(t)) => ExtractUser(t)
    }.get
  }
}
