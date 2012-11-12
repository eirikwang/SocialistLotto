package org.clarity.demo.cqrs.server.actors

import akka.actor.{OneForOneStrategy, Props, ActorRef, Actor}
import org.clarity.demo.cqrs.server.actors.AccountHolder.FindAccount
import akka.actor.SupervisorStrategy.Restart
import persistence.AccountStorage.{AccountDetail, AllAccountsOperation}
import akka.pattern.ask
import org.clarity.demo.cqrs.server.SystemState.Started
import akka.util.Duration
import akka.util.Timeout
import akka.util.duration._
import org.clarity.demo.cqrs.server.exceptions.AccountNotAvailableException
import collection.JavaConversions
import akka.dispatch.{Await, Future}

/**
 * @author Eirik Wang - eirik.wang@bekk.no
 * @since 2.2 TODO: Check version
 */
object AccountHolder {

  case class FindAccount(id: Long)

}

class AccountHolder extends Actor {
  def accounts: ActorRef = context.actorFor("/user/persistence/accounts")
  implicit val timeout = Timeout(30 seconds)

  def findAccount(id: Long): ActorRef = {
    val actorFor: ActorRef = context.actorFor("./%s" format id)
    if (actorFor.isTerminated) {
      actorFor
    } else {
      context.actorOf(Props[Account])
    }
  }

  protected def receive = {
    case FindAccount(id) => sender ! findAccount(id)
    case o: Object => println("object " + o)
  }

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 100, withinTimeRange = Duration.Inf) {
    case x: AccountNotAvailableException => {
      println(x)
      Restart
    }
  }

  override def preStart() {
    println("Prepopulating database")
    val allAccounts = (accounts ? AllAccountsOperation)
    println("Ask sent: " + allAccounts)

    val accountDetails2 = Await.result(allAccounts, Duration.Inf)
    println("Ask received: " + accountDetails2)
    val accountDetails = accountDetails2.asInstanceOf[collection.mutable.Map[Long, AccountDetail]]

    for {
      account <- accountDetails
    } yield (context.actorOf(Props(new Account(account._2)), "" + account._2.id))
  }
}
