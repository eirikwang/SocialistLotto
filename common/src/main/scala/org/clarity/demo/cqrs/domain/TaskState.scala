package org.clarity.demo.cqrs.domain

object TaskState extends Enumeration {
  type TaskState = Value
  val Created = Value(0, "Created")
  val InProgress = Value(1, "InProgress")
  val Test = Value(2, "InProgress")
  val Done = Value(3, "Done")
}