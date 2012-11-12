package org.clarity.demo.cqrs.server.actors.account

import akka.testkit.{TestProbe, ImplicitSender, TestKit, TestActorRef}
import org.scalatest._
import matchers.MustMatchers
import org.clarity.demo.cqrs.server.actors.account.Account.{ReceiveAction, Balance, SendAction}
import akka.actor.ActorSystem
import org.clarity.demo.cqrs.server.actors.persistence.AccountStorage.AccountDetail

/**
 * @author Eirik Wang - eirik.wang@bekk.no
 * @since 2.2 TODO: Check version
 */
class AccountTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpec with MustMatchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AccountTest"))



  override def afterAll() {
    system.shutdown()
  }

  "Account" must {
    "change balance when riches are received" in {
      val actorRef = TestActorRef(new Account(AccountDetail(1, "name")))
      val actorRef2 = TestActorRef(new Account(AccountDetail(2, "name")))
      actorRef ! AccountBalance(1L, 0)
      actorRef ! ReceiveAction(1, 2, 1.0)
      actorRef ! ReceiveAction(1, 2, 2.0)
      actorRef ! Balance
      expectMsg(Cleared)
      expectMsg(Cleared)
      expectMsg(AccountBalance(1, 3.0))
    }

    "place items in backlog while initializing" in {
      val actorRef = TestActorRef(new Account(AccountDetail(1, "name")))
      actorRef ! ReceiveAction(1, 2, 1.0)
      actorRef ! ReceiveAction(1, 2, 2.0)
      actorRef ! Balance
      actorRef ! AccountBalance(1L, 0)
      expectMsg(Cleared)
      expectMsg(Cleared)
      expectMsg(AccountBalance(1, 3.0))
    }

    "change balance when riches is spread" in {
      val actorRef = TestActorRef(new Account(AccountDetail(1, "name")))
      val probe = TestProbe()
      actorRef ! AccountBalance(1L, 0)
      actorRef ! ReceiveAction(1, 2, 3.0)
      val action = SendAction(2, 1, 1.0, probe.ref)
      actorRef ! action
      actorRef ! Balance
      expectMsg(Cleared)
      expectMsg(AccountBalance(1, 2.0))
      probe.expectMsg(action.genReceiveAction())

    }

    "reject when trying to use more than ur riches current" in {
      val actorRef = TestActorRef(new Account(AccountDetail(1, "name")))
      val nextActor = TestActorRef(new Account(AccountDetail(2, "name")))
      actorRef ! AccountBalance(1L, 0)
      actorRef ! ReceiveAction(1, 2, 3.0)
      actorRef ! SendAction(2, 1, 4.0, nextActor)
      actorRef ! Balance
      expectMsg(Cleared)
      expectMsg(Rejected)
      expectMsg(AccountBalance(1, 3.0))
    }
  }
}
