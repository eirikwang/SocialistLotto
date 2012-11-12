package org.clarity.demo.cqrs.server.actors.persistence.operations

import java.util.concurrent.{TimeUnit, Callable}
import org.clarity.demo.cqrs.server.actors.{AccountChange, AccountBalance}
import com.hazelcast.core.{HazelcastInstance, Hazelcast, IMap}
import com.hazelcast.client.HazelcastClient
import com.hazelcast.config.Config

/**
 * @author Eirik Wang - eirik.wang@bekk.no
 * @since 2.2 TODO: Check version
 */
case class BalanceCallable(config: String, account:Long, amount: Double) extends Callable[AccountBalance] {
  def call(): AccountBalance = {
    val client = Hazelcast.getHazelcastInstanceByName(config)
    val map: IMap[Long, AccountBalance] = client.getMap("accountBalance")
    println("Client: " + client)
    val balance: AccountBalance = map.tryLockAndGet(account, 5, TimeUnit.SECONDS)
    println("Balance: " + balance)
    val newBalance: AccountBalance = balance.change(amount)
    println("NewBalance: " + balance)
    map.putAndUnlock(account, newBalance)
    newBalance
  }
}
