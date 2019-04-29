package com.poryadin.moneytransfer.model;

import java.math.BigDecimal;
import java.util.Objects;

public class TransferResult extends Result {
    private final BigDecimal amount;
    private final long accFrom;
    private final long accTo;

    public TransferResult(String status, String msg, BigDecimal amount, long accFrom, long accTo) {
        super(status, msg);
        this.amount = amount;
        this.accFrom = accFrom;
        this.accTo = accTo;
    }

    public TransferResult(Transfer transfer) {
        super(transfer.getStatus(), transfer.getMsg());
        this.amount = transfer.getAmount();
        this.accFrom = transfer.getFrom();
        this.accTo = transfer.getTo();
    }

    public TransferResult(String status, String message, Transfer transfer) {
        super(status, message);
        this.amount = transfer.getAmount();
        this.accFrom = transfer.getFrom();
        this.accTo = transfer.getTo();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public long getAccFrom() {
        return accFrom;
    }

    public long getAccTo() {
        return accTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransferResult)) return false;
        if (!super.equals(o)) return false;
        TransferResult that = (TransferResult) o;
        return accFrom == that.accFrom &&
                accTo == that.accTo &&
                Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), amount, accFrom, accTo);
    }
}
