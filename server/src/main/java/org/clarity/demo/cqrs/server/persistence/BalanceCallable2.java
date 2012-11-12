package org.clarity.demo.cqrs.server.persistence;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.clarity.demo.cqrs.server.actors.AccountBalance;
import org.clarity.demo.cqrs.server.actors.AccountChange;

/**
 * @author Eirik Wang - eirik.wang@bekk.no
 * @since 2.2x
 */
public class BalanceCallable2 implements Callable<AccountBalance>, Serializable {
    private String config;
    private AccountChange accountChange;

    public BalanceCallable2(String config, AccountChange accountChange) {
        this.config = config;
        this.accountChange = accountChange;
    }

    public AccountBalance call() {
        HazelcastInstance client = Hazelcast.getHazelcastInstanceByName(config);
        IMap<Long, AccountBalance> map = client.getMap("accountBalance");
        System.out.println("Client: " + client);
        AccountBalance balance = null;
        try {
            balance = map.tryLockAndGet(accountChange.account(), 5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        System.out.println("Balance: " + balance);
        AccountBalance newBalance = balance.change(accountChange.balanceChange());
        System.out.println("NewBalance: " + balance);
        map.putAndUnlock(accountChange.account(), newBalance);
        return newBalance;
    }
}
