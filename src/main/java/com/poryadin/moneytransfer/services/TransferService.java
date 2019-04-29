package com.poryadin.moneytransfer.services;

import com.poryadin.moneytransfer.dao.AccountDao;
import com.poryadin.moneytransfer.dao.TransferDao;
import com.poryadin.moneytransfer.model.Account;
import com.poryadin.moneytransfer.model.Result;
import com.poryadin.moneytransfer.model.Transfer;
import com.poryadin.moneytransfer.model.TransferResult;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public class TransferService {
    private final AccountDao accountDao;
    private final TransferDao transferDao;

    public TransferService(AccountDao accountDao, TransferDao transferDao) {
        this.accountDao = accountDao;
        this.transferDao = transferDao;
    }

    public TransferResult transfer(Transfer transfer) {
        long threadId = Thread.currentThread().getId();

        try {
            transfer = transferDao.createTransfer(transfer);

            Account accFrom = accountDao.get(transfer.getFrom());
            Account accTo = accountDao.get(transfer.getTo());

            try {
                boolean lockFrom;
                boolean lockTo;

                if(transfer.getFrom() > transfer.getTo()) {
                    lockTo = lock(accTo, threadId);
                    lockFrom = lock(accFrom, threadId);
                } else {
                    lockFrom = lock(accFrom, threadId);
                    lockTo = lock(accTo, threadId);
                }

                if (lockFrom && lockTo) {
                    BigDecimal amount = transfer.getAmount();
                    accFrom = accountDao.update(accountDao.get(transfer.getFrom()).debit(amount));
                    accTo = accountDao.update(accountDao.get(transfer.getTo()).credit(amount));
                    transfer = transferDao.updateState(transfer.complete("OK", ""));
                } else {
                    throw new IllegalStateException("Unable to lock account");
                }
            } finally {
                accountDao.unlock(accFrom, threadId);
                accountDao.unlock(accTo, threadId);
            }

            return new TransferResult(transfer);
        } catch (Exception ex) {
            //TODO log
            transferDao.updateState(transfer.complete("ERROR", ex.getMessage()));
            return new TransferResult("ERROR", ex.getMessage(), transfer);
        }
    }

    private boolean lock(Account account, long ownerId) {
        return accountDao.lock(account, ownerId, 100, TimeUnit.MILLISECONDS);
    }
}
