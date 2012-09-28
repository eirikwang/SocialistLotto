package org.clarity.demo.cqrs.server.actors.db

import akka.actor.{ActorRef, Terminated, ActorLogging, Actor}
import com.hazelcast.client.{HazelcastClient, ClientConfig}
import org.clarity.demo.cqrs.server.actors.db.Hazelcast.{GetClient, GetTransaction, GetMap}
import akka.dispatch.Create
import akka.util.Timeout
import akka.util.duration._

object Hazelcast {
  case class GetMap(name:String)
  case class GetTransaction()
  case class GetClient()
}
class Hazelcast extends Actor with ActorLogging {
  implicit val timeout = Timeout(60 seconds)

  var hazelcast: HazelcastClient = null

  protected def receive = {
    case GetMap(name) => sender ! hazelcast.getMap(name)
    case Create => {
      println("Creating database")
      hazelcast = HazelcastClient.newHazelcastClient(new ClientConfig() {
        addAddress("localhost")
      })
      sender ! "Done"
    }
    case GetTransaction() => sender ! hazelcast.getTransaction
    case GetClient() => sender ! hazelcast
    case any:Any => throw new Exception("no such op: " + any)
  }

}
