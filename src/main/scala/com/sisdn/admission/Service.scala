package com.sisdn.admission

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContext

/**
 * Created by mhashim on 10/14/2015.
 */
object Service {
//  implicit val executer: ExecutionContext = ???
//  implicit val materializer: ActorMaterializer = ???
//  implicit val system: ActorSystem = ???

  val admissionRoute = {
    path("admission") {
      post {
        complete{
          "Hello"
        }
      }
    }
  }

}
