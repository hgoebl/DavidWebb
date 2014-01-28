package com.goebl.david;

import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestWebbUtils_Mock extends TestCase {

    private Webb webb;

    public void setUp() throws Exception {
        super.setUp();
        webb = Webb.create(); // not necessary to mock this
    }

    public void testAddRequestProperties() throws Exception {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        String dateStr = WebbUtils.getRfc1123DateFormat().format(now);

        HttpURLConnection connection = mock(HttpURLConnection.class);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("date", now);
        map.put("calendar", calendar);
        map.put("int", 4711);
        map.put("bool", true);

        WebbUtils.addRequestProperties(connection, map);

        verify(connection).addRequestProperty("date", dateStr);
        verify(connection).addRequestProperty("calendar", dateStr);
        verify(connection).addRequestProperty("int", "4711");
        verify(connection).addRequestProperty("bool", "true");
    }

    public void testAddRequestProperties_Empty() throws Exception {
        HttpURLConnection connection = mock(HttpURLConnection.class);
        doThrow(new RuntimeException()).when(connection).addRequestProperty(anyString(), anyString());
        WebbUtils.addRequestProperties(connection, null);
    }

    public void testAddRequestProperty() throws Exception {
        HttpURLConnection connection = mock(HttpURLConnection.class);

        WebbUtils.addRequestProperty(connection, "name1", "value1");
        WebbUtils.addRequestProperty(connection, "name2", "value2");
        verify(connection).addRequestProperty("name1", "value1");
        verify(connection).addRequestProperty("name2", "value2");
    }

    public void testEnsureRequestProperty() throws Exception {
        Map<String,List<String>> headers = new HashMap<String, List<String>>();
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getRequestProperties()).thenReturn(headers);

        WebbUtils.ensureRequestProperty(connection, "name", "value");

        verify(connection).addRequestProperty("name", "value");
    }

    public void testGetPayloadAsBytesAndSetContentType_Form() throws Exception {
        Request request = new Request(webb, null, null);
        request.payload = null;
        request.params = new LinkedHashMap<String, Object>();
        request.params.put("abc", 123);
        request.params.put("email", "abc@def.com");

        HttpURLConnection connection = mock(HttpURLConnection.class);
        byte[] payload = WebbUtils.getPayloadAsBytesAndSetContentType(connection, request, false, -1);

        assertArrayEquals("abc=123&email=abc%40def.com".getBytes("UTF-8"), payload);
        verify(connection).setFixedLengthStreamingMode(payload.length);
        verify(connection).addRequestProperty(Const.HDR_CONTENT_TYPE, Const.APP_FORM);
    }

    public void testGetPayloadAsBytesAndSetContentType_null() throws Exception {
        Request request = new Request(webb, null, null);
        request.payload = null;

        HttpURLConnection connection = mock(HttpURLConnection.class);
        byte[] payload = WebbUtils.getPayloadAsBytesAndSetContentType(connection, request, false, -1);

        assertNull(payload);
        verify(connection, never()).setFixedLengthStreamingMode(anyInt());
        verify(connection, never()).addRequestProperty(eq(Const.HDR_CONTENT_TYPE), anyString());
    }

    public void testGetPayloadAsBytesAndSetContentType_JSONObject() throws Exception {
        Request request = new Request(webb, null, null);

        JSONObject json = new JSONObject();
        json.put("int", 1);
        json.put("bool", true);
        json.put("str", "a string");

        request.payload = json;

        byte[] expected = json.toString().getBytes("UTF-8");

        HttpURLConnection connection = mock(HttpURLConnection.class);
        byte[] payload = WebbUtils.getPayloadAsBytesAndSetContentType(connection, request, false, -1);

        assertArrayEquals(expected, payload);
        verify(connection).setFixedLengthStreamingMode(expected.length);
        verify(connection).addRequestProperty(Const.HDR_CONTENT_TYPE, Const.APP_JSON);
    }

    public void testGetPayloadAsBytesAndSetContentType_JSONArray() throws Exception {
        Request request = new Request(webb, null, null);

        JSONArray json = new JSONArray();
        json.put(1);
        json.put(true);
        json.put("a string");

        request.payload = json;
        byte[] expected = json.toString().getBytes("UTF-8");

        HttpURLConnection connection = mock(HttpURLConnection.class);
        byte[] payload = WebbUtils.getPayloadAsBytesAndSetContentType(connection, request, false, -1);

        assertArrayEquals(expected, payload);
        verify(connection).setFixedLengthStreamingMode(expected.length);
        verify(connection).addRequestProperty(Const.HDR_CONTENT_TYPE, Const.APP_JSON);
    }

    public void testGetPayloadAsBytesAndSetContentType_String() throws Exception {
        Request request = new Request(webb, null, null);
        String strPayload = "\"München 1 Maß 10 €\"";
        request.payload = strPayload;
        byte[] expected = strPayload.getBytes("UTF-8");

        HttpURLConnection connection = mock(HttpURLConnection.class);
        byte[] payload = WebbUtils.getPayloadAsBytesAndSetContentType(connection, request, false, -1);

        assertArrayEquals(expected, payload);
        verify(connection).setFixedLengthStreamingMode(expected.length);
        verify(connection).addRequestProperty(Const.HDR_CONTENT_TYPE, Const.TEXT_PLAIN);
    }

    public void testGetPayloadAsBytesAndSetContentType_bytes() throws Exception {
        Request request = new Request(webb, null, null);
        String strPayload = "\"München 1 Maß 10 €\"";
        request.payload = strPayload.getBytes("UTF-8");
        byte[] expected = strPayload.getBytes("UTF-8");

        HttpURLConnection connection = mock(HttpURLConnection.class);
        byte[] payload = WebbUtils.getPayloadAsBytesAndSetContentType(connection, request, false, -1);

        assertArrayEquals(expected, payload);
        verify(connection).setFixedLengthStreamingMode(expected.length);
        verify(connection).addRequestProperty(Const.HDR_CONTENT_TYPE, Const.APP_BINARY);
    }

    public void testParseResponseBody() throws Exception {
        // String
        Response response = mock(Response.class);
        String expected = "München 1 Maß 10 €";
        byte[] payload = expected.getBytes("UTF-8");

        WebbUtils.parseResponseBody(String.class, response, payload);
        verify(response).setBody(expected);

        // byte[]
        response = mock(Response.class);

        WebbUtils.parseResponseBody(Const.BYTE_ARRAY_CLASS, response, payload);
        verify(response).setBody(payload);

        // void
        response = mock(Response.class);

        WebbUtils.parseResponseBody(Void.class, response, null);
        verify(response, never()).setBody(anyObject());
    }

    public void testParseErrorBody() throws Exception {
        Response response = mock(Response.class);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        response.connection = connection;

        String expected = "München 1 Maß 10 €";
        byte[] payload = expected.getBytes("UTF-8");

        // this call should not eat any of the getContentType() return values
        WebbUtils.parseErrorResponse(String.class, null, null); // without NPE it's OK!
        assertNull(response.errorBody);
        when(connection.getContentType()).thenReturn(
                Const.APP_BINARY, // 1
                null,             // 2
                Const.APP_JSON,   // 3
                Const.APP_JSON,   // 4
                "text/plain; charset=UTF-8");    // 5


        // (1) we expect a byte[] and Content-Type = app/bin => should be byte[]
        WebbUtils.parseErrorResponse(Const.BYTE_ARRAY_CLASS, response, payload);
        assertNotNull(response.errorBody);
        assertArrayEquals(payload, (byte[]) response.errorBody);
        response.errorBody = null;

        // (2) we expect a byte[] and Content-Type is null => should be byte[]
        WebbUtils.parseErrorResponse(Const.BYTE_ARRAY_CLASS, response, payload);
        assertNotNull(response.errorBody);
        assertArrayEquals(payload, (byte[]) response.errorBody);
        response.errorBody = null;

        // (3) we expect a String and Content-Type is JSON => should be String
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", expected);
        payload = jsonObject.toString().getBytes(Const.UTF8);
        WebbUtils.parseErrorResponse(String.class, response, payload);
        assertNotNull(response.errorBody);
        assertEquals(jsonObject.toString(), (String) response.errorBody);
        response.errorBody = null;

        // (4) we expect JSON and Content-Type is JSON => should return JSON
        WebbUtils.parseErrorResponse(JSONObject.class, response, payload);
        assertNotNull(response.errorBody);
        assertEquals(JSONObject.class, response.errorBody.getClass());
        assertEquals(jsonObject.toString(), ((JSONObject) response.errorBody).toString());
        response.errorBody = null;

        // (5) we expect JSON and Content-Type is text => should return String
        WebbUtils.parseErrorResponse(JSONObject.class, response, payload);
        assertNotNull(response.errorBody);
        assertEquals(String.class, response.errorBody.getClass());
        assertEquals(jsonObject.toString(), response.errorBody);
    }

    private void assertArrayEquals(byte[] expected, byte[] bytes) {
        assertEquals("array length mismatch", expected.length, bytes.length);
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != bytes[i]) {
                fail(String.format("array different at index %d expected: %d, is: %d", i, expected[i], bytes[i]));
            }
        }
    }

}
