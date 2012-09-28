package org.clarity.demo.cqrs.server.actors.db

import akka.actor.{Props, Actor}
import akka.pattern.ask
import akka.dispatch.Create

class Database extends Actor{
  val hazelcast = context.actorOf(Props[Hazelcast], "hazelcast")
  protected def receive = {
    case Create => {
      println("create db")
      hazelcast ! Create
    }
  }
}
