package org.clarity.demo.cqrs.service

import org.clarity.demo.cqrs.domain.Project
import com.hazelcast.client.{ClientConfig, HazelcastClient}
import com.hazelcast.query.SqlPredicate
import collection.JavaConversions

trait ProjectReadService {
  def findProjects(): Iterable[Project]
}

class ProjectReadServiceImpl extends ProjectReadService {
  def hazelcast = HazelcastClient.newHazelcastClient(new ClientConfig() {
    addAddress("localhost")
  })

  def projects = hazelcast.getMap[Long, Project]("projects")

  def tasks = hazelcast.getMap[Long, Project]("tasks")

  def findProjects() = {
    def res = projects.values(new SqlPredicate("status = 'Active'"))
    JavaConversions.collectionAsScalaIterable(res)
  }
}
