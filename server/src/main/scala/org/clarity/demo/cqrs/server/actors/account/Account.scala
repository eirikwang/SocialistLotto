package org.clarity.demo.cqrs.server.actors.account

import akka.actor.{Failed, ActorRef, Props, Actor}
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import akka.dispatch.Await
import akka.event.LoggingReceive
import org.clarity.demo.cqrs.server.actors.persistence.AccountStorage.{AccountDetail, BalanceOperation}
import org.clarity.demo.cqrs.server.actors.account.Account.{ReceiveAction, SendAction, Balance, AccountOperation}


abstract case class TransactionStatus()
case class Rejected(oper: AccountOperation) extends TransactionStatus
case class Cleared(oper: AccountOperation) extends TransactionStatus


trait AccountChange{
  implicit def account:Long
  implicit def participant:Long
  implicit def amount:Double
  def balanceChange():Double = {
    this match {
      case a: SendAction => -a.amount
      case a: ReceiveAction => a.amount
    }
  }
}
object Account {
  trait AccountOperation
  case class SendAction(participant: Long, account:Long, amount: Double, send:ActorRef) extends AccountOperation with AccountChange{
    def genReceiveAction() = ReceiveAction(participant, account, amount)
  }
  case class ReceiveAction(account: Long, participant:Long, amount: Double) extends AccountOperation with AccountChange
  case object Balance extends AccountOperation
}


class Account(accountOps:ActorRef, account: AccountDetail) extends Actor {

  implicit val timeout = Timeout(5 seconds)
  var backlog = IndexedSeq.empty[(ActorRef, AccountOperation)]

  var balance:Double = 0

  protected def receive = uninitialized

  def uninitialized = LoggingReceive {
    // Response from prestart operations.
    case AccountBalance(account: Long, balance: Double) => {
      this.balance = balance
      context.become(initialized)
      println("running backlog")
      for ((replyTo, msg) ← backlog) self.tell(msg, sender = replyTo)
    }
    case ao:AccountOperation => {
      println("backlogging: " + ao)
      backlog = backlog :+ (sender, ao)
    }
    case o: Object => {
      println("unknown2: " + o)
    }
  }
  def initialized: Receive = {
    case Balance => sender ! AccountBalance(account.id, balance)
    case p: SendAction => {
      println("Send: " + p)
      if (balance < p.amount){
        sender ! Rejected
      } else {
        balance -= p.amount
        accountOps ! p
        p.send ! p.genReceiveAction()
      }
    }
    case p: ReceiveAction => {
      println("receive: " + p)
      balance += p.amount
      accountOps ! p
      context.system.eventStream.publish(AccountBalance(account.id, balance))
      sender ! Cleared
    }
    case o: Object => {
      println("unknown: " + o)
    }
  }

  override def preStart() {
    accountOps ! BalanceOperation(account.id)
  }
}


