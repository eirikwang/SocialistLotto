package org.clarity.demo.cqrs.server.actors.account

import akka.testkit.{TestProbe, ImplicitSender, TestKit, TestActorRef}
import org.scalatest._
import matchers.MustMatchers
import org.clarity.demo.cqrs.server.actors.account.Account.Balance
import akka.actor.{Props, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import akka.dispatch.Await
import org.clarity.demo.cqrs.server.actors.account.AccountHolder.{Created, CreateAccount}


/**
 * @author Eirik Wang - eirik.wang@bekk.no
 * @since 2.2 TODO: Check version
 */
class AccountHolderTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpec with MustMatchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AccountHolderTest"))
  implicit val timeout = Timeout(60 seconds)



val testProbe = TestProbe()
 system.actorOf(Props(ctx => {
   case msg => testActor ! msg;println("msg: " + msg); testProbe.ref ! msg
 }), "persistence/accounts")

  override def afterAll() {
    system.shutdown()
  }

  "AccountHolder" must {
    "CreateActor" in {
      val actorRec = TestActorRef(new AccountHolder, "accounts")
      actorRec ! CreateAccount("new account")
      testProbe.expectMsg(CreateAccount)
    }
  }

}
