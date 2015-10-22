package sisdn.test

import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import sisdn.admission.service.admission.admissionRoute
import org.scalatest.{FlatSpec, Matchers}
import MediaTypes._
import headers._

class AdmissionRouteSpecs extends FlatSpec with Matchers with ScalatestRouteTest with JwtFixture {


  val hd = Authorization(OAuth2BearerToken(jwt))


  "Admission Service" should "Return Success for POST Request" in {
    Post("/add", HttpEntity(`application/json`, stdJson)).addHeader(hd) ~> admissionRoute ~> check {
      status shouldBe StatusCodes.OK
    }
  }

  it should "Return MethodNotAllowed for GET Request" in {
    Get("/add") ~> admissionRoute ~> check {
      rejection shouldEqual MethodRejection(HttpMethods.POST)
    }
  }

  it should """Accept request for /v1 route as the default route""" in {
    Post("/v1/add", HttpEntity(`application/json`, stdJson)).addHeader(hd) ~> admissionRoute ~> check {
      status shouldBe StatusCodes.OK
    }
  }

  it should """Fail for arbitrary Url""" in {
    Post("/x", HttpEntity(`application/json`, "[]")) ~> admissionRoute ~> check {
      handled shouldBe false
    }
    Post("/v1/aaa", HttpEntity(`application/json`, "[]")) ~> admissionRoute ~> check {
      handled shouldBe false
    }
    Post("/xxx/add", HttpEntity(`application/json`, "[]")) ~> admissionRoute ~> check {
      handled shouldBe false
    }
  }
}


