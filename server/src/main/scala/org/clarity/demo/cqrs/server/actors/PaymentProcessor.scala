package org.clarity.demo.cqrs.server.actors

import akka.actor.{Props, Actor}
import akka.pattern.ask
import org.clarity.demo.cqrs.server.actors.PaymentProcessor.Send
import akka.dispatch.{Await, Futures}
import akka.util.Timeout
import akka.util.duration._


object PaymentProcessor {
  case class Send(transaction:Transaction)
}
class PaymentProcessor extends Actor{
  implicit val timeout = Timeout(5 seconds)

  val account = context.actorOf(Props(new Account()), name = "account")
  protected def receive = {
    case Send(t) => {
      val payer = account? SendAction(t.to, t.amount)
      val receiver = account? ReceiveAction(t.from, t.amount)
      Await.result(payer, timeout.duration)
      Await.result(receiver, timeout.duration)
    }
  }
}
