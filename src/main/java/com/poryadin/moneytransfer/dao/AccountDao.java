package com.poryadin.moneytransfer.dao;

import com.poryadin.moneytransfer.datastore.DataStore;
import com.poryadin.moneytransfer.datastore.IdGenerator;
import com.poryadin.moneytransfer.datastore.LockSupport;
import com.poryadin.moneytransfer.model.Account;

import java.util.concurrent.TimeUnit;

public class AccountDao {
    private final DataStore dataStore;
    private final IdGenerator idGenerator;
    private final LockSupport lockSupport;

    public AccountDao(DataStore dataStore, IdGenerator idGenerator, LockSupport lockSupport) {
        this.dataStore = dataStore;
        this.idGenerator = idGenerator;
        this.lockSupport = lockSupport;
    }

    public Account create(Account account) {
        return dataStore.add(account.withId(idGenerator.generateId(Account.class)));
    }

    public Account update(Account newAccountFrom) {
        return dataStore.update(newAccountFrom);
    }

    public Account get(long id) {
        Account account = dataStore.getEntity(Account.class, id);
        if(account == null) {
            throw new IllegalArgumentException("Account " + id + " is not found");
        }
        return account;
    }

    public boolean lock(Account account, long ownerId, int timeout, TimeUnit timeUnit) {
        return lockSupport.lock(account, ownerId, timeout, timeUnit);
    }

    public void unlock(Account account, long ownerId) {
        lockSupport.unlock(account, ownerId);
    }
}
