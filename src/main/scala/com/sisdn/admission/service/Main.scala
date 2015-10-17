package com.sisdn.admission.service

import akka.http.scaladsl.server.Directives._

object Main {
  val admissionRoute = {
    pathPrefix("admission" | "admission" / "v1") {
      post {
        complete{
          "Hello"
        }
      }
    }
  }
}
