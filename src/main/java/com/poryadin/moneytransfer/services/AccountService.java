package com.poryadin.moneytransfer.services;

import com.poryadin.moneytransfer.dao.AccountDao;
import com.poryadin.moneytransfer.model.Account;

public class AccountService {
    private final AccountDao accountDao;

    public AccountService(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public Account create(Account account) {
        return accountDao.create(account);
    }

    public Account get(long id) {
        return accountDao.get(id);
    }

}
