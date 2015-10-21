package sisdn.admission.utils

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import sisdn.admission.model.{User, Student}
import spray.json._

trait JsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val studentFormat = jsonFormat7(Student.apply)
  implicit val userFormat    = jsonFormat4(User.apply)
}
