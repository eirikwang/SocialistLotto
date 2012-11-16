package org.clarity.demo.cqrs.server.actors.account

import akka.actor.{OneForOneStrategy, Props, ActorRef, Actor}
import akka.actor.SupervisorStrategy.{Resume, Escalate, Restart}
import akka.pattern.ask
import akka.util.Duration
import akka.util.Timeout
import akka.util.duration._
import org.clarity.demo.cqrs.server.exceptions.{AccountClosedException, AccountNotAvailableException}
import akka.dispatch.{Create, Await}
import org.clarity.demo.cqrs.server.actors.account.AccountHolder.{Send, CreateAccount, FindAccount}
import org.clarity.demo.cqrs.server.actors.persistence.AccountStorage.{AccountDetail, AllAccountsOperation}
import org.clarity.demo.cqrs.server.actors.db.{Database, DbClient}
import org.clarity.demo.cqrs.server.actors.account.Account.SendAction
import org.clarity.demo.cqrs.server.objects.UserTransaction
import org.clarity.demo.cqrs.server.db

/**
 * @author Eirik Wang - eirik.wang@bekk.no
 * @since 1.0
 */
object AccountHolder {

  @SerialVersionUID(43l) trait AccountHolderOperation
  case class FindAccount(id: Long) extends AccountHolderOperation
  @SerialVersionUID(42l) case class CreateAccount(name: String) extends AccountHolderOperation
  @SerialVersionUID(40l) case class Send(transaction: UserTransaction) extends AccountHolderOperation


  @SerialVersionUID(44l)trait AccountHolderResult
  @SerialVersionUID(45l)case class Created(accountDetails: AccountDetail) extends AccountHolderResult
  @SerialVersionUID(46l)case class Failed(account: CreateAccount) extends AccountHolderResult
}

class AccountHolder extends Actor with DbClient with Serializable{

  implicit val timeout = Timeout(30 seconds)
  val accountStorage: ActorRef = createAccountStorage()

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
          val payer = context.actorFor("" + t.from) ? SendAction(t.to, t.from, t.amount, context.actorFor("" + t.to), sender)
          payer map {
            x =>
              x match {
                case Cleared => println("Ok transaction"); sender!"ok"
                case Rejected => println("Rejected transaction"); sender!"rejected"
              }
          }
        }
    case o:Object => {
      println("Received in AH: " + o)
    }
  }

  override val supervisorStrategy = OneForOneStrategy(
    maxNrOfRetries = 100, withinTimeRange = Duration.Inf) {
    case c:AccountClosedException => Resume
    case c:AccountNotAvailableException => Restart
    case c:Exception => Restart
    case _ => Escalate
  }

  override def preStart() {
    Await.result(accountStorage ? Create, timeout.duration)
    val accountDetails2 = Await.result(accountStorage ? AllAccountsOperation, Duration.Inf)
    val accountDetails = accountDetails2.asInstanceOf[collection.mutable.Map[Long, AccountDetail]]
    println(for {
      account <- accountDetails
    } yield {
      (context.actorOf(Props(new Account(accountStorage, account._2)), "" + account._2.id))
    })
  }
}
