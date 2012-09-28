package org.clarity.demo.cqrs.server.actors.db

import akka.actor._
import akka.pattern.ask
import akka.dispatch.{Await, Create}
import akka.routing.RoundRobinRouter
import akka.util.{Timeout, Duration}
import akka.util.duration._
import akka.dispatch.Create
import org.clarity.demo.cqrs.server.actors.db.Hazelcast.GetMap

class Database extends Actor  with ActorLogging{
  implicit val timeout = Timeout(60 seconds)

  val hazelcast = context.actorOf(Props[Hazelcast].withRouter(
    RoundRobinRouter(nrOfInstances = 1)), "hazelcast")
  protected def receive = {
    case Create => {
      println("create db")
      println(hazelcast)
      sender ! Await.result( hazelcast ? Create, timeout.duration)
    }
    case Terminated(actorRef: ActorRef) => println(actorRef + "Terminated")
    case _ => println("Unexpected")
  }


}
