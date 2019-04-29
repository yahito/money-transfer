package com.poryadin.moneytransfer.bootstrap;

import com.poryadin.moneytransfer.dao.AccountDao;
import com.poryadin.moneytransfer.dao.TransferDao;
import com.poryadin.moneytransfer.datastore.*;
import com.poryadin.moneytransfer.handlers.AccountGetHandler;
import com.poryadin.moneytransfer.handlers.AccountCreateHandler;
import com.poryadin.moneytransfer.handlers.TransferHandler;
import io.netty.handler.codec.http.HttpMethod;
import com.poryadin.moneytransfer.routing.Route;
import com.poryadin.moneytransfer.routing.Router;
import com.poryadin.moneytransfer.services.AccountService;
import com.poryadin.moneytransfer.services.TransferService;

public class MoneyTransfer {

    private Bootstrap bootstrap;

    public static void main(String[] args) throws Exception {
        if(args.length < 1) {
            throw new IllegalArgumentException("Usage: java -jar money-transfer.jar port");
        }


        Integer port;

        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Port is incorrect");
        }

        MoneyTransfer moneyTransfer = new MoneyTransfer();
        moneyTransfer.start(port);

    }

    void start(int port) throws Exception {
        DataStore dataStore = new DataStore();
        IdGenerator idGenerator = new IdGenerator();
        LockSupport lockSupport = new LockSupport();

        AccountDao accountDao = new AccountDao(dataStore, idGenerator, lockSupport);
        TransferDao transferDao = new TransferDao(dataStore, idGenerator);

        AccountService accountService = new AccountService(accountDao);
        TransferService transferService = new TransferService(accountDao, transferDao);

        bootstrap = new Bootstrap();
        bootstrap.start(initRouter(accountService, transferService), port);
    }

    private Router initRouter(AccountService accountService, TransferService transferService) {
        Router router = new Router();
        router.addRoute(new Route(HttpMethod.GET, "/account"), new AccountGetHandler(accountService));
        router.addRoute(new Route(HttpMethod.POST, "/account"), new AccountCreateHandler(accountService));
        router.addRoute(new Route(HttpMethod.POST, "/transfer"), new TransferHandler(transferService));
        return router;
    }

    public void stop() {
        bootstrap.stop();
    }
}