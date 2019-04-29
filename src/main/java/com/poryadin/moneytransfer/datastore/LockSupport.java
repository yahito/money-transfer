package com.poryadin.moneytransfer.datastore;

import com.poryadin.moneytransfer.model.Entity;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class LockSupport {

    private class LockKey {
        final long id;
        final Class<? extends Entity> _class;

        LockKey(long id, Class<? extends Entity> aClass) {
            this.id = id;
            _class = aClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LockKey lockKey = (LockKey) o;
            return id == lockKey.id &&
                    Objects.equals(_class, lockKey._class);
        }

        @Override
        public int hashCode() {

            return Objects.hash(id, _class);
        }
    }

    private final ConcurrentHashMap<LockKey, AtomicLong> locks = new ConcurrentHashMap<>();

    public boolean lock(Entity entity, long ownerId, long timeout, TimeUnit timeUnit) {
        long start = System.nanoTime();
        long currTime = start;

        AtomicLong lock = locks.computeIfAbsent(getKey(entity), k -> new AtomicLong(-1));
        boolean locked;

        long timeoutNanos = timeUnit.toNanos(timeout);
        while (!(locked = lock.compareAndSet(-1, ownerId)) && (currTime - start) < timeoutNanos) {
            currTime = System.nanoTime();
        }

        return locked;
    }

    public void unlock(Entity entity, long ownerId) {
        AtomicLong lock = locks.get(getKey(entity));
        if(lock == null) {
            throw new IllegalStateException("Lock is not found for: " + entity);
        }

        lock.compareAndSet(ownerId, -1);
    }

    private LockKey getKey(Entity entity) {
        return new LockKey(entity.getId(), entity.getClass());
    }
}
