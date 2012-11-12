package org.clarity.demo.cqrs.server.actors

import account.AccountHolder
import akka.actor.{Actor, ActorRef, Props, ActorSystem}
import akka.pattern.ask

import db.{Database, Hazelcast}
import akka.util.Timeout
import akka.util.duration._
import akka.dispatch.{Future, Await, Create}
import db.Hazelcast.GetMap
import org.clarity.demo.cqrs.server.actors.PaymentProcessor.Send
import org.clarity.demo.cqrs.server.objects.{UserTransaction, UserAccount}
import java.util
import scala.util.Random

class BalanceSystem {

  implicit val timeout = Timeout(60 seconds)
  val system = ActorSystem("BalanceSystem")
  val db = system.actorOf(Props[Database], name = "persistence")
  val paymentProsessor = system.actorOf(Props[PaymentProcessor], name = "paymentProsessor")
  def userListener = system.actorOf(Props[UserListener], name = "userListener")

  def startup() {
    // Create an Akka system

    // create the result listener, which will print the result and
    // shutdown the system
    println("Creating db")
    Await.result(db ? Create, timeout.duration)
    println("Db created")

    val accounts = system.actorOf(Props[AccountHolder], name = "accounts")

    system.eventStream.subscribe(userListener, classOf[UserAccount])

  }
  def pay() {    // create the master
    paymentProsessor ! Send(UserTransaction(Random.nextInt(100), Random.nextInt(100), 1))
  }
}
class UserListener extends Actor{
  protected def receive = {
    case u: UserAccount => println("Account changed: " + u)
  }
}
  object runner {

  def main(args: Array[String]) {
    val app = new BalanceSystem
    app.startup
    println("Started Testrunner Application")
    while (true) {
      Thread.sleep(1000)
      app.pay
    }
  }
}
