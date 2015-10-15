/**
 * Created by mhashim on 10/14/2015.
 */
package com.sisdn.admission

import org.scalatest.{ Matchers, FlatSpec }
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._

import com.sisdn.admission.Service.admissionRoute

class AdminServiceHttpSpec extends FlatSpec with Matchers with ScalatestRouteTest {


    "Admission Service" should "Return Success for POST Request" in {
      Post("/admission") ~> Route.seal(admissionRoute) ~> check {
        status should be(StatusCodes.OK)
      }
    }

    it should "Return MethodNotAllowed for GET Request" in {
      Get("/admission") ~> Route.seal(admissionRoute) ~> check {
        status === StatusCodes.MethodNotAllowed
      }
    }

}
