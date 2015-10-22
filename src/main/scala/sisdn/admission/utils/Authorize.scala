package sisdn.admission.utils

import akka.http.scaladsl.model._
import akka.actor.ActorSystem
import akka.http.scaladsl.server.RequestContext
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.util.Timeout
import scala.concurrent.duration._
import sisdn.admission.model.{User, Student}
import headers._
import scala.concurrent.{Await, Future, ExecutionContext}
import scala.util.control.NonFatal
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

    val user = User("","", Set(1), Set(1) )
    val result: Future[Boolean] = Unmarshal(ctx.request.entity).to[List[Student]].
      flatMap { xs => Future(xs.map(_.org).toSet).map{
        case o if o.size == 1 && o.head == user.org => true
        case _ =>  IllegalRequestException(ErrorInfo("Multiple or wrong organization submission", ""),
            StatusCodes.Unauthorized)
          false
      }.map{
        case false => false
        case true  => xs.map(_.faculty).toSet subsetOf user.faculties
        }
    }.recover { case NonFatal(e) => false }

    Await.result(result, 10 seconds)
  }
}
