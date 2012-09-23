package org.clarity.demo.cqrs.domain

import org.joda.time.DateTime

case class Comment(id: Long,
                   version: Long,
                   comment: String,
                   user: String,
                   timestamp: DateTime
                    ) extends Versionable {

}
