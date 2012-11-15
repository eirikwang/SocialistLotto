package org.clarity.demo.cqrs.server.actors.db

import com.hazelcast.client.{ClientConfig, HazelcastClient}
import akka.actor.{ActorRef, ActorContext, Props}
import org.clarity.demo.cqrs.server.actors.persistence.AccountStorage
import akka.routing.RoundRobinRouter

/**
 * @author Eirik Wang - eirik.wang@bekk.no
 * @since 1.0
 */
trait DbClient {
  val client = HazelcastClient.newHazelcastClient(new ClientConfig() {
    addAddress("localhost")
  })
  def createAccountStorage()(implicit context:ActorContext): ActorRef = {
    context.actorOf(Props(new AccountStorage(client)).withRouter(
      RoundRobinRouter(nrOfInstances = 5)), "persistence")
  }
}
