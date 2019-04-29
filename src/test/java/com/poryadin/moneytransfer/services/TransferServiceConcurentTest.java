package com.poryadin.moneytransfer.services;

import com.poryadin.moneytransfer.dao.AccountDao;
import com.poryadin.moneytransfer.dao.TransferDao;
import com.poryadin.moneytransfer.datastore.*;
import com.poryadin.moneytransfer.model.Account;
import com.poryadin.moneytransfer.model.Result;
import com.poryadin.moneytransfer.model.Transfer;
import com.poryadin.moneytransfer.model.TransferResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TransferServiceConcurentTest {
    private AccountDao accountDao;

    private TransferService transferService;

    @Before
    public void init() {
        DataStore dataStore = new DataStore();
        IdGenerator idGenerator = new IdGenerator();
        LockSupport lockSupport = new LockSupport();
        accountDao = new AccountDao(dataStore, idGenerator, lockSupport);
        TransferDao transferDao = new TransferDao(dataStore, idGenerator);

        transferService = new TransferService(accountDao, transferDao);
    }

    @Test
    public void testConcurrentTransfersOnSameAccountPairs() throws ExecutionException, InterruptedException {
        accountDao.create(new Account(BigDecimal.valueOf(100110.1)));
        accountDao.create(new Account(BigDecimal.valueOf(0)));
        BigDecimal amount = BigDecimal.valueOf(100.11);

        ExecutorService executorService = Executors.newFixedThreadPool(100);

        List<Future<Result>> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        for(int i =0; i < 1000; i++) {
            Future<Result> result = executorService.submit(() ->
            {
                latch.await();
                return transferService.transfer(new Transfer(1L, 2L, amount));
            });
            results.add(result);
        }

        latch.countDown();

        BigDecimal lockErrorsCount = BigDecimal.ZERO;
        for (Future<Result> future : results) {
            Result result = future.get();
            if(result.getStatus().equals("ERROR")) {
                Assert.assertEquals(result.getMsg(), "Unable to lock account");
                lockErrorsCount = lockErrorsCount.add(BigDecimal.ONE);
            }
        }

        System.out.println(lockErrorsCount);
        Assert.assertEquals(new BigDecimal("0.10").add(amount.multiply(lockErrorsCount)), accountDao.get(1L).getBalance());
        Assert.assertEquals(new BigDecimal("100110.00").subtract(amount.multiply(lockErrorsCount)), accountDao.get(2L).getBalance());
    }

    @Test
    public void testConcurrentTransferBackAndForth() throws ExecutionException, InterruptedException {
        BigDecimal initialAmount = new BigDecimal("10000.00");
        accountDao.create(new Account(initialAmount));
        accountDao.create(new Account(initialAmount));

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<Future<TransferResult>> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        BigDecimal amount = BigDecimal.valueOf(100.11);

        for(int i =0; i < 100; i++) {
            int caseId = i;
            Future<TransferResult> result = executorService.submit(() ->
            {
                latch.await();
                if(caseId % 2 == 0) {
                    return transferService.transfer(new Transfer(1L, 2L, amount));
                } else {
                    return transferService.transfer(new Transfer(2L, 1L, amount));
                }
            });
            results.add(result);
        }

        latch.countDown();


        BigDecimal fromErrorCounter = BigDecimal.ZERO;
        BigDecimal toErrorCounter = BigDecimal.ZERO;

        for (Future<TransferResult> future : results) {
            TransferResult result = future.get();
            if (result.getStatus().equals("ERROR")) {
                Assert.assertEquals(result.getMsg(), "Unable to lock account");
                if(result.getAccFrom() == 1L) {
                    fromErrorCounter = fromErrorCounter.add(BigDecimal.ONE);
                } else {
                    toErrorCounter = toErrorCounter.add(BigDecimal.ONE);
                }

            }
        }

        Assert.assertTrue(fromErrorCounter.longValue() < 10);
        Assert.assertTrue(toErrorCounter.longValue() < 10);

        BigDecimal diff = toErrorCounter.subtract(fromErrorCounter).multiply(amount);
        Assert.assertEquals(initialAmount.subtract(diff),
                accountDao.get(1L).getBalance());
        Assert.assertEquals(initialAmount.add(diff),
                accountDao.get(2L).getBalance());
    }

}

