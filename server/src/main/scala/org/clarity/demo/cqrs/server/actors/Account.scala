package org.clarity.demo.cqrs.server.actors

import akka.actor.{Props, Actor}
import akka.pattern.ask
import db.AccountOperation
import db.AccountOperation._
import akka.util.Timeout
import akka.util.duration._
import akka.dispatch.Await
import db.AccountOperation.BeginTransaction
import db.AccountOperation.Change
import db.AccountOperation.Get
import org.clarity.demo.cqrs.server.objects.UserAccount
import org.clarity.demo.cqrs.server.objects.UserAccount
import org.clarity.demo.cqrs.server.actors.ReceiveAction
import org.clarity.demo.cqrs.server.actors.TransactionStatus
import org.clarity.demo.cqrs.server.actors.SendAction

case class SendAction(to: Long, amount: Double)
case class ReceiveAction(from: Long, amount: Double)

abstract case class TransactionStatus()
case object Rejected extends TransactionStatus
case object Cleared extends TransactionStatus


class Account() extends Actor {
  var accountOps = context.actorOf(Props[AccountOperation], "acountops")
  implicit val timeout = Timeout(5 seconds)


  case object AddIntrest
  case object Balance


  protected def receive: PartialFunction[Any, Unit] = {
    //case AddIntrest => balance = balance * 1.05
    //case Balance => sender ! balance
    case p: SendAction => {
      val account:UserAccount = getAccount(p.to)
      if (account.balance < p.amount){
        sender ! Rejected
      }
      else {
        val ua:UserAccount = Await.result(accountOps? Change(account.id, - p.amount), timeout.duration).asInstanceOf[UserAccount]
        sender ! Cleared
      }
    }
    case p: ReceiveAction => {
      val account: UserAccount = getAccount(p.from)
      val ua:UserAccount = Await.result(accountOps? Change(account.id, p.amount), timeout.duration).asInstanceOf[UserAccount]
      context.system.eventStream.publish(ua)
      sender ! Cleared
    }

  }

  def getAccount(p: Long): UserAccount = {
    Await.result(accountOps ? Get(p), timeout.duration).asInstanceOf[UserAccount]
  }
}
