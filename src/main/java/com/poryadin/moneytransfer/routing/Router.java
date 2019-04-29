package com.poryadin.moneytransfer.routing;

import com.poryadin.moneytransfer.handlers.RequestHandler;

import java.util.HashMap;

public class Router {
    private final HashMap<Route, RequestHandler> routes = new HashMap<>();

    public void addRoute(Route route, RequestHandler handler) {
        routes.put(route, handler);
    }

    public RequestHandler getHandler(Route route) {
        return routes.get(route);
    }
}
