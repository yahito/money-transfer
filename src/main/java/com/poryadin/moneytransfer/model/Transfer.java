package com.poryadin.moneytransfer.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Transfer extends Entity<Transfer> {
    private final long id;
    private final long from;
    private final long to;
    private final BigDecimal amount;
    private final String status;
    private final String msg;

    public Transfer(long from, long to, BigDecimal amount) {
        this(-1, from, to, amount);
    }

    public Transfer(long id, long from, long to, BigDecimal amount) {
        this(id, from, to, amount, "INIT", "");
    }

    public Transfer(long id, long from, long to, BigDecimal amount, String status, String msg) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.status = status;
        this.msg = msg;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    public String getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public Transfer withId(long id) {
        return new Transfer(id, from, to, amount);
    }

    public Transfer complete(String status, String msg) {
        return new Transfer(id, from, to, amount, status, msg);
    }

    public Transfer validate() {
        if(from <= 0) {
            throw new IllegalStateException("from account is not initialized");
        }
        if(to <= 0) {
            throw new IllegalStateException("to account is not initialized");
        }

        if(amount == null) {
            throw new IllegalStateException("amount account is not initialized");
        }

        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("amount is negative or zero");
        }

        if(amount.scale() > 2) {
            throw new IllegalStateException("There should be no more that 2 digits after decimal point");
        }

        if(from == to) {
            throw new IllegalArgumentException("Destination account is equal to target");
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transfer)) return false;
        Transfer transfer = (Transfer) o;
        return id == transfer.id &&
                from == transfer.from &&
                to == transfer.to &&
                Objects.equals(amount, transfer.amount) &&
                Objects.equals(status, transfer.status) &&
                Objects.equals(msg, transfer.msg);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, from, to, amount, status, msg);
    }
}
