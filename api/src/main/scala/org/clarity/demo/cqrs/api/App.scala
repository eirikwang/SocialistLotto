package org.clarity.demo.cqrs.api

import com.typesafe.play.mini._
import play.api.mvc._
import play.api.mvc.Results._
import com.codahale.jerkson.Json._
import org.clarity.demo.cqrs.service._
import org.clarity.demo.cqrs.domain.Project

object App extends com.typesafe.play.mini.Application {

  def route = Routes(
  new ProjectAction().route
  )
}

