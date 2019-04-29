package com.poryadin.moneytransfer.model;

import java.math.BigDecimal;
import java.util.Objects;

public class AccountResult extends Result {
    private final long id;
    private final BigDecimal balance;

    public AccountResult(String status, String msg, long id, BigDecimal balance) {
        super(status, msg);
        this.id = id;
        this.balance = balance;
    }

    public AccountResult(Account account) {
        super("OK", "");
        id = account.getId();
        balance = account.getBalance();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountResult)) return false;
        if (!super.equals(o)) return false;
        AccountResult that = (AccountResult) o;
        return id == that.id &&
                Objects.equals(balance, that.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, balance);
    }
}
