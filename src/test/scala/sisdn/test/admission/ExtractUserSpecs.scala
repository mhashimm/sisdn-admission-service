package sisdn.test.admission

import org.scalatest.{Matchers, FlatSpec}
import sisdn.admission.model.User
import sisdn.admission.utils.ExtractUser
import scala.concurrent.duration._
import scala.language.postfixOps

import scala.concurrent.Await

class ExtractUserSpecs extends FlatSpec with Matchers with JwtFixture with ContextFixture {

  "User Extraction" should "succeed for correct token" in {
    val user:User = Await.result(new ExtractUser().apply(jwt), 10 seconds).get
    user.subject shouldEqual "subject"
    user.departments shouldEqual  Set(1)
  }
}
