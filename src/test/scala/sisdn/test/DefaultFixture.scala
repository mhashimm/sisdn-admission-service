package sisdn.test

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import com.typesafe.config.{ConfigValueFactory, ConfigFactory}
import scala.concurrent.duration._
import scala.language.postfixOps

import scala.concurrent.ExecutionContext

trait BaseFixture {
  ConfigFactory.load().withValue("admission.key", ConfigValueFactory.fromAnyRef("mySecret"))
}

trait ContextFixture extends BaseFixture{
  implicit val system = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = 10 seconds
}

trait JwtFixture extends BaseFixture {
  val jwtHed = JwtHeader("HS256")
  val claimsSet = JwtClaimsSet("""{"departments" : [1], "subject" : "subject", "org" : "org", "faculties" : [1]}""")
  val jwt: String = JsonWebToken(jwtHed, claimsSet, ConfigFactory.load().getString("admission.key"))
  val stdJson =
    """[{"firstName" : "first", "secondName" : "second", "thirdName" : "third", "fourthName" : "fourth",
      |"org" : "org", "faculty" : 1, "program" : 1}]""".stripMargin
}
