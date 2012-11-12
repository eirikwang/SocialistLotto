package org.clarity.demo.cqrs.server.actors

import akka.testkit.{TestProbe, ImplicitSender, TestKit, TestActorRef}
import org.scalatest._
import matchers.MustMatchers
import org.clarity.demo.cqrs.server.actors.Account.{ReceiveAction, Balance, SendAction}
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import akka.dispatch.Await


/**
 * @author Eirik Wang - eirik.wang@bekk.no
 * @since 2.2 TODO: Check version
 */
class AccountHolderTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpec with MustMatchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AccountHolderTest"))
  implicit val timeout = Timeout(60 seconds)



  override def afterAll() {
    system.shutdown()
  }

  "AccountHolder" must {
    "CreateActorWhenNotStarted" in {
      val actorRec = TestActorRef(new AccountHolder(), "accounts")
      val actorOf = system.actorFor("/user/accounts/1")
      println(actorOf.isTerminated)
      println(actorRec)
      val p = actorOf ? Balance
      Await.result(p, timeout.duration)
    }
  }

}
