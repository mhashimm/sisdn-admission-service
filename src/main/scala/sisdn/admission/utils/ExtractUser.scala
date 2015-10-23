package sisdn.admission.utils

import akka.http.scaladsl.model.{StatusCodes, IllegalRequestException, ErrorInfo}
import authentikat.jwt.JsonWebToken
import com.typesafe.config.ConfigFactory
import sisdn.admission.model.User
import spray.json.JsonParser
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ExtractUser extends (String => Future[User]) with JsonProtocol {
  val key = ConfigFactory.load().getString("admission.key")

  def apply(jwt: String) = {
    Future(JsonWebToken.validate(jwt, key)).flatMap {
      case true =>
        Future(JsonWebToken.unapply(jwt).map(x => x._2.asJsonString)).flatMap{
          case Some(token) => Future(JsonParser(token).convertTo[User])
          case None => Future.failed(IllegalRequestException(ErrorInfo("Error while parsing token", ""),
            StatusCodes.Unauthorized))
        }
      case false => Future.failed(IllegalRequestException(ErrorInfo("Invalid authorization token", ""),
        StatusCodes.Unauthorized))
    }
  }
}

object ExtractUser extends ExtractUser