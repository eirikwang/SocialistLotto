package org.clarity.demo.cqrs.server.actors.account

import akka.actor.{OneForOneStrategy, Props, ActorRef, Actor}
import akka.actor.SupervisorStrategy.Restart
import akka.pattern.ask
import akka.util.Duration
import akka.util.Timeout
import akka.util.duration._
import org.clarity.demo.cqrs.server.exceptions.AccountNotAvailableException
import akka.dispatch.Await
import org.clarity.demo.cqrs.server.actors.account.AccountHolder.{Send, CreateAccount, FindAccount}
import org.clarity.demo.cqrs.server.actors.persistence.AccountStorage.{AccountDetail, AllAccountsOperation}
import org.clarity.demo.cqrs.server.actors.db.{Database, DbClient}
import org.clarity.demo.cqrs.server.actors.account.Account.SendAction
import org.clarity.demo.cqrs.server.objects.UserTransaction

/**
 * @author Eirik Wang - eirik.wang@bekk.no
 * @since 1.0
 */
object AccountHolder {

  trait AccountHolderOperation
  case class FindAccount(id: Long) extends AccountHolderOperation
  case class CreateAccount(name: String) extends AccountHolderOperation
  case class Send(transaction: UserTransaction) extends AccountHolderOperation

  trait AccountHolderResult
  case class Created(accountDetails: AccountDetail) extends AccountHolderResult
  case class Failed(account: CreateAccount) extends AccountHolderResult
}

class AccountHolder extends Actor with DbClient{

  implicit val timeout = Timeout(30 seconds)
  def accountStorage: ActorRef = createAccountStorage()

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
    case ca: CreateAccount => accountStorage forward ca
    case Send(t) => {
          println("paying: " + t)
          val payer = context.actorFor("./" + t.from) ? SendAction(t.to, t.from, t.amount, context.actorFor("./" + t.to))
          payer map {
            x =>
              x match {
                case Cleared => println("Ok transaction")
                case Rejected => println("Rejected transaction")
              }
          }
        }
  }

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 100, withinTimeRange = Duration.Inf) {
    case x: AccountNotAvailableException => {
      Restart
    }
  }

  override def preStart() {
    val accountDetails2 = Await.result(accountStorage ? AllAccountsOperation, Duration.Inf)
    val accountDetails = accountDetails2.asInstanceOf[collection.mutable.Map[Long, AccountDetail]]
    for {
      account <- accountDetails
    } yield {
      (context.actorOf(Props(new Account(accountStorage, account._2)), "" + account._2.id))
    }
  }
}
