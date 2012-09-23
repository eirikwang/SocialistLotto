package org.clarity.demo.cqrs.server.db

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.server.bio.SocketConnector
import com.hazelcast.core.{HazelcastInstance, Hazelcast}
import com.hazelcast.config.ClasspathXmlConfig
import org.clarity.demo.cqrs.domain.Project
import org.clarity.demo.cqrs.server.dao.{ProjectWriteDao, ProjectWriteDaoImpl}

object JettyServer {
  def projectWriteDao:ProjectWriteDao = new ProjectWriteDaoImpl
  def initHZ() = {
    val newHazelcastInstance: HazelcastInstance = Hazelcast.newHazelcastInstance(new ClasspathXmlConfig("hazelcast.xml"))
    projectWriteDao.createProject(new Project(name= "Eirik1"))
    projectWriteDao.createProject(new Project(name= "Eirik2"))
    projectWriteDao.createProject(new Project(name= "Eirik3"))
  }

  def main(args: Array[String]) {
    val server = new Server
    val context = new WebAppContext("src/main/webapp", "/web")
    val connector = new SocketConnector

    connector.setMaxIdleTime(1000 * 60 * 60)
    connector.setPort(8081)

    context.setServer(server)

    server.setConnectors(Array(connector))
    server.setHandler(context)

    try {
      server.start()
      initHZ()
      server.join()
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }
}