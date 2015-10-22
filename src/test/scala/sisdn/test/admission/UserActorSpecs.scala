package sisdn.test.admission

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike, Matchers}
import sisdn.admission.service.UserActor


class UserActorSpecs(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
        with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("UserActorSpec"))

  override def afterAll { TestKit.shutdownActorSystem(system)}

  val actor = system.actorOf(UserActor.props(""), "userActor")

  /*"UserActor" should {
    "Send acknowledgement of the transaction " in {
      actor ! "hello"
      expectMsg("world")
    }
  }*/
}
