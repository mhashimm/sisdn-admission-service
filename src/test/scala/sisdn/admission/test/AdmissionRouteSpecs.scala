package sisdn.admission.test

import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import sisdn.admission.model.User
import sisdn.admission.service.AdmissionRoute
import org.scalatest.{FlatSpec, Matchers}
import MediaTypes._
import headers._

class AdmissionRouteSpecs extends FlatSpec with Matchers with ScalatestRouteTest {
  val stdJson = """[{"firstName" : "first", "secondName" : "second", "thirdName" : "third",
                  |"fourthName" : "fourth", "org" : "org", "faculty" : 1, "program" : 1}]""".stripMargin
  val claimsSet = JwtClaimsSet("""{"departments" : [1], "subject" : "subject",
      |"org" : "org", "faculties" : [1]}""".stripMargin)
  val jwt: String = JsonWebToken(JwtHeader("HS256"), claimsSet, "mySecret")

  val routeClass = new AdmissionRoute {
    override val userExtractor = (str:String) => User("subject", "org", Some(Set(1)), Some(Set(1)))
  }

  val admissionRoute = routeClass.route
  val header = Authorization(OAuth2BearerToken(jwt))

  "Admission Service" should "Return Success for POST Request" in {
    Post("/admit", HttpEntity(`application/json`, stdJson)).addHeader(header) ~> admissionRoute ~> check {
      status shouldBe StatusCodes.OK
    }
  }

  it should "Return MethodNotAllowed for GET Request" in {
    Get("/admit") ~> admissionRoute ~> check {
      rejection shouldEqual MethodRejection(HttpMethods.POST)
    }
  }

  it should """Accept request for /v1 route as the default route""" in {
    Post("/admit/v1", HttpEntity(`application/json`, stdJson)).addHeader(header) ~> admissionRoute ~> check {
      status shouldBe StatusCodes.OK
    }
  }

  it should """Fail for arbitrary Url""" in {
    Post("/x", HttpEntity(`application/json`, "[]")) ~> admissionRoute ~> check {
      handled shouldBe false
    }
    Post("/admit/aa", HttpEntity(`application/json`, "[]")) ~> admissionRoute ~> check {
      handled shouldBe false
    }
    Post("/aa/admit", HttpEntity(`application/json`, "[]")) ~> admissionRoute ~> check {
      handled shouldBe false
    }
  }
}

