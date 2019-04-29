package com.poryadin.moneytransfer.bootstrap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.poryadin.moneytransfer.NetUtils;
import com.poryadin.moneytransfer.model.AccountResult;
import com.poryadin.moneytransfer.model.Transfer;
import com.poryadin.moneytransfer.model.TransferResult;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MoneyTransferTest {
    private static int PORT;
    private static MoneyTransfer INSTANCE;
    private static Gson GSON = new GsonBuilder().create();

    @BeforeClass
    public static void startServer() throws Exception {
        new Thread(() -> {
            INSTANCE = new MoneyTransfer();
            PORT = NetUtils.nextFreePort(1000, 10000);
            try {
                INSTANCE.start(PORT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            while (NetUtils.isLocalPortFree(PORT)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            latch.countDown();
        }).start();

        if(!latch.await(1, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Server is not available");
        }
    }

    @AfterClass
    public static void stopServer() {
        if(INSTANCE != null) {
            INSTANCE.stop();
        }
    }

    class AccountRequest {
        private final BigDecimal balance;

        AccountRequest(BigDecimal balance) {
            this.balance = balance;
        }

        String toJson() {
            return GSON.toJson(this);
        }
    }

    @Test
    public void flow() throws Exception {
        AccountResult accountResult = createAccount();
        Assert.assertEquals(new AccountResult("OK", "", 1, BigDecimal.TEN), accountResult);
        System.out.println("Created account: " + GSON.toJson(accountResult));

        AccountResult accountResult2 = createAccount();
        System.out.println("Created account: " + GSON.toJson(accountResult2));
        Assert.assertEquals(new AccountResult("OK", "", 2, BigDecimal.TEN), accountResult2);

        TransferResult transferResult = transfer();
        System.out.println("Transfer: " + GSON.toJson(transferResult));

        AccountResult fromAccountRequest = getAccount(1L);
        System.out.println("From account: " + GSON.toJson(fromAccountRequest));

        AccountResult toAccountRequest = getAccount(2L);
        System.out.println("To account: " + GSON.toJson(toAccountRequest));
    }

    public AccountResult createAccount() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) getUrl("account").openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        IOUtils.write(new AccountRequest(BigDecimal.TEN).toJson(), connection.getOutputStream(), StandardCharsets.UTF_8);
        Assert.assertEquals(HttpResponseStatus.OK.code(), connection.getResponseCode());

        AccountResult accountResult = GSON.fromJson(getBody(connection), AccountResult.class);
        Assert.assertNotNull(accountResult);
        return accountResult;
    }

    public AccountResult getAccount(long id) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) getUrl("account?id=" + id).openConnection();
        connection.setRequestMethod("GET");
        Assert.assertEquals(HttpResponseStatus.OK.code(), connection.getResponseCode());

        AccountResult accountResult = GSON.fromJson(getBody(connection), AccountResult.class);
        Assert.assertNotNull(accountResult);

        Assert.assertEquals("OK", accountResult.getStatus());
        return accountResult;
    }

    public TransferResult transfer() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) getUrl("transfer").openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        IOUtils.write(GSON.toJson(new Transfer(1, 2, new BigDecimal("1.25"))),
                connection.getOutputStream(), StandardCharsets.UTF_8);
        Assert.assertEquals(HttpResponseStatus.OK.code(), connection.getResponseCode());

        TransferResult transferResult = GSON.fromJson(getBody(connection), TransferResult.class);
        Assert.assertNotNull(transferResult);

        Assert.assertEquals(new TransferResult("OK", "", new BigDecimal("1.25"),
                1L, 2L), transferResult);

        return transferResult;
    }

    private String getBody(HttpURLConnection connection) throws IOException {
        String body;
        try (InputStream inputStream = connection.getInputStream()) {
            body = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
        return body;
    }

    private URL getUrl(String path) throws MalformedURLException {
        return new URL("http://localhost:" + PORT + "/" + path);
    }
}