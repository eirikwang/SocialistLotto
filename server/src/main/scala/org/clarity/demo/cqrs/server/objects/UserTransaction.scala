package org.clarity.demo.cqrs.server.objects

case class UserTransaction(from:Long, to:Long, amount:Double)
