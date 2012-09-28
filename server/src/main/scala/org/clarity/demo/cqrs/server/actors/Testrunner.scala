package org.clarity.demo.cqrs.server.actors

import akka.actor.{Actor, ActorRef, Props, ActorSystem}
import akka.pattern.ask

import db.{Hazelcast, Database}
import akka.util.Timeout
import akka.util.duration._
import akka.dispatch.{Future, Await, Create}
import db.Hazelcast.GetMap
import org.clarity.demo.cqrs.server.actors.PaymentProcessor.Send
import org.clarity.demo.cqrs.server.objects.{UserTransaction, UserAccount}
import java.util
import scala.util.Random

class Testrunner {

  implicit val timeout = Timeout(60 seconds)
  // actors and messages ...
  val system = ActorSystem("PiSystem")
  val db = system.actorOf(Props[Database], name = "db")
  val paymentProsessor = system.actorOf(Props[PaymentProcessor], name = "paymentProsessor")
  def userListener = system.actorOf(Props[UserListener], name = "userListener")

  def startup() {
    // Create an Akka system

    // create the result listener, which will print the result and
    // shutdown the system
    println("Creating db")
    Await.result(db ? Create, timeout.duration)
    println("Db created")
    system.eventStream.subscribe(userListener, classOf[UserAccount])

    val awaitable: Future[Any] = system.actorFor("/user/db/hazelcast") ? GetMap("accounts")
    val map: util.Map[Long, UserAccount] = Await.result(awaitable, timeout.duration).asInstanceOf[util.Map[Long, UserAccount]]

    for (i<-(0 until 100)) map.put(i, new UserAccount(i, 100))
    map.put(2L, new UserAccount(2, 100))
    println("User added")

  }
  def pay() {    // create the master
    paymentProsessor ! Send(UserTransaction(Random.nextInt(100), Random.nextInt(100), 1.6))
  }
}
class UserListener extends Actor{
  protected def receive = {
    case u: UserAccount => println("Account changed: " + u)
  }
}
  object runner {

  def main(args: Array[String]) {
    val app = new Testrunner
    app.startup
    println("Started Testrunner Application")
    while (true) {
      Thread.sleep(100)
      app.pay
    }
  }
}
