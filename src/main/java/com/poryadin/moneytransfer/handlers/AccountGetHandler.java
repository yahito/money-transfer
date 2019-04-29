package com.poryadin.moneytransfer.handlers;

import com.poryadin.moneytransfer.model.AccountResult;
import com.poryadin.moneytransfer.model.Result;
import com.poryadin.moneytransfer.services.AccountService;

import java.util.List;
import java.util.Map;

public class AccountGetHandler extends RequestHandler {

    private final AccountService accountService;

    public AccountGetHandler(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public Result handle(Map<String, List<String>> params, String body) {
        try {
            String idParam = getParam(params, "id");
            if(idParam == null) {
                return new Result("ERROR", "id parameter should be specified");
            }
            long id;
            try {
                id = Long.parseLong(idParam);
            } catch (NumberFormatException ex) {
                return new Result("ERROR", "id parameter is not a number");
            }

            return new AccountResult(accountService.get(id));
        } catch (Exception e) {
            return new Result("ERROR", e.getMessage());
        }
    }

    private String getParam(Map<String, List<String>> params, String id) {
        List<String> list = params.get(id);
        if(list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
}
