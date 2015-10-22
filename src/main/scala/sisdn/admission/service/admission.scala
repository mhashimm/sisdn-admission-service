package sisdn.admission.service

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import sisdn.admission.model.Student
import sisdn.admission.utils.{AdmissionAuth, JsonProtocol}
import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.concurrent.duration._


object admission extends Directives with JsonProtocol {
  implicit val system = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = 1 second

  val admissionRoute: Route = {
    path("v1" / "add" | "add") {
      post {
        authorize(AdmissionAuth) {
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