package sisdn.test.admission

import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import sisdn.admission.model.User
import sisdn.admission.service.AdmissionRoute
import org.scalatest.{FlatSpec, Matchers}
import MediaTypes._
import sisdn.admission.utils.{ExtractUserDirective, ValidateToken}

import scala.concurrent.Future

class AdmissionRouteSpecs extends FlatSpec with Matchers with ScalatestRouteTest with JwtFixture {

  val routeClass = new AdmissionRoute {
    override val validator = new ValidateToken {
      override def apply(c: RequestContext) = true
    }
    override val userExtractor = new ExtractUserDirective {
      override def apply(c: RequestContext) = c.complete {
        Future(User("", "", None, None))
      }
    }
  }
  val admissionRoute = routeClass.route



  "Admission Service" should "Return Success for POST Request" in {
    Post("/add", HttpEntity(`application/json`, stdJson)) ~> admissionRoute ~> check {
      status shouldBe StatusCodes.OK
    }
  }

  it should "Return MethodNotAllowed for GET Request" in {
    Get("/add") ~> admissionRoute ~> check {
      rejection shouldEqual MethodRejection(HttpMethods.POST)
    }
  }

  it should """Accept request for /v1 route as the default route""" in {
<<<<<<< HEAD
    Post("/add/v1", HttpEntity(`application/json`, stdJson)).addHeader(hd) ~> admissionRoute ~> check {
=======
    Post("/v1/add", HttpEntity(`application/json`, stdJson)) ~> admissionRoute ~> check {
>>>>>>> refs/heads/experimental
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


