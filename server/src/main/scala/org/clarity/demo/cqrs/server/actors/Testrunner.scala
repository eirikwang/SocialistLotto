package org.clarity.demo.cqrs.server.actors

import akka.actor.{ActorRef, Props, ActorSystem}
import akka.pattern.ask

import db.{Hazelcast, Database}
import akka.util.Timeout
import akka.util.duration._
import akka.dispatch.{Future, Await, Create}
import db.Hazelcast.GetMap
import org.clarity.demo.cqrs.server.actors.PaymentProcessor.Send
import org.clarity.demo.cqrs.server.objects.UserAccount

object Testrunner extends App {

  implicit val timeout = Timeout(5 seconds)
  calculate(nrOfWorkers = 4, nrOfElements = 10000, nrOfMessages = 10000)

  // actors and messages ...

  def calculate(nrOfWorkers: Int, nrOfElements: Int, nrOfMessages: Int) {
    // Create an Akka system
    val system = ActorSystem("PiSystem")

    // create the result listener, which will print the result and
    // shutdown the system
    val listener = system.actorOf(Props[Database], name = "db")
    listener ! Create

    val actorFor: ActorRef = system.actorOf(Props[Hazelcast])
    val awaitable: Future[Any] = actorFor ? GetMap("accounts")
    // create the master
    val paymentProsessor = system.actorOf(Props[PaymentProcessor], name = "paymentProsessor")
    println("sending: ")
    paymentProsessor ! Send(Transaction(1, 2, 100.0))
    paymentProsessor ! Send(Transaction(2, 1, 100.0))

    Thread.sleep(99999999)

  }
}
