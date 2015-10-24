package sisdn.admission.utils

import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.RequestContext
import authentikat.jwt.JsonWebToken
import com.typesafe.config.ConfigFactory

class ValidateToken extends ((RequestContext) => Boolean) with JsonProtocol {

  def apply(ctx: RequestContext) = ctx.request.headers.collectFirst {
    case Authorization(OAuth2BearerToken(token)) => token
                    }.exists(JsonWebToken.validate(_, ConfigFactory.load().getString("admission.key")))
}