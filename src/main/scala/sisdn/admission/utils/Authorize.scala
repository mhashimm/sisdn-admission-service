package sisdn.admission.utils

import akka.http.scaladsl.model._
import akka.actor.ActorSystem
import akka.http.scaladsl.server.RequestContext
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.util.Timeout
import authentikat.jwt._
import scala.concurrent.duration._
import sisdn.admission.model.{User, Student}
import headers._
import scala.language.postfixOps
import scala.concurrent.{Await, Future, ExecutionContext}
import scala.util.control.NonFatal
import spray.json._


trait AuthorizeAdmission extends ((RequestContext) => Boolean) with JsonProtocol

object AuthorizeAdmission extends AuthorizeAdmission {
  implicit val system = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = 1 second

  def apply(ctx: RequestContext): Boolean = {
    val jwt: String = ctx.request.headers.collectFirst {
      case Authorization(OAuth2BearerToken(token)) => token
    } getOrElse ""

    if (!JsonWebToken.validate(jwt, "secretkey"))
      return false

    val userClaims = JsonWebToken.unapply(jwt).map(x => x._2.asJsonString) getOrElse ""

    if (userClaims.isEmpty)
      return false

    val user: User = JsonParser(userClaims.toString).convertTo[User]

    val result: Future[Boolean] = Unmarshal(ctx.request.entity).to[List[Student]].
      flatMap { xs => Future(xs.map(_.org).toSet).map{
        case o if o.size == 1 && o.head == user.org => true
        case _ => { new IllegalRequestException(
          ErrorInfo("Multiple or wrong organization submission", ""),StatusCodes.Forbidden)
          false
        }
      }.map{
        case false => false
        case true  => {
          xs.map(_.faculty).toSet subsetOf user.faculties
          }
        }
    }.recover { case NonFatal(e) => false }

    Await.result(result, 10 seconds)
  }
}
