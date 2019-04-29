package com.poryadin.moneytransfer.datastore;

import com.poryadin.moneytransfer.model.Entity;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {
    private final ConcurrentHashMap<Class<? extends Entity>, AtomicLong> generators = new ConcurrentHashMap<>();

    public <T extends Entity> long generateId(Class<T> _class) {
        return generators.computeIfAbsent(_class, (k) -> new AtomicLong(0L)).incrementAndGet();
    }

}
