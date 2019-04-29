package com.poryadin.moneytransfer.model;

public abstract class Entity<T> {
    public abstract long getId();

    @Override
    public String toString() {
        return "Entity{id: +" + getId() +"}";
    }
}
