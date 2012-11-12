package org.clarity.demo.cqrs.server.actors.db

import akka.actor.{ActorRef, Actor}
import akka.pattern.ask
import akka.dispatch.Await
import akka.util.Timeout
import akka.util.duration._
import com.hazelcast.core.Transaction
import org.clarity.demo.cqrs.server.actors.db.AccountOperation.Put
import org.clarity.demo.cqrs.server.objects.UserAccount
import org.clarity.demo.cqrs.server.actors.db.Hazelcast.{GetClient, GetMap}
import org.clarity.demo.cqrs.server.actors.db.AccountOperation.Change
import org.clarity.demo.cqrs.server.actors.db.AccountOperation.Get
import com.hazelcast.client.HazelcastClient


object AccountOperation {

  trait AccountEvent
  case class BeginTransaction() extends AccountEvent
  case class CommitTransaction() extends AccountEvent
  case class Get(id: Long) extends AccountEvent
  case class Put(account: UserAccount) extends AccountEvent
  case class Change(id: Long, balChange: Double) extends AccountEvent
}

class AccountOperation extends Actor with AccountOps{
  implicit val timeout = Timeout(5 seconds)

  def hazelcast: ActorRef = context.actorFor("/user/persistence/hazelcast")

  protected def receive: Receive = {
    case get: Get => sender ! account(get)

    case put: Put => {
      context.system.eventStream.publish(put.account)
      accounts.put(put.account.id, put.account)
    }
    case put: Change => {
      val trans: Transaction = transaction
      trans.begin()
      val account: UserAccount = accounts.get(put.id)
      val newAccount: UserAccount = account.copy(balance = account.balance + put.balChange)
      accounts.put(put.id, newAccount)
      try {
        trans.commit()
      } catch {
        case e: Exception => trans.rollback()
      }
      context.system.eventStream.publish(newAccount)
      sender ! newAccount
    }
    case o: Object => println("illegal " + o); throw new Exception("illegal")
  }

  def accounts = {
    Await.result(hazelcast ? GetMap("accounts"), timeout.duration).asInstanceOf[java.util.Map[Long, UserAccount]]
  }

  def transaction = {
    Await.result(hazelcast ? GetClient(), timeout.duration).asInstanceOf[HazelcastClient].getTransaction
  }

  def account(get: Get): UserAccount = {
    val account: UserAccount = accounts.get(get.id)
    if (account == null) {
      val account1: UserAccount = new UserAccount(get.id)
      accounts.put(get.id, account1)
    }
    account
  }

}

