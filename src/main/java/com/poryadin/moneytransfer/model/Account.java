package com.poryadin.moneytransfer.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Account extends Entity<Account> {
    private final long id;
    private final BigDecimal balance;

    public Account(BigDecimal balance) {
        this(-1, balance);
    }

    public Account(long id, BigDecimal balance) {
        this.id = id;
        this.balance = balance;
    }

    @Override
    public long getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Account withId(long id) {
        return new Account(id, balance);
    }

    public Account debit(BigDecimal amount) {
        if(this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Not enough money");
        }
        return new Account(id, this.balance.subtract(amount));
    }

    public Account credit(BigDecimal amount) {
        return new Account(id, this.balance.add(amount));
    }

    public Account validate() {
        if(balance == null) {
            throw new IllegalStateException("balance is not initialized");
        }

        if(balance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("balance is zero or negative");
        }

        if(balance.scale() > 2) {
            throw new IllegalStateException("There should be no more that 2 digits after decimal point");
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return id == account.id &&
                Objects.equals(balance, account.balance);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, balance);
    }
}
