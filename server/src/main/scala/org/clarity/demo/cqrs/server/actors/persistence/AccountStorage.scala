package org.clarity.demo.cqrs.server.actors.persistence

import akka.actor.{ActorSystem, Actor}
import com.hazelcast.client.HazelcastClient
import com.hazelcast.core.{MultiMap, IMap}
import operations.BalanceCallable
import org.clarity.demo.cqrs.server.actors.{AccountBalance, AccountChange}
import org.clarity.demo.cqrs.server.actors.persistence.AccountStorage._
import java.util.concurrent.{Callable, TimeUnit}
import akka.util.Deadline
import util.control.Exception.allCatch
import akka.util.duration._
import org.clarity.demo.cqrs.server.actors.Account.Balance
import akka.dispatch.{Create, ExecutionContext, Promise}
import org.clarity.demo.cqrs.server.actors.persistence.AccountStorage.AccountOperation
import org.clarity.demo.cqrs.server.actors.persistence.AccountStorage.BalanceOperation
import collection.JavaConversions
import org.scalatest.tools.RunningState
import org.clarity.demo.cqrs.server.SystemState.Started
import org.clarity.demo.cqrs.server.persistence.BalanceCallable2


/**
 * @author Eirik Wang - eirik.wang@bekk.no
 * @since 2.2 TODO: Check version
 */
object AccountStorage {

  case class AccountOperation(account: Long, participant: Long, amount: Double, timestamp: Long)
  case class BalanceOperation(account: Long)
  case object AllAccountsOperation
  case class AccountDetail(id:Long, name: String)

}

class AccountStorage(client: HazelcastClient) extends Actor {
  lazy val accountHistory: MultiMap[Long, AccountOperation] = client.getMultiMap("accountHistory")
  lazy val accountBalance: IMap[Long, AccountBalance] = client.getMap("accountBalance")
  lazy val accountDetail: IMap[Long, AccountDetail] = client.getMap("accountDetail")
  val actorSystem = context.system
  val executionContext = ExecutionContext.defaultExecutionContext(context.system)

  protected def receive = {
    case Create => sender ! Started
    case o: AccountChange => {
      accountHistory.put(o.account, AccountOperation(o.account, o.participant, o.balanceChange(), System.currentTimeMillis()))
      val balance: AccountBalance = accountBalance.tryLockAndGet(o.account, 5, TimeUnit.SECONDS)
      val newBalance: AccountBalance = balance.change(o.balanceChange())
      accountBalance.putAndUnlock(o.account, newBalance)


      //val res = client.getExecutorService.submit(BalanceCallable(client.getName, o.account, o.balanceChange()))
      //val result = new akka.dispatch.DefaultPromise[AccountBalance]()(executionContext)
      //pollJavaFuture(res, result)(actorSystem)
      sender! newBalance
    }
    case BalanceOperation(account:Long) => {
      if(!accountBalance.containsKey(account)) sender!AccountBalance(account, 0)
      else sender! accountBalance.get(account)
    }
    case AllAccountsOperation => {
      sender ! JavaConversions.mapAsScalaMap(accountDetail)
    }
  }

  def pollJavaFuture[T](javaFuture: java.util.concurrent.Future[T], promise: akka.dispatch.Promise[T], maybeTimeout: Option[Deadline] = None)(implicit system: ActorSystem) {
    if (maybeTimeout.exists(_.isOverdue())) {
      javaFuture.cancel(true)
    }
    if (javaFuture.isDone || javaFuture.isCancelled) {
      promise.complete(allCatch either {
        javaFuture.get
      })
    } else {
      system.scheduler.scheduleOnce(50 milliseconds) {
        pollJavaFuture(javaFuture, promise, maybeTimeout)
      }
    }
  }
}
