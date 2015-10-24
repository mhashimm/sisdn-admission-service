package sisdn.admission.utils

import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.RequestContext
<<<<<<< HEAD
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.util.Timeout
import scala.concurrent.duration._
import sisdn.admission.model.{User, Student}
import headers._
import scala.concurrent.{Await, Future, ExecutionContext}
import scala.language.postfixOps


trait AdmissionAuth extends ((RequestContext) => Boolean) with JsonProtocol

object AdmissionAuth extends AdmissionAuth {
  implicit val system = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = 10 second

  def apply(ctx: RequestContext): Boolean = {
    val jwt = ctx.request.headers.collectFirst {
      case Authorization(OAuth2BearerToken(token)) => token
    } getOrElse ""

    if (jwt.isEmpty) return false

    val userF = ExtractUser(jwt)
    val studentsF = Unmarshal(ctx.request.entity).to[List[Student]]

    val result = authorize(userF, studentsF)

    Await.result(result, 10 seconds)
  }

  def authorize(userF: Future[User], studentsF: Future[List[Student]]) = {
    userF.flatMap { user =>
      studentsF.map { students =>
        if ((students.map(_.org).toSet.size == 1) &&
          (students.map(_.org).head == user.org) &&
          (students.map(_.faculty).toSet subsetOf user.faculties))
          true
        else false
      }
    }
  }
}
=======
import authentikat.jwt.JsonWebToken
import com.typesafe.config.ConfigFactory
>>>>>>> refs/heads/experimental

class ValidateToken extends ((RequestContext) => Boolean) with JsonProtocol {

  def apply(ctx: RequestContext) = ctx.request.headers.collectFirst {
    case Authorization(OAuth2BearerToken(token)) => token
                    }.exists(JsonWebToken.validate(_, ConfigFactory.load().getString("admission.key")))
}
