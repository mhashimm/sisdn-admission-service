package com.sisdn.admission.utils

import com.sisdn.admission.model.Student
import spray.json.DefaultJsonProtocol

trait JsonProtocol extends DefaultJsonProtocol {
  implicit val studentFormat = jsonFormat6(Student.apply)
}

object JsonProtocol extends JsonProtocol
