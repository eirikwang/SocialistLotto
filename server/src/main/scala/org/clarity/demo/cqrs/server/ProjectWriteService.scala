package org.clarity.demo.cqrs.server

import dao.ProjectWriteDao
import javax.inject.Inject
import org.clarity.demo.cqrs.domain.{Task, Project}

class ProjectWriteService @Inject()(
                                     val projectWriteDao: ProjectWriteDao
                                     ) {
  def createProject(project: Project): Project = {
    projectWriteDao.createProject(project)
  }
  def addTask(project: Long, task: Task): Task = {
    projectWriteDao.addTaskToProject(task, project)
  }

}
