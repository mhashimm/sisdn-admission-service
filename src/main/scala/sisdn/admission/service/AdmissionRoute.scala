package sisdn.admission.service

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.{OAuth2BearerToken, Authorization}
import akka.http.scaladsl.server.{RequestContext, Directives, Route}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import sisdn.admission.model.{User, Student}
import sisdn.admission.utils.{ExtractUserDirective, ValidateToken, JsonProtocol, ExtractUser}
import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.concurrent.duration._

class AdmissionRoute extends Directives with JsonProtocol {
  implicit val system = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = 1 second

  val validator = new ValidateToken
  val userExtractor = new ExtractUserDirective

  val route: Route = {
    path("v1" / "add" | "add") {
      post {
        authorize(validator) {
          extract(userExtractor) { user =>
          //ExtractUserDirective{ user: User =>
            entity(as[List[Student]]) { students =>
              complete {
                ""
              }
            }
          }
          }
        }
      }
    }
  }
