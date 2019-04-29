package com.poryadin.moneytransfer.datastore;

import com.poryadin.moneytransfer.model.Entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private final ConcurrentHashMap<Class<? extends Entity>, ConcurrentHashMap<Long, Entity>> datastore = new ConcurrentHashMap<>();

    public <T extends Entity> T add(T entity) {
        if(entity == null) {
            throw new NullPointerException("entity is null");
        }

        Map<Long, Entity> entities = datastore.computeIfAbsent(entity.getClass(), c -> new ConcurrentHashMap<>());
        if(entities.putIfAbsent(entity.getId(), entity) != null) {
            throw new IllegalStateException("Already exists");
        }
        return entity;
    }

    public <T extends Entity> T update(T entity) {
        if(entity == null) {
            throw new NullPointerException("entity is null");
        }

        Map<Long, Entity> entities = datastore.get(entity.getClass());

        if(entities == null) {
            throw new IllegalStateException("");
        }

        entities.put(entity.getId(), entity);
        return entity;
    }

    public <T extends Entity> T getEntity(Class<T> _class, long id) {
        Map<Long, Entity> entities = datastore.get(_class);
        if(entities == null) {
            return null;
        }

        return (T) entities.get(id);
    }

}
