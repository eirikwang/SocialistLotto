package org.clarity.demo.cqrs.server

import actors.account.AccountHolder
import actors.account.AccountHolder.CreateAccount
import actors.db.Database
import actors.UserListener
import akka.kernel.Bootable
import akka.actor.{Props, ActorSystem}
import akka.dispatch.{Create, Await}
import akka.util.Timeout
import akka.util.duration._
import akka.pattern.ask
import objects.UserAccount

/**
 * @author Eirik Wang - eirik.wang@bekk.no
 * @since 2.2 TODO: Check version
 */
object Server {
  def main(args: Array[String]) {
    new Server().startup()
  }
}
class Server extends Bootable{
  implicit val timeout = Timeout(60 seconds)

  val system = ActorSystem("SocialistLotto")

  def startup() {
    def userListener = system.actorOf(Props[UserListener], name = "userListener")
    val accounts = system.actorOf(Props[AccountHolder], name = "accounts")
    system.eventStream.subscribe(userListener, classOf[UserAccount])
  }

  def shutdown() {
    system.shutdown()
  }}
