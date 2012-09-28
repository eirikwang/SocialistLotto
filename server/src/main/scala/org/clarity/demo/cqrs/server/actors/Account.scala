package org.clarity.demo.cqrs.server.actors

import akka.actor.{Props, Actor}
import akka.pattern.ask
import db.AccountOperation
import db.AccountOperation.{Put, Get}
import akka.util.Timeout
import akka.util.duration._
import akka.dispatch.Await
import org.clarity.demo.cqrs.server.objects.UserAccount

case class SendAction(to: Long, amount: Double)
case class ReceiveAction(from: Long, amount: Double)
case class Transaction(from:Long, to:Long, amount:Double)
class Account() extends Actor {
  val account = context.actorOf(Props[AccountOperation], "acountops")
  implicit val timeout = Timeout(5 seconds)


  case object AddIntrest
  case object Balance

  case object Rejected

  case object Cleared


  protected def receive: PartialFunction[Any, Unit] = {
    //case AddIntrest => balance = balance * 1.05
    //case Balance => sender ! balance
    case p: SendAction => {
      println("Pay: " + p)
      val accountf = account ? Get(p.to)
      val a = Await.result(accountf, timeout.duration).asInstanceOf[UserAccount]

      if (a.balance < p.amount) sender ! Rejected
      else {
        account! Put(a.copy(balance = a.balance - p.amount))
        sender ! Cleared
      }
    }
    case p: ReceiveAction => {
      println("Receive: " + p)
      val accountf = account ? Get(p.from)
      val a = Await.result(accountf, timeout.duration).asInstanceOf[UserAccount]
      account ! a.copy(balance = a.balance + p.amount)
      sender ! Cleared
    }

  }
}
