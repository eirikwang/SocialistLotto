package org.clarity.demo.cqrs.server.db

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.server.bio.SocketConnector
import com.hazelcast.core.{IMap, HazelcastInstance, Hazelcast}
import com.hazelcast.config.ClasspathXmlConfig
import org.clarity.demo.cqrs.domain.Project
import org.clarity.demo.cqrs.server.dao.{ProjectWriteDao, ProjectWriteDaoImpl}
import com.hazelcast.client.{ClientConfig, HazelcastClient}
import org.clarity.demo.cqrs.server.actors.persistence.AccountStorage.AccountDetail

object JettyServer {
  def projectWriteDao:ProjectWriteDao = new ProjectWriteDaoImpl
  def initHZ() = {
    val newHazelcastInstance: HazelcastInstance = Hazelcast.newHazelcastInstance(new ClasspathXmlConfig("hazelcast.xml"))
    def hazelcast = HazelcastClient.newHazelcastClient(new ClientConfig() {
      addAddress("localhost")
    })
    val map: IMap[Long, AccountDetail] = hazelcast.getMap("accountDetail")
    for(i <- 1 to 100) {
      map.put(i, AccountDetail(i, "Account" + i))
    }
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