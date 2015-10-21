package sisdn.admission.service

import akka.actor.Props
import akka.persistence.PersistentActor
import sisdn.admission.model.Student

object UserActor {
  def props(userId: String) = Props(new UserActor(userId))

  case class Admit(students: List[Student])
}

class UserActor(userId: String) extends PersistentActor {

  override def persistenceId: String = userId

  val receiveRecover = ???

  val receiveCommand = ???

}
