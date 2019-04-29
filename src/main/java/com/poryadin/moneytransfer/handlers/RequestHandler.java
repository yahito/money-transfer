package com.poryadin.moneytransfer.handlers;

import com.poryadin.moneytransfer.model.Result;

import java.util.List;
import java.util.Map;

public abstract class RequestHandler {
    public abstract Result handle(Map<String, List<String>> params, String body);
}
