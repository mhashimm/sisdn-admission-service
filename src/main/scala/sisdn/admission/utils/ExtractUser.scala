package sisdn.admission.utils

import akka.http.scaladsl.model.{ErrorInfo, ParsingException}
import authentikat.jwt.JsonWebToken
import com.typesafe.config.ConfigFactory
import sisdn.admission.model.User
import spray.json.JsonParser

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ExtractUser extends (String => Future[Option[User]]) with JsonProtocol {
  val key = ConfigFactory.load().getString("admission.key")

  def apply(jwt: String): Future[Option[User]] = {
    Future(JsonWebToken.validate(jwt, key)).flatMap {
      case true =>
        Future(JsonWebToken.unapply(jwt).map(x => x._2.asJsonString)).map{
          case Some(token) =>
            Some(JsonParser(token).convertTo[User])
          case _ => Future.failed(ParsingException(ErrorInfo("Claim parsing error", "")))
            None
        }
      case false => Future.failed(ParsingException(ErrorInfo("Token parsing error", "")))
        Future(None)
    }
  }
}

//.recover {case _ => Future.failed(ParsingException(ErrorInfo("Claim parsing error", "")));None }
//.recover {case _ => Future.failed(ParsingException(ErrorInfo("Token parsing error", "")));None}