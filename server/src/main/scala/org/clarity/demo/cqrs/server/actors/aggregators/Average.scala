package org.clarity.demo.cqrs.server.actors.aggregators

import akka.actor.{ActorRef, Props, Actor}
import org.clarity.demo.cqrs.server.actors.db.{AccountOps, AccountOperation}
import akka.util.Timeout
import akka.util.duration._
import java.util
import org.clarity.demo.cqrs.server.objects.UserAccount


/**
 * @author Eirik Wang - eirik.wang@bekk.no
 */
class Average extends Actor with AccountOps{
  implicit val timeout = Timeout(5 seconds)
  def hazelcast: ActorRef = context.actorFor("/user/persistence/hazelcast")
  protected def receive = {
    case "" => ""

  }
}
