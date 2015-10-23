package sisdn.test.admission

import akka.http.scaladsl.model.IllegalRequestException
import authentikat.jwt.{JwtClaimsSet, JsonWebToken}
import org.scalatest.concurrent._
import org.scalatest.time._
import org.scalatest.{Matchers, FlatSpec}
import spray.json.DeserializationException
import sisdn.admission.utils.ExtractUser
import scala.language.postfixOps

class ExtractUserSpecs extends FlatSpec with Matchers with ScalaFutures with JwtFixture {

  implicit val defaultPatienceConfig = PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))

  "User Extraction" should "succeed for correct token" in {
    val result = ExtractUser(jwt)
    whenReady(result){ user =>
      user.subject shouldEqual "subject"
      user.departments shouldEqual Set(1)
    }
  }

  it should "fail for invalid token" in {
    val result = ExtractUser("no.key.exists")
    whenReady(result.failed){ e =>
      e shouldBe a [IllegalRequestException]
      e.asInstanceOf[IllegalRequestException].info.summary shouldEqual "Invalid authorization token"
    }
  }

  it should "fail for invalid claims" in {
    val result = ExtractUser(JsonWebToken(jwtHed, JwtClaimsSet("{}"), "mySecret"))
    whenReady(result.failed) { e =>
      e shouldBe a [DeserializationException]
    }
  }
}
