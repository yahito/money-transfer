package com.poryadin.moneytransfer.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class JsonHandler<T> extends RequestHandler {
    private static final Gson GSON = new GsonBuilder().create();
    private final Class<T> _class;

    public JsonHandler(Class<T> _class) {
        this._class = _class;
    }

    public T convert(String body) {
        T t = GSON.fromJson(body, _class);
        if(t == null) {
            throw new IllegalArgumentException("Wrong request format");
        }
        return t;
    }
}
