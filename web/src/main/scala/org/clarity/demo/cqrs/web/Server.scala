package org.clarity.demo.cqrs.web

import org.eclipse.jetty.server.{Handler, Server}
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.server.bio.SocketConnector
import org.eclipse.jetty.servlets.ProxyServlet
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}
import org.eclipse.jetty.server.handler.ContextHandlerCollection

object JettyServer {

  def main(args: Array[String]) {
    val server = new Server
    val context = new WebAppContext("src/main/webapp", "/web")
    val connector = new SocketConnector
    val proxy = new ProxyServlet.Transparent("/api", "localhost", 9000)
    var holder = new ServletHolder("apiProxy", proxy)
    var proxyContext = new ServletContextHandler()
    proxyContext.addServlet(holder, "/api/*")
    connector.setMaxIdleTime(1000 * 60 * 60)
    connector.setPort(8080)
    context.setServer(server)
    proxyContext.setServer(server)
    val contexts = new ContextHandlerCollection()
    contexts.setHandlers(Array(context, proxyContext))
    server.setConnectors(Array(connector))
    server.setHandler(contexts)


    try {
      server.start()
      server.join()
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }
}