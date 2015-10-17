package com.sisdn.admission.service

import akka.http.impl.util.JavaMapping.HttpMethod
import org.scalatest.{Matchers, FlatSpec}
import akka.http.scaladsl.model.{HttpMethods, StatusCode, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._

import com.sisdn.admission.Main.admissionRoute

class AdminServiceHttpSpec extends FlatSpec with Matchers with ScalatestRouteTest {
  import com.sisdn.admission.Main.admissionRoute

  "Admission Service" should "Return Success for POST Request" in {
    Post("/admission") ~> admissionRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }

  it should "Return MethodNotAllowed for GET Request" in {
    Get("/admission") ~> admissionRoute ~> check {
      //status === StatusCodes.MethodNotAllowed
      rejection shouldEqual MethodRejection(HttpMethods.POST)
    }
  }

  it should """Accept request for /admission/v1 route by default""" in {
    Post("/admission/v1") ~> admissionRoute ~> check {
    status === StatusCodes.OK
}
}
}
