package com.poryadin.moneytransfer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ThreadLocalRandom;

public class NetUtils {

    public static int nextFreePort(int from, int to) {
        int port = getPort(from, to);
        while (true) {
            if (isLocalPortFree(port)) {
                return port;
            } else {
                port = getPort(from, to);
            }
        }
    }

    private static int getPort(int from, int to) {
        int port;
        port = ThreadLocalRandom.current().nextInt(from, to);
        return port;
    }

    public static boolean isLocalPortFree(int port) {
        try {
            new ServerSocket(port).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
