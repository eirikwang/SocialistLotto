package org.clarity.demo.cqrs.domain

import org.scalatest.FunSuite
import org.joda.time.DateTime

class ProjectTest extends FunSuite {

  test("check that tasks is immutable") {
    def p = new Project(name = "Name")
    p.tasks + Task(1, 1, p)
    assert(p.tasks.size == 0)
  }

}
