package sisdn.admission.model




sealed trait ValidationData
case object ValidAdmission extends ValidationData
case class InvalidAdmission(reason: String) extends ValidationData

sealed trait ProcessResponseData
case object Accepted extends ProcessResponseData
case class Rejected(reason: String) extends ProcessResponseData

case object ACK

