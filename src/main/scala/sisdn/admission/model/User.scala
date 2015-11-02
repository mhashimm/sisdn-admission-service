package sisdn.admission.model

case class User(
 subject: String,
 org: String,
 departments: Option[Set[Int]],
 faculties: Option[Set[Int]]
)
