package sisdn.admission.model

case class User(subject: String, org: String, departments: Set[Int], faculties: Set[Int])
