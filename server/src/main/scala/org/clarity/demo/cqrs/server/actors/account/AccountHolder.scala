package org.clarity.demo.cqrs.server.actors.account

import akka.actor.{OneForOneStrategy, Props, ActorRef, Actor}
import akka.actor.SupervisorStrategy.Restart
import akka.pattern.ask
import akka.util.Duration
import akka.util.Timeout
import akka.util.duration._
import org.clarity.demo.cqrs.server.exceptions.AccountNotAvailableException
import akka.dispatch.Await
import org.clarity.demo.cqrs.server.actors.account.AccountHolder.{CreateAccount, FindAccount}
import org.clarity.demo.cqrs.server.actors.persistence.AccountStorage.{AccountDetail, AllAccountsOperation}

/**
 * @author Eirik Wang - eirik.wang@bekk.no
 * @since 2.2 TODO: Check version
 */
object AccountHolder {

  trait AccountHolderOperation
  case class FindAccount(id: Long) extends AccountHolderOperation
  case class CreateAccount(name: String) extends AccountHolderOperation

  trait AccountHolderResult
  case class Created(accountDetails: AccountDetail) extends AccountHolderResult
  case class Failed(account: CreateAccount) extends AccountHolderResult
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
    case ca: CreateAccount => accounts forward ca
    case o: Object => println("object " + o)
  }

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 100, withinTimeRange = Duration.Inf) {
    case x: AccountNotAvailableException => {
      println(x)
      Restart
    }
  }

  override def preStart() {
    val allAccounts = (accounts ? AllAccountsOperation)
    val accountDetails2 = Await.result(allAccounts, Duration.Inf)
    val accountDetails = accountDetails2.asInstanceOf[collection.mutable.Map[Long, AccountDetail]]
    for {
      account <- accountDetails
    } yield {
      (context.actorOf(Props(new Account(account._2)), "" + account._2.id))
    }
  }
}
