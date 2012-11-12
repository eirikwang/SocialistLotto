package org.clarity.demo.cqrs.server.actors.db

import org.clarity.demo.cqrs.server.objects.UserAccount
import akka.actor.ActorRef
import akka.dispatch.Await
import akka.pattern.ask
import org.clarity.demo.cqrs.server.actors.db.Hazelcast.{GetClient, GetMap}
import com.hazelcast.client.HazelcastClient
import akka.util.Timeout


/**
 * @author Eirik Wang - eirik.wang@bekk.no
 * @since 2.2 TODO: Check version
 */
trait AccountOps {
  def hazelcast: ActorRef

  def accounts()(implicit timeout: Timeout) = {
    Await.result(hazelcast ? GetMap("accounts"), timeout.duration).asInstanceOf[java.util.Map[Long, UserAccount]]
  }

  def transaction()(implicit timeout: Timeout) = {
    Await.result(hazelcast ? GetClient(), timeout.duration).asInstanceOf[HazelcastClient].getTransaction
  }

  def account(id:Long)(implicit timeout: Timeout): UserAccount = {
    val account: UserAccount = accounts.get(id)
    if (account == null) {
      val account1: UserAccount = new UserAccount(id)
      accounts.put(id, account1)
    }
    account
  }
}
