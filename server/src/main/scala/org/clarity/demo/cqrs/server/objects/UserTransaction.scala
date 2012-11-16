package org.clarity.demo.cqrs.server.objects

@SerialVersionUID(445l) case class UserTransaction(from:Long, to:Long, amount:Double)
