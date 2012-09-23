package org.clarity.demo.cqrs.domain

import TaskState._

case class Task(id: Long,
                version: Long,
                project: Project,
                state: TaskState = Created,
                comments: Set[Comment] = Set()
                 ) extends Versionable {

}


