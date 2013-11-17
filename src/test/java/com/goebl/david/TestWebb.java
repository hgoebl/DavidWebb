package com.goebl.david;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestWebb {
    private static final String SIMPLE_ASCII = "Hello/World & Co.?";
    private static final String COMPLEX_UTF8 = "München 1 Maß 10 €";
    private static final String HTTP_MESSAGE_OK = "OK";

    Webb webb;

    @Before public void createWebb() {
        Webb.setGlobalHeader(Webb.HDR_USER_AGENT, Webb.DEFAULT_USER_AGENT);
        webb = Webb.create();
        webb.setBaseUri("http://localhost:3003");
    }

    @Test public void simpleGetText() throws Exception {
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

    @Test public void simplePostText() throws Exception {
        Response<String> response = webb
                .post("/simple.txt")
                .param("p1", SIMPLE_ASCII)
                .param("p2", COMPLEX_UTF8)
                .asString();

        assertEquals(200, response.getStatusCode());
        assertEquals(HTTP_MESSAGE_OK, response.getResponseMessage());
        assertEquals(SIMPLE_ASCII + ", " + COMPLEX_UTF8, response.getBody());
        assertEquals(Webb.TEXT_PLAIN, response.getContentType());
    }

    @Test public void simpleGetJson() throws Exception {
        Response<JSONObject> response = webb
                .get("/simple.json")
                .param("p1", SIMPLE_ASCII)
                .param("p2", COMPLEX_UTF8)
                .asJsonObject();

        assertEquals(200, response.getStatusCode());
        assertEquals(HTTP_MESSAGE_OK, response.getResponseMessage());
        assertTrue(response.getContentType().startsWith(Webb.APP_JSON));
        JSONObject result = response.getBody();
        assertNotNull(result);
        assertEquals(SIMPLE_ASCII, result.getString("p1"));
        assertEquals(COMPLEX_UTF8, result.getString("p2"));
    }

    @Test public void simplePutJson() throws Exception {
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

    @Test public void simplePostJson() throws Exception {
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
        assertEquals("http://example.com/4711", response.getHeaderField("Link"));
    }

    @Test public void simpleDelete() throws Exception {

        Response<Void> response = webb
                .delete("/simple")
                .asVoid();

        assertEquals(204, response.getStatusCode());
        assertTrue(response.isSuccess());
        assertEquals("No Content", response.getResponseMessage());
    }

    @Test public void headersIn() throws Exception {

        Response<Void> response = webb
                .get("/headers/in")
                .header("x-test-string", COMPLEX_UTF8)
                .header("x-test-int", 4711)
                .param(Webb.HDR_USER_AGENT, Webb.DEFAULT_USER_AGENT)
                .asVoid();

        assertEquals(200, response.getStatusCode());
    }

}
