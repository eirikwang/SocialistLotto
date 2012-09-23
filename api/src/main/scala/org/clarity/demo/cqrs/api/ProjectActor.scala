package org.clarity.demo.cqrs.api


import akka.actor.Actor
import org.clarity.demo.cqrs.domain.Project
import akka.dispatch.{Await, Future}
import org.clarity.demo.cqrs.service.{ProjectReadServiceImpl, ProjectReadService}
import org.clarity.demo.cqrs.server.ProjectWriteService
import org.clarity.demo.cqrs.server.dao.ProjectWriteDaoImpl

class ProjectActor extends Actor {
  lazy val projectService: ProjectReadService = new ProjectReadServiceImpl
  lazy val projectWriteService: ProjectWriteService = new ProjectWriteService(new ProjectWriteDaoImpl)

  protected def receive = {
    case find: Int =>
      println("find " + find )
      sender ! new Project(name = "")
    case save: Project =>
      val project = projectWriteService.createProject(save)
      sender ! project
  }
}
