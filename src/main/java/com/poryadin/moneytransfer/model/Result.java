package com.poryadin.moneytransfer.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Objects;

public class Result {
    private static final Gson GSON = new GsonBuilder().create();

    protected final String status;
    protected final String msg;

    public Result(String status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public String getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return Objects.equals(status, result.status) &&
                Objects.equals(msg, result.msg);
    }

    @Override
    public int hashCode() {

        return Objects.hash(status, msg);
    }
}
