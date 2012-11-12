package org.clarity.demo.cqrs.server.exceptions

/**
 * @author Eirik Wang - eirik.wang@bekk.no
 * @since 2.2 TODO: Check version
 */
case class AccountNotAvailableException(message: String) extends RuntimeException
