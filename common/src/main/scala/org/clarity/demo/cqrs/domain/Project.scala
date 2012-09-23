package org.clarity.demo.cqrs.domain

import org.joda.time.DateTime

case class Project(id: Long = -1,
                   version: Long = 1,
                   name: String,
                   status: String = "Active",
                   description: String = "",
                   startDate: DateTime = new DateTime(),
                   tasks: Set[Task] = Set()
                    ) extends Versionable
