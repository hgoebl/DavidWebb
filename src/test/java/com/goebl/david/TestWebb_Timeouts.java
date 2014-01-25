package com.goebl.david;

import java.net.SocketTimeoutException;

public class TestWebb_Timeouts extends AbstractTestWebb {

    public void testConnectTimeoutRequest() throws Exception {
        // throw away artifact under test and create new
        webb = Webb.create();
        try {
            webb.get("http://www.goebl.com/robots.txt").connectTimeout(11).asVoid();
        } catch (WebbException e) {
            assertEquals(SocketTimeoutException.class, e.getCause().getClass());
        }
    }

    public void testConnectTimeoutGlobal() throws Exception {
        // throw away artifact under test and create new
        Webb.setConnectTimeout(11);
        webb = Webb.create();
        try {
            webb.get("http://www.goebl.com/robots.txt").asVoid();
        } catch (WebbException e) {
            assertEquals(SocketTimeoutException.class, e.getCause().getClass());
        } finally {
            Webb.setConnectTimeout(10000);
        }
    }

    public void testConnectTimeoutRequestOverrulesGlobal() throws Exception {
        // throw away artifact under test and create new
        Webb.setConnectTimeout(11);
        webb = Webb.create();
        try {
            webb.get("http://www.goebl.com/robots.txt").connectTimeout(10000).asVoid();
        } catch (WebbException e) {
            fail("no exception expected (only if server is down), but is: " + e);
        } finally {
            Webb.setConnectTimeout(10000);
        }
    }

    public void testReadTimeoutRequest() throws Exception {
        // the REST api delivers after 500 millis
        webb.get("/read-timeout").readTimeout(800).ensureSuccess().asString();

        try {
            webb.get("/read-timeout").readTimeout(100).asString();
        } catch (WebbException e) {
            assertEquals(SocketTimeoutException.class, e.getCause().getClass());
        }
    }

    public void testReadTimeoutGlobal() throws Exception {
        // the REST api delivers after 500 millis
        Webb.setReadTimeout(800);
        webb.get("/read-timeout").ensureSuccess().asString();

        try {
            Webb.setReadTimeout(100);
            webb.get("/read-timeout").asString();
        } catch (WebbException e) {
            assertEquals(SocketTimeoutException.class, e.getCause().getClass());
        } finally {
            Webb.setReadTimeout(180000);
        }
    }

    public void testReadTimeoutRequestOverrulesGlobal() throws Exception {
        Webb.setReadTimeout(11);
        try {
            webb.get("/read-timeout").readTimeout(1000).asString();
        } catch (WebbException e) {
            fail("no exception expected (only if server is busy), but is: " + e);
        } finally {
            Webb.setReadTimeout(180000);
        }
    }

}
