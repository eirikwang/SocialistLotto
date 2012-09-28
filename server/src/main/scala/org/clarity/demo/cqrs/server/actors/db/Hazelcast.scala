package org.clarity.demo.cqrs.server.actors.db

import akka.actor.Actor
import com.hazelcast.client.{HazelcastClient, ClientConfig}
import org.clarity.demo.cqrs.server.actors.db.Hazelcast.GetMap

object Hazelcast {
  case class GetMap(name:String)

}

class Hazelcast extends Actor {
  val hazelcast = HazelcastClient.newHazelcastClient(new ClientConfig() {
    addAddress("localhost")
  })

  protected def receive = {
    case GetMap(name) => sender ! hazelcast.getMap(name)
  }
}
