package sisdn.admission.test

import akka.actor.{ActorRef, ActorSystem}
import akka.persistence.fsm.PersistentFSM._
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest._
import sisdn.admission.model._
import sisdn.admission.service.Admission
import sisdn.admission.service.Admission.SubmittedEvt
import scala.language.postfixOps
import akka.util.Timeout
import scala.concurrent.duration._
import sisdn.admission.utils.Conversions._

class AdmissionSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with FlatSpecLike with Matchers with BeforeAndAfterAll {

  import Admission._

  def this() = this(ActorSystem("AdmissionSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  val config = ConfigFactory.load().getConfig("sisdn.admission")
  val validTimeout = config.getDuration("validationResponseTimeout")
  val processTimeout = config.getDuration("processingAckTimeout")

  def admissionFunc(id: String, valid: ActorRef, proc: ActorRef): ActorRef = system.actorOf(Admission.props(id, valid, proc))

  def admissionData(id: String) = new SubmissionData(id, Student(id, "", "", "", 1, 1, "org"), AdmissionStatus.Pending, "")


  "Admission actor" should "acknowledge received and save admission" in {
    val valid, proc, driver = TestProbe()
    val id = "1"
    val admission = admissionFunc("1", valid.ref, proc.ref)
    val aData = admissionData("1")
    admission ! SubmittedEvt(aData)
    expectMsg(ACK)

    admission.tell("state", driver.ref)
    driver.expectMsgPF(){
      case current: SubmissionData => current.student shouldEqual aData.student
    }
  }

  it should "invoke Validation service" in {
    val valid, proc, driver = TestProbe()
    val id = "2"
    val admission = admissionFunc(id, valid.ref, proc.ref)
    admission.tell(SubscribeTransitionCallBack(driver.ref), driver.ref)
    admission.tell(SubmittedEvt(admissionData(id)), driver.ref)
    valid.expectMsg(admissionData(id).student)
  }

  it should """respond to positive validation by moving to "ValidState" """ in {
    val driver, valid, proc = TestProbe()
    val id = "3"
    val admission = admissionFunc(id, valid.ref, proc.ref)
    admission.tell(SubscribeTransitionCallBack(driver.ref), driver.ref)
    admission.tell(SubmittedEvt(admissionData(id)), driver.ref)
    valid.expectMsg(admissionData(id).student)
    valid.reply(ValidatedEvt( ValidAdmission))
    driver.receiveN(3)
    driver.expectMsgPF(validTimeout, "") {
      case Transition(_, PendingValidationState, ValidState, _) => true
    }

    admission.tell("state", driver.ref)
    driver.expectMsgPF(validTimeout, "") {
      case current: SubmissionData => current.status shouldBe AdmissionStatus.Valid
    }

  }

  it should """respond to negative validation by moving to "InvalidState" """ in {
    val driver, valid, proc = TestProbe()
    val id = "4"
    val admission = admissionFunc(id, valid.ref, proc.ref)
    admission.tell(SubscribeTransitionCallBack(driver.ref), driver.ref)
    admission.tell(SubmittedEvt(admissionData(id)), driver.ref)
    valid.expectMsg(admissionData(id).student)
    valid.reply(ValidatedEvt(InvalidAdmission("")))

    driver.receiveN(3)
    driver.expectMsgPF() {
      case Transition(_, PendingValidationState, InvalidState, _) => true
    }

    admission.tell("state", driver.ref)
    driver.expectMsgPF() {
      case current: SubmissionData => current.status shouldEqual AdmissionStatus.Invalid
    }
  }

  it should """stay in "PendingValidation" and retry when no response is received from validation service""" in {
    val driver, valid, proc = TestProbe()
    val id = "5"
    val admission = admissionFunc(id, valid.ref, proc.ref)
    val aData = admissionData(id)
    admission.tell(SubmittedEvt(aData), driver.ref)
    valid.receiveN(2, validTimeout * 2)
  }

  it should """move to "ValidState" after receiving confirmation from processor""" in {
    val driver, valid, proc = TestProbe()
    val id = "6"
    val admission = admissionFunc(id, valid.ref, proc.ref)
    val aData = admissionData(id)

    admission.tell(SubscribeTransitionCallBack(driver.ref), driver.ref)
    admission.tell(SubmittedEvt(aData), driver.ref)

    valid.expectMsg(aData.student) /* and respond with*/
    valid.reply(ValidatedEvt(ValidAdmission))
    proc.expectMsg(aData.student)
    proc.reply(ACK)
    driver.receiveN(4)
    driver.expectMsgPF() {
      case Transition(_, ValidState, InProcessingState, _) => true
    }

    admission.tell("state", driver.ref)
    driver.expectMsgPF() {
      case current: SubmissionData => current.status shouldEqual AdmissionStatus.Valid
    }
  }

  it should """stay in "ValidState" and keep retrying processor if not confirmed""" in {
    val driver, valid, proc = TestProbe()
    val id = "7"
    val admission = admissionFunc(id, valid.ref, proc.ref)
    val aData = admissionData(id)

    admission.tell(SubmittedEvt(aData), driver.ref)

    valid.expectMsg(aData.student)
    valid.reply(ValidatedEvt(ValidAdmission))
    proc.receiveN(2, validTimeout * 2)
  }

  it should """move to "AcceptedState" after being accepted""" in {
    val driver, valid, proc = TestProbe()
    val id = "8"
    val admission = admissionFunc(id, valid.ref, proc.ref)
    val aData = admissionData(id)

    admission.tell(SubscribeTransitionCallBack(driver.ref), driver.ref)
    admission.tell(SubmittedEvt(aData), driver.ref)

    valid.expectMsg(aData.student)
    valid.reply(ValidatedEvt(ValidAdmission))
    proc.expectMsg(aData.student)
    proc.reply(ACK)
    proc.send(admission, ProcessedEvt(Accepted))
    driver.receiveN(5)
    driver.expectMsgPF(processTimeout, "") {
      case Transition(_, InProcessingState, AcceptedState, _) => true
    }

    admission.tell("state", driver.ref)
    driver.expectMsgPF() {
      case current: SubmissionData => current.status shouldEqual AdmissionStatus.Accepted
    }
  }

  it should """move to "RejectedState" after being rejected""" in {
    val driver, valid, proc = TestProbe()
    val id = "9"
    val admission = admissionFunc(id, valid.ref, proc.ref)
    val aData = admissionData(id)

    admission.tell(SubscribeTransitionCallBack(driver.ref), driver.ref)
    admission.tell(SubmittedEvt(aData), driver.ref)

    valid.expectMsg(aData.student)
    valid.reply(ValidatedEvt(ValidAdmission))
    proc.expectMsg(aData.student)
    proc.reply(ACK)
    proc.send(admission, ProcessedEvt(Rejected("rejected")))
    driver.receiveN(5)
    driver.expectMsgPF(processTimeout, "") {
      case Transition(_, InProcessingState, RejectedState, _) => true
    }

    admission.tell("state", driver.ref)
    driver.expectMsgPF(){
      case current: SubmissionData => current.status shouldEqual AdmissionStatus.Rejected
    }
  }
}
