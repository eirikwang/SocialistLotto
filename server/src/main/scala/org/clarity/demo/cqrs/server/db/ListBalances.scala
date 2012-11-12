package org.clarity.demo.cqrs.server.db

import com.hazelcast.client.{ClientConfig, HazelcastClient}
import com.hazelcast.core.IMap
import org.clarity.demo.cqrs.server.actors.persistence.AccountStorage.AccountDetail
import collection.JavaConversions
import org.clarity.demo.cqrs.server.actors.account.AccountBalance

/**
 * @author Eirik Wang - eirik.wang@bekk.no
 * @since 2.2 TODO: Check version
 */
object ListBalances {
  def main(args:Array[String]) {
    def hazelcast = HazelcastClient.newHazelcastClient(new ClientConfig() {
      addAddress("localhost")
    })
    val details: IMap[Long, AccountDetail] = hazelcast.getMap("accountDetail")
    val balance: IMap[Long, AccountBalance] = hazelcast.getMap("accountBalance")

    JavaConversions.mapAsScalaMap(balance).foreach(println(_))


  }

}
