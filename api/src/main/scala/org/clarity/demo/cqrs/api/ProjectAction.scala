package org.clarity.demo.cqrs.api

import play.api.mvc.{Action, AsyncResult}
import play.api.mvc.Results._


import akka.actor.{Props, ActorSystem}
import akka.pattern.Patterns._
import akka.pattern.ask
import akka.pattern.pipe
import com.typesafe.play.mini.{Routes, POST, Path, GET}
import play.api.data.Form
import play.api.data.Forms._
import org.clarity.demo.cqrs.domain.Project
import org.clarity.demo.cqrs.service.{ProjectReadServiceImpl, ProjectReadService}
import com.codahale.jerkson.Json
import akka.util.Timeout
import akka.util.duration._
import play.api.libs.concurrent._
import play.api.libs.concurrent.execution.defaultContext
import play.api.libs.iteratee.Concurrent


class ProjectAction {
  lazy val projectService: ProjectReadService = new ProjectReadServiceImpl
  lazy val system = ActorSystem("ShakespeareGenerator")
  lazy val projectActor = system.actorOf(Props[ProjectActor], "projectActor")
  implicit val timeout = Timeout(5 seconds)
  val (rawStream, channel) = Concurrent.broadcast[Project]

  def route = Routes {
    /*case GET(Path("/events")) => Action {

        Ok.stream(clock &> Comet(callback = "parent.clockChanged"))
    } */

    case GET(Path("/project")) => Action {
      request =>
        val result: Iterable[Project] = projectService.findProjects()
        Ok(Json.generate(result)).as("text/json")
    }
    case POST(Path("/project")) => Action {
      implicit request =>
        val project = projectForm.bindFromRequest.get
        AsyncResult {
          (projectActor ? project).mapTo[Project].asPromise.map {
            result => {
              println("result: " + result)
              Ok(Json.generate(result))
            }
          }
        }
    }
  }

  val projectForm: Form[Project] = Form(
    mapping(
      "name" -> nonEmptyText
    )((name) => Project(name = name))
      ((project: Project) => Some(project.name))
  )
}
