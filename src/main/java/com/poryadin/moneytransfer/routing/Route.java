package com.poryadin.moneytransfer.routing;

import io.netty.handler.codec.http.HttpMethod;

import java.util.Objects;

public class Route {
    private final HttpMethod method;
    private final String path;

    public Route(HttpMethod method, String path) {
        this.method = method;
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return Objects.equals(method, route.method) &&
                Objects.equals(path, route.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path);
    }
}
