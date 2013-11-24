package com.goebl.david;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.*;

import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.*;

// TODO test read-timeout
// TODO test redirect (follow redirect true/false)
// TODO express res.location and res.redirect
// TODO express res.charset = "value" => Content-Type: text/html; charset=value

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

    @Test public void parameterTypes() throws Exception {
        Response<String> response = webb
                .get("/parameter-types")
                .param("string", SIMPLE_ASCII)
                .param("number", 4711)
                .param("null", null)
                .param("empty", "")
                .asString();

        assertEquals(204, response.getStatusCode());
    }

    @Test public void headersIn() throws Exception {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(2013, Calendar.NOVEMBER, 24, 23, 59, 33);

        Response<Void> response = webb
                .get("/headers/in")
                .header("x-test-string", COMPLEX_UTF8)
                .header("x-test-int", 4711)
                .header("x-test-calendar", cal)
                .header("x-test-date", cal.getTime())
                .param(Webb.HDR_USER_AGENT, Webb.DEFAULT_USER_AGENT)
                .asVoid();

        assertEquals(200, response.getStatusCode());
    }

    @Test public void headersOut() throws Exception {

        Response<Void> response = webb
                .get("/headers/out")
                .asVoid();

        assertEquals(200, response.getStatusCode());
        assertEquals(4711, response.getHeaderFieldInt("x-test-int", 0));
        long serverTime = response.getHeaderFieldDate("x-test-date", 0L);
        assertTrue(Math.abs(serverTime - System.currentTimeMillis()) < 1000);

        serverTime = response.getDate();
        assertTrue(Math.abs(serverTime - System.currentTimeMillis()) < 1000);

        assertEquals(SIMPLE_ASCII, response.getHeaderField("x-test-string"));
    }

    @Test public void headerExpires() throws Exception {

        long offset = 3600 * 1000;
        Response<Void> response = webb
                .get("/headers/expires")
                .param("offset", offset)
                .asVoid();

        assertEquals(200, response.getStatusCode());
        long expiresRaw = response.getHeaderFieldDate("Expires", 0L);
        long expires = response.getExpiration();

        assertTrue(Math.abs(expires - offset - System.currentTimeMillis()) < 10000); // <10 seconds time drift is ok
        assertEquals(expiresRaw, expires);
    }

    @Test public void ifModifiedSince() throws Exception {

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

    @Test public void lastModified() throws Exception {

        long lastModified = (System.currentTimeMillis() / 1000) * 1000L;

        Response<Void> response = webb
                .get("/headers/last-modified")
                .param("lastModified", lastModified)
                .asVoid();

        assertEquals(200, response.getStatusCode());
        assertEquals(lastModified, response.getLastModified());
    }

    @Test public void httpsValidCertificate() throws Exception {
        webb.setBaseUri(null);

        Response<JSONObject> response = webb
                .get("https://www.googleapis.com/oauth2/v1/certs")
                .ensureSuccess()
                .asJsonObject();

        assertEquals(200, response.getStatusCode());
    }

    @Test public void httpsInvalidCertificate() throws Exception {
        webb.setBaseUri(null);

        try {
            webb.get("https://tv.eurosport.com/").ensureSuccess().asString();
            fail();
        } catch (WebbException e) {
            assertEquals(SSLHandshakeException.class, e.getCause().getClass());
        }
    }

    @Test public void httpsHostnameIgnore() throws Exception {
        webb.setBaseUri(null);
        webb.setHostnameVerifier(new TrustingHostnameVerifier());

        // www.wellcrafted.de has same IP as www.goebl.com, but certificate is for www.goebl.com
        Response<Void> response = webb.get("https://www.wellcrafted.de/").asVoid();
        assertTrue(response.isSuccess());
    }

    @Test public void httpsInvalidCertificateAndHostnameIgnore() throws Exception {
        webb.setBaseUri(null);

        TrustManager[] trustAllCerts = new TrustManager[] { new AlwaysTrustManager() };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        webb.setSSLSocketFactory(sslContext.getSocketFactory());
        webb.setHostnameVerifier(new TrustingHostnameVerifier());

        Response<Void> response = webb.get("https://tv.eurosport.com/").asVoid();
        assertTrue(response.isSuccess() || response.getStatusCode() == 301);
    }

    private static class TrustingHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private static class AlwaysTrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) { }
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) { }
        public X509Certificate[] getAcceptedIssuers() { return null; }
    }
}
