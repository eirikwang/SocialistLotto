package org.clarity.demo.cqrs.server.dao


import org.clarity.demo.cqrs.domain.{Task, Project}

import com.hazelcast.client.{ClientConfig, HazelcastClient}
import com.hazelcast.core.IdGenerator

trait ProjectWriteDao {
  def addTaskToProject(task: Task, projectId: Long): Task

  def createProject(project: Project): Project
}

class ProjectWriteDaoImpl() extends ProjectWriteDao {

  def hazelcast = HazelcastClient.newHazelcastClient(new ClientConfig() {
    addAddress("localhost")
  })

  def taskIdGenerator: IdGenerator = hazelcast.getIdGenerator("tasks")

  def projectIdGenerator: IdGenerator = hazelcast.getIdGenerator("projects")

  def projects = hazelcast.getMap[Long, Project]("projects")

  def tasks = hazelcast.getMap[Long, Project]("tasks")


  def createProject(project: Project): Project = {
    val newProject = project.copy(id = projectIdGenerator.newId())
    projects.put(newProject.id, newProject)
    projects.get(newProject.id)
  }

  def addTaskToProject(task: Task, projectId: Long): Task = {
    def tasks = hazelcast.getMap[Long, Task]("tasks")
    val newTask = task.copy(id = taskIdGenerator.newId())
    tasks.put(newTask.id, newTask)
  }
}
