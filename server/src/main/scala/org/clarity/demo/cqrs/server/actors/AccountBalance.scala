package org.clarity.demo.cqrs.server.actors

/**
 * @author Eirik Wang - eirik.wang@bekk.no
 * @since 2.2 TODO: Check version
 */
case class AccountBalance (accountId: Long, balance: Double){
    def change(change: Double): AccountBalance = {
      AccountBalance(accountId, balance + change)
    }
  }
