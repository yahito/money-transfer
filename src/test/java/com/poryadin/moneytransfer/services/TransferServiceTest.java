package com.poryadin.moneytransfer.services;

import com.poryadin.moneytransfer.dao.AccountDao;
import com.poryadin.moneytransfer.datastore.IdGenerator;
import com.poryadin.moneytransfer.dao.TransferDao;
import com.poryadin.moneytransfer.model.Account;
import com.poryadin.moneytransfer.model.Result;
import com.poryadin.moneytransfer.model.Transfer;
import com.poryadin.moneytransfer.model.TransferResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.math.BigDecimal.ONE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransferServiceTest {
    @Mock
    private AccountDao accountDao;

    @Mock
    private TransferDao transferDao;

    private IdGenerator idGenerator = new IdGenerator();

    private TransferService transferService;

    @Before
    public void init() {
        transferService = new TransferService(accountDao, transferDao);
        when(transferDao.createTransfer(any(Transfer.class))).then(invocationOnMock -> {
            Transfer transfer = (Transfer) invocationOnMock.getArguments()[0];
            return transfer.withId(idGenerator.generateId(Transfer.class));
        });

        when(transferDao.updateState(any(Transfer.class))).then(invocationOnMock -> invocationOnMock.getArguments()[0]);

        when(accountDao.update(any(Account.class))).then(invocationOnMock -> invocationOnMock.getArguments()[0]);
    }

    @Test
    public void transferSuccess() {
        Account accFrom = new Account(1L, BigDecimal.TEN);
        when(accountDao.lock(eq(accFrom), eq(Thread.currentThread().getId()), anyInt(),
                any(TimeUnit.class))).thenReturn(true);
        when(accountDao.get(eq(1L))).thenReturn(accFrom);
        Account accTo = new Account(2L, BigDecimal.TEN);
        when(accountDao.lock(eq(accTo), eq(Thread.currentThread().getId()), anyInt(),
                any(TimeUnit.class))).thenReturn(true);
        when(accountDao.get(eq(2L))).thenReturn(accTo);

        Result transferResult = transferService.transfer(new Transfer(1L, 2L, ONE));

        Assert.assertEquals(new TransferResult("OK", "", ONE,
                1L, 2L), transferResult);
        verify(accountDao, times(2)).unlock(any(Account.class),
                eq(Thread.currentThread().getId()));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        verify(accountDao, times(2)).update(captor.capture());

        List<Account> accounts = captor.getAllValues();

        Optional<Account> from = accounts.stream().filter(a -> a.getId() == 1L).findFirst();
        Assert.assertTrue(from.isPresent());
        Assert.assertEquals(new Account(1L, new BigDecimal("9")), from.get());

        Optional<Account> to = accounts.stream().filter(a -> a.getId() == 2L).findFirst();
        Assert.assertTrue(to.isPresent());
        Assert.assertEquals(new Account(2L, new BigDecimal("11")),
                to.get());
    }


    @Test
    public void transferErrorFromAccountGetError() {
        when(accountDao.get(eq(1L))).thenThrow(new RuntimeException("Test"));
        Account accTo = new Account(2L, BigDecimal.TEN);
        when(accountDao.lock(eq(accTo), eq(Thread.currentThread().getId()), anyInt(),
                eq(TimeUnit.MILLISECONDS))).thenReturn(true);
        when(accountDao.get(eq(2L))).thenReturn(accTo);

        TransferResult transferResult = transferService.transfer(new Transfer(1L, 2L, ONE));

        Assert.assertEquals(new TransferResult("ERROR", "Test", ONE, 1L, 2L), transferResult);

        ArgumentCaptor<Transfer> captor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferDao, times(1)).updateState(captor.capture());
        Assert.assertEquals(new Transfer(1L, 1L, 2L, ONE, "ERROR",
                "Test"), captor.getValue());
    }

    @Test
    public void transferErrorToAccountGetError() {
        when(accountDao.get(eq(2L))).thenThrow(new RuntimeException("Test"));
        Account accFrom = new Account(1L, BigDecimal.TEN);
        when(accountDao.lock(eq(accFrom), eq(Thread.currentThread().getId()), anyInt(),
                eq(TimeUnit.MILLISECONDS))).thenReturn(true);
        when(accountDao.get(eq(1L))).thenReturn(accFrom);

        TransferResult transferResult = transferService.transfer(new Transfer(1L, 2L, ONE));

        Assert.assertEquals(new TransferResult("ERROR", "Test", ONE, 1L, 2L), transferResult);

        ArgumentCaptor<Transfer> captor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferDao, times(1)).updateState(captor.capture());
        Assert.assertEquals(new Transfer(1L, 1L, 2L, ONE, "ERROR",
                "Test"), captor.getValue());
    }


    @Test
    public void transferErrorNotEnoughMoney() {
        Account accFrom = new Account(1L, BigDecimal.TEN);
        when(accountDao.lock(eq(accFrom), eq(Thread.currentThread().getId()), anyInt(),
                any(TimeUnit.class))).thenReturn(true);
        when(accountDao.get(eq(1L))).thenReturn(accFrom);
        Account accTo = new Account(2L, BigDecimal.TEN);
        when(accountDao.lock(eq(accTo), eq(Thread.currentThread().getId()), anyInt(),
                any(TimeUnit.class))).thenReturn(true);
        when(accountDao.get(eq(2L))).thenReturn(accTo);
        Result transferResult = transferService.transfer(new Transfer(1L, 2L, BigDecimal.valueOf(100)));

        Assert.assertEquals(new TransferResult("ERROR", "Not enough money", BigDecimal.valueOf(100), 1L, 2L), transferResult);

        verify(accountDao, times(2)).unlock(any(Account.class), eq(Thread.currentThread().getId()));

        ArgumentCaptor<Transfer> captor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferDao, times(1)).updateState(captor.capture());
        Assert.assertEquals(new Transfer(1L, 1L, 2L, BigDecimal.valueOf(100), "ERROR",
                "Not enough money"), captor.getValue());
    }

    @Test
    public void transferErrorFromAccountLockBoth() {
        Account accFrom = new Account(1L, BigDecimal.TEN);
        when(accountDao.lock(eq(accFrom), eq(Thread.currentThread().getId()), anyInt(),
                eq(TimeUnit.MILLISECONDS))).thenReturn(false);
        when(accountDao.get(eq(1L))).thenReturn(accFrom);
        Account accTo = new Account(2L, BigDecimal.TEN);
        when(accountDao.lock(eq(accTo), eq(Thread.currentThread().getId()), anyInt(),
                eq(TimeUnit.MILLISECONDS))).thenReturn(false);
        when(accountDao.get(eq(2L))).thenReturn(accTo);

        Result transferResult = transferService.transfer(new Transfer(1L, 2L, ONE));

        Assert.assertEquals(new TransferResult("ERROR", "Unable to lock account", ONE, 1L, 2L), transferResult);

        ArgumentCaptor<Transfer> captor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferDao, times(1)).updateState(captor.capture());
        Assert.assertEquals(new Transfer(1L, 1L, 2L, ONE, "ERROR",
                "Unable to lock account"), captor.getValue());
    }

    @Test
    public void transferErrorFromAccountLockFail() {
        Account accFrom = new Account(1L, BigDecimal.TEN);
        when(accountDao.lock(eq(accFrom), eq(Thread.currentThread().getId()), anyInt(),
                eq(TimeUnit.MILLISECONDS))).thenReturn(false);
        when(accountDao.get(eq(1L))).thenReturn(accFrom);
        Account accTo = new Account(2L, BigDecimal.TEN);
        when(accountDao.lock(eq(accTo), eq(Thread.currentThread().getId()), anyInt(),
                eq(TimeUnit.MILLISECONDS))).thenReturn(true);
        when(accountDao.get(eq(2L))).thenReturn(accTo);

        Result transferResult = transferService.transfer(new Transfer(1L, 2L, ONE));

        Assert.assertEquals(new TransferResult("ERROR", "Unable to lock account", ONE, 1L, 2L), transferResult);

        ArgumentCaptor<Transfer> captor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferDao, times(1)).updateState(captor.capture());
        Assert.assertEquals(new Transfer(1L, 1L, 2L, ONE, "ERROR",
                "Unable to lock account"), captor.getValue());
    }

    @Test
    public void transferErrorToAccountLockFail() {
        Account accFrom = new Account(1L, BigDecimal.TEN);
        when(accountDao.lock(eq(accFrom), eq(Thread.currentThread().getId()), anyInt(),
                eq(TimeUnit.MILLISECONDS))).thenReturn(true);
        when(accountDao.get(eq(1L))).thenReturn(accFrom);
        Account accTo = new Account(2L, BigDecimal.TEN);
        when(accountDao.lock(eq(accTo), eq(Thread.currentThread().getId()), anyInt(),
                eq(TimeUnit.MILLISECONDS))).thenReturn(false);
        when(accountDao.get(eq(2L))).thenReturn(accTo);

        Result transferResult = transferService.transfer(new Transfer(1L, 2L, ONE));

        Assert.assertEquals(new TransferResult("ERROR", "Unable to lock account", ONE, 1L, 2L), transferResult);

        ArgumentCaptor<Transfer> captor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferDao, times(1)).updateState(captor.capture());
        Assert.assertEquals(new Transfer(1L, 1L, 2L, ONE, "ERROR",
                        "Unable to lock account"), captor.getValue());
    }
}