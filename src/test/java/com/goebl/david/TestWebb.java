package com.goebl.david;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.TimeZone;

public class TestWebb extends AbstractTestWebb {

    public void testMisc() throws Exception {
        Request request = webb
                .get("/ping")
                .useCaches(true);

        assertEquals(Request.Method.GET, request.method);
        assertEquals(true, request.useCaches);

        Response<String> response = request.asString();

        assertTrue(response.isSuccess());
        assertEquals("pong", response.getBody());
        assertNull(response.getErrorBody());

        HttpURLConnection connection = response.getConnection();
        assertNotNull(connection);
        assertEquals(connection.getResponseMessage(), response.getResponseMessage());
        assertEquals(connection.getResponseCode(), response.getStatusCode());

        assertSame(request, response.getRequest());
    }

    public void testIgnoreBaseUri() throws Exception {
        webb.get("http://www.goebl.com/robots.txt").ensureSuccess().asVoid();
    }

    public void testSimpleGetText() throws Exception {
        Response<String> response = webb
                .get("/simple.txt")
                .param("p1", SIMPLE_ASCII)
                .param("p2", COMPLEX_UTF8)
                .asString();

        assertEquals(200, response.getStatusCode());
        assertTrue(response.isSuccess());
        assertEquals(HTTP_MESSAGE_OK, response.getResponseMessage());
        assertEquals("HTTP/1.1 200 OK", response.getStatusLine());

        assertEquals(SIMPLE_ASCII + ", " + COMPLEX_UTF8, response.getBody());
        assertEquals(Webb.TEXT_PLAIN, response.getContentType());
    }

    public void testSimplePostText() throws Exception {
        Response<String> response = webb
                .post("/simple.txt")
                .param("p1", SIMPLE_ASCII)
                .param("p2", COMPLEX_UTF8)
                .asString();

        assertEquals(200, response.getStatusCode());
        assertEquals(HTTP_MESSAGE_OK, response.getResponseMessage());
        assertEquals(SIMPLE_ASCII + ", " + COMPLEX_UTF8, response.getBody());
        assertTrue(response.getContentType().startsWith(Webb.TEXT_PLAIN));
    }

    public void testEchoPostText() throws Exception {
        String expected = SIMPLE_ASCII + ", " + COMPLEX_UTF8;
        Response<String> response = webb
                .post("/echoText")
                .body(expected)
                .asString();

        assertEquals(200, response.getStatusCode());
        assertEquals(HTTP_MESSAGE_OK, response.getResponseMessage());
        assertEquals(expected, response.getBody());
        assertTrue(response.getContentType().startsWith(Webb.TEXT_PLAIN));
    }

    public void testSimpleGetJson() throws Exception {
        Response<JSONObject> response = webb
                .get("/simple.json")
                .param("p1", SIMPLE_ASCII)
                .param("p2", COMPLEX_UTF8)
                .useCaches(false)
                .asJsonObject();

        assertEquals(200, response.getStatusCode());
        assertEquals(HTTP_MESSAGE_OK, response.getResponseMessage());
        assertTrue(response.getContentType().startsWith(Webb.APP_JSON));
        JSONObject result = response.getBody();
        assertNotNull(result);
        assertEquals(SIMPLE_ASCII, result.getString("p1"));
        assertEquals(COMPLEX_UTF8, result.getString("p2"));
    }

    public void testSimplePutJson() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("p1", SIMPLE_ASCII);
        payload.put("p2", COMPLEX_UTF8);

        Response<JSONObject> response = webb
                .put("/simple.json")
                .body(payload)
                .asJsonObject();

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getContentType().startsWith(Webb.APP_JSON));
        JSONObject result = response.getBody();
        assertNotNull(result);
        assertEquals(SIMPLE_ASCII, result.getString("p1"));
        assertEquals(COMPLEX_UTF8, result.getString("p2"));
    }

    public void testSimplePostJson() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("p1", SIMPLE_ASCII);
        payload.put("p2", COMPLEX_UTF8);

        Response<Void> response = webb
                .post("/simple.json")
                .body(payload)
                .asVoid();

        assertEquals(201, response.getStatusCode());
        assertTrue(response.isSuccess());
        assertEquals("Created", response.getResponseMessage());
        assertEquals("http://example.com/4711", response.getHeaderField("Location"));
    }

    public void testSimpleDelete() throws Exception {

        Response<Void> response = webb
                .delete("/simple")
                .asVoid();

        assertEquals(204, response.getStatusCode());
        assertTrue(response.isSuccess());
        assertEquals("No Content", response.getResponseMessage());
    }

    public void testNoContent() throws Exception {

        Response<Void> responseAsVoid = webb
                .get("/no-content")
                .asVoid();

        assertEquals(204, responseAsVoid.getStatusCode());
        assertTrue(responseAsVoid.isSuccess());
        assertEquals("No Content", responseAsVoid.getResponseMessage());

        Response<String> responseAsString = webb
                .get("/no-content")
                .asString();

        assertEquals(204, responseAsString.getStatusCode());
        assertTrue(responseAsString.isSuccess());
        assertEquals("No Content", responseAsString.getResponseMessage());
        assertEquals("", responseAsString.getBody());
    }

    public void testParameterTypes() throws Exception {
        Response<String> response = webb
                .get("/parameter-types")
                .param("string", SIMPLE_ASCII)
                .param("number", 4711)
                .param("null", null)
                .param("empty", "")
                .asString();

        assertEquals(204, response.getStatusCode());
    }

    public void testHeadersIn() throws Exception {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(2013, Calendar.NOVEMBER, 24, 23, 59, 33);

        Response<Void> response = webb
                .get("/headers/in")
                .header("x-test-string", SIMPLE_ASCII)
                .header("x-test-int", 4711)
                .header("x-test-calendar", cal)
                .header("x-test-date", cal.getTime())
                .param(Webb.HDR_USER_AGENT, Webb.DEFAULT_USER_AGENT)
                .asVoid();

        assertEquals(200, response.getStatusCode());
    }

    public void testHeadersOut() throws Exception {

        Response<Void> response = webb
                .get("/headers/out")
                .asVoid();
        long nowMoreOrLess = System.currentTimeMillis();

        assertEquals(200, response.getStatusCode());
        assertEquals(4711, response.getHeaderFieldInt("x-test-int", 0));
        long serverTime = response.getHeaderFieldDate("x-test-datum", 0L);

        assertTrue(Math.abs(serverTime - nowMoreOrLess) < 5000);

        serverTime = response.getDate();
        assertTrue(Math.abs(serverTime - nowMoreOrLess) < 5000);

        assertEquals(SIMPLE_ASCII, response.getHeaderField("x-test-string"));
    }

    public void testHeaderExpires() throws Exception {

        long offset = 3600 * 1000;
        Response<Void> response = webb
                .get("/headers/expires")
                .param("offset", offset)
                .asVoid();

        assertEquals(200, response.getStatusCode());
        long expiresRaw = response.getHeaderFieldDate("Expires", 0L);
        long expires = response.getExpiration();

        // <10 seconds time drift is ok
        long delta = expires - offset - System.currentTimeMillis();
        if (Math.abs(delta) > 10000) {
            fail("expires / offset mismatch: " + expires + " / " + offset + " delta=" + delta);
        }

        assertEquals(expiresRaw, expires);
    }

    public void testIfModifiedSince() throws Exception {

        long lastModified = System.currentTimeMillis() - 10000; // resource was modified 10 seconds ago

        // we ask if it was modified earlier than 100 seconds ago => yes!
        Response<Void> response = webb
                .get("/headers/if-modified-since")
                .ifModifiedSince(lastModified - 100000)
                .param("lastModified", lastModified)
                .asVoid();

        assertEquals(200, response.getStatusCode());

        // we ask if it was modified earlier than 5 seconds ago => no!
        response = webb
                .get("/headers/if-modified-since")
                .ifModifiedSince(lastModified + 5000)
                .param("lastModified", lastModified)
                .asVoid();

        assertEquals(304, response.getStatusCode());
    }

    public void testLastModified() throws Exception {

        long lastModified = (System.currentTimeMillis() / 1000) * 1000L;

        Response<Void> response = webb
                .get("/headers/last-modified")
                .param("lastModified", lastModified)
                .asVoid();

        assertEquals(200, response.getStatusCode());
        assertEquals(lastModified, response.getLastModified());
    }

    public void testEnsureSuccess() throws Exception {
        String result = webb.get("/ping").ensureSuccess().asString().getBody();
        assertEquals("pong", result);
    }

    // should be moved to TestRequest
    public void testGetUri() throws Exception {

        webb.setBaseUri("http://example.com");
        Request request = webb.get("/simple.txt");

        assertEquals("http://example.com/simple.txt", request.getUri());
    }
}
