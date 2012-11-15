package org.clarity.demo.presentation

import akka.actor.{Props, Actor, ActorSystem}
import akka.kernel.Bootable
import org.clarity.demo.presentation.Counter.NTimes
import org.clarity.demo.presentation.Worker.{Result, Calculate}
import org.clarity.demo.cqrs.server.actors.persistence.AccountStorage
import akka.routing.RoundRobinRouter

object DemoRunner{
  def main(args: Array[String]) {
    new DemoRunner().startup()
  }
}
class DemoRunner extends Bootable {
  val system = ActorSystem("BekkFagdag")

  def startup() {
    var counter = system.actorOf(Props[Counter], "counter")
    counter ! NTimes(0 to 10)
  }

  def shutdown() {
    system.shutdown()
  }
}

object Counter {
  case class NTimes(range:Range)
}
class Counter extends Actor {
  val worker = context.actorOf(Props[Worker].withRouter(
        RoundRobinRouter(nrOfInstances = 2)), "persistence")
  protected def receive = {
    case NTimes(range: Range) => {
      for( number <- range) yield (
        worker ! Calculate(number, 100)
      )
    }
    case Result(number:Int, res:Int) => {
      println("Result for :%d is %d".format(number, res))
    }
  }
}
object Worker {
  case class Calculate(number:Int, times:Int)
  case class Result(number: Int, result:Int)
}
class Worker extends Actor {
  protected def receive = {
    case Calculate(number:Int, times:Int) => {
      val res:Int = 0 to times reduceLeft(_ + number/_)
      sender ! Result(number, res)
    }

  }
}
