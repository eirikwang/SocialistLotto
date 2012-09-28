package org.clarity.demo.cqrs.server.actors.db

import akka.actor.{Props, ActorRef, Actor}
import akka.pattern.ask
import akka.dispatch.{Await, Future}
import akka.util.Timeout
import akka.util.duration._
import org.clarity.demo.cqrs.server.actors.Account
import org.clarity.demo.cqrs.server.actors.db.AccountOperation.{Put, Get}
import org.clarity.demo.cqrs.server.objects.UserAccount
import collection.JavaConversions


object AccountOperation {

  case class Get(id: Long)
  case class Put(account:UserAccount)

}

class AccountOperation extends Actor {
  val hazelcast: ActorRef = context.actorFor("/db/hazelcast")
  implicit val timeout = Timeout(5 seconds)
  protected def receive = {
    case get: Get => {
      val future: Future[Any] = hazelcast ? "accounts"
      println("getting")
      val accounts = Await.result(future, timeout.duration).asInstanceOf[java.util.Map[Long, UserAccount]]
      println("gotten")
      sender ! accounts.get(get.id)
    }
    case put:Put => {
      val future: Future[Any] = hazelcast ? "accounts"
      val accounts = Await.result(future, timeout.duration).asInstanceOf[java.util.Map[Long, UserAccount]]
      accounts.put(put.account.id, put.account)
    }

  }
}

