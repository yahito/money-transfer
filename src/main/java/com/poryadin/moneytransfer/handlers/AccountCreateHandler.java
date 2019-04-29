package com.poryadin.moneytransfer.handlers;

import com.poryadin.moneytransfer.model.Account;
import com.poryadin.moneytransfer.model.AccountResult;
import com.poryadin.moneytransfer.model.Result;
import com.poryadin.moneytransfer.services.AccountService;

import java.util.List;
import java.util.Map;

public class AccountCreateHandler extends JsonHandler<Account> {

    private final AccountService service;

    public AccountCreateHandler(AccountService service) {
        super(Account.class);
        this.service = service;
    }

    @Override
    public Result handle(Map<String, List<String>> params, String body) {
        try {
            return new AccountResult(service.create(convert(body).validate()));
        } catch (Exception e) {
            return new Result("ERROR", e.getMessage());
        }
    }
}
