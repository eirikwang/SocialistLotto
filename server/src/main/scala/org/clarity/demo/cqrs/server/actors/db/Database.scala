package org.clarity.demo.cqrs.server.actors.db

import akka.actor._
import akka.pattern.ask
import akka.dispatch.Await
import akka.routing.RoundRobinRouter
import akka.util.Timeout
import akka.util.duration._
import akka.dispatch.Create
import org.clarity.demo.cqrs.server.actors.persistence.AccountStorage
import com.hazelcast.client.{ClientConfig, HazelcastClient}

class Database extends Actor with ActorLogging {
  implicit val timeout = Timeout(60 seconds)
  val client = HazelcastClient.newHazelcastClient(new ClientConfig() {
    addAddress("localhost")
  })

  val accounts = context.actorOf(Props(new AccountStorage(client)).withRouter(
    RoundRobinRouter(nrOfInstances = 5)), "accounts")

  protected def receive = {
    case Create => {
      println("Creating")
      accounts forward Create
    }
    case Terminated(actorRef: ActorRef) => println(actorRef + "Terminated")
    case _ => println("Unexpected")
  }

  override def preStart() {
    println(accounts)
      accounts!"EnsureStarted"
  }
}
