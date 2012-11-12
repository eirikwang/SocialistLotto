package org.clarity.demo.cqrs.server.actors

import account.{Rejected, Cleared, Account}
import akka.actor.{ActorRef, Props, Actor}
import akka.pattern.ask
import org.clarity.demo.cqrs.server.actors.PaymentProcessor.Send
import akka.util.Timeout
import akka.util.duration._
import org.clarity.demo.cqrs.server.objects.UserTransaction
import org.clarity.demo.cqrs.server.actors.account.Account.SendAction
import persistence.AccountStorage.AccountDetail

object PaymentProcessor {

  case class Send(transaction: UserTransaction)

}

class PaymentProcessor extends Actor {
  implicit val timeout = Timeout(5 seconds)

  val account = context.actorOf(Props(new Account(AccountDetail(1, "name"))), name = "accountOps")
  val accountManager = context.actorFor("/user/accounts/")

  protected def receive = {
    case Send(t) => {
      println("paying: " + t)
      val payer = context.actorFor("/user/accounts/" + t.from) ? SendAction(t.to, t.from, t.amount, context.actorFor("/user/accounts/" + t.to))
      payer map {
        x =>
          x match {
            case Cleared => println("Ok transaction")
            case Rejected => println("Rejected transaction")
          }
      }
    }
  }

  protected def account(id: Long) = {
    val actorFor: ActorRef = context.actorFor("/user/accounts/" + id)
    if (!actorFor.isTerminated) {
      actorFor
    } else {
      context.actorOf(Props(new Account(AccountDetail(id, ""))), name = "accountOps")
    }

  }

  override def unhandled(message: Any) {
    println("Unhandled message: " + message)
    super.unhandled(message)
  }
}
