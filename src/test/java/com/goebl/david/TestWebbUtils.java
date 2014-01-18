package com.goebl.david;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestWebbUtils {
    @Test
    public void testQueryString() throws Exception {
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        assertEquals("empty map", "", WebbUtils.queryString(map));

        map.put("abc", 123);
        assertEquals("simple single param", "abc=123", WebbUtils.queryString(map));

        map.put("dumb param", Boolean.TRUE);
        assertEquals("uri-encode param", "abc=123&dumb+param=true", WebbUtils.queryString(map));

        map.clear();
        map.put("email", "abc@abc.com");
        assertEquals("uri-encode value", "email=abc%40abc.com", WebbUtils.queryString(map));
    }

    @Test
    public void testUrlEncode() throws Exception {
        assertEquals("", WebbUtils.urlEncode(""));
        assertEquals("normal-ascii", WebbUtils.urlEncode("normal-ascii"));

        // instead of '+' for space '%20' is valid as well; in case of problems adapt test
        assertEquals("Hello%2FWorld+%26+Co.%3F", WebbUtils.urlEncode("Hello/World & Co.?"));
        assertEquals("M%C3%BCnchen+1+Ma%C3%9F+10+%E2%82%AC", WebbUtils.urlEncode("München 1 Maß 10 €"));
    }

    @Test
    public void testToJsonObject() throws Exception {
        JSONObject jsonExpected = new JSONObject();
        jsonExpected.put("int", 1);
        jsonExpected.put("bool", true);
        jsonExpected.put("str", "a string");
        String jsonStr = jsonExpected.toString();

        JSONObject json = WebbUtils.toJsonObject(jsonStr.getBytes("UTF-8"));

        assertEquals(jsonExpected.toString(), json.toString());
    }

    @Test(expected = WebbException.class)
    public void testToJsonObjectFail() throws Exception {
        // JSONObject parser is very forgiving: without ',' it would get parsed - unbelievable!
        JSONObject json = WebbUtils.toJsonObject("{in, valid: 'json object'}".getBytes("UTF-8"));
        // System.out.println(json.toString());
    }

    @Test
    public void testToJsonArray() throws Exception {
        JSONArray jsonExpected = new JSONArray();
        jsonExpected.put(1);
        jsonExpected.put(true);
        jsonExpected.put("a string");
        String jsonStr = jsonExpected.toString();

        JSONArray json = WebbUtils.toJsonArray(jsonStr.getBytes("UTF-8"));

        assertEquals(jsonExpected.toString(), json.toString());
    }

    @Test(expected = WebbException.class)
    public void testToJsonArrayFail() throws Exception {
        JSONArray array = WebbUtils.toJsonArray("[in - valid, 'json! array]".getBytes("UTF-8"));
        // System.out.println(array.toString());
    }

    @Test
    public void testReadBytes() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1024 * 2 + 100; ++i) {
            sb.append(Character.valueOf((char) (i % 256)));
        }
        byte[] input = sb.toString().getBytes("UTF-8");
        ByteArrayInputStream is = new ByteArrayInputStream(input);

        byte[] read = WebbUtils.readBytes(is);
        is.close();

        assertArrayEquals(input, read);

        assertNull("return null when is=null", WebbUtils.readBytes(null));
    }

    @Test
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

    @Test
    public void testAddRequestProperties_Empty() throws Exception {
        HttpURLConnection connection = mock(HttpURLConnection.class);
        doThrow(new RuntimeException()).when(connection).addRequestProperty(anyString(), anyString());
        WebbUtils.addRequestProperties(connection, null);
    }

    @Test
    public void testAddRequestProperty() throws Exception {
        HttpURLConnection connection = mock(HttpURLConnection.class);

        WebbUtils.addRequestProperty(connection, "name1", "value1");
        WebbUtils.addRequestProperty(connection, "name2", "value2");
        verify(connection).addRequestProperty("name1", "value1");
        verify(connection).addRequestProperty("name2", "value2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddRequestProperty_valueNull() throws Exception {
        WebbUtils.addRequestProperty(null, "name1", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddRequestProperty_nameNull() throws Exception {
        WebbUtils.addRequestProperty(null, null, "abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddRequestProperty_nameEmpty() throws Exception {
        WebbUtils.addRequestProperty(null, "", "abc");
    }

    @Test
    public void testEnsureRequestProperty() throws Exception {
        Map<String,List<String>> headers = new HashMap<String, List<String>>();
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getRequestProperties()).thenReturn(headers);

        WebbUtils.ensureRequestProperty(connection, "name", "value");

        verify(connection).addRequestProperty("name", "value");
    }

    @Test
    public void testGetRfc1123DateFormat() throws Exception {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(2013, Calendar.DECEMBER, 24, 23, 59);
        cal.set(Calendar.SECOND, 30);
        cal.set(Calendar.MILLISECOND, 501);
        Date date = cal.getTime();

        DateFormat dateFormat = WebbUtils.getRfc1123DateFormat();
        String formatted = dateFormat.format(date);

        assertEquals("Tue, 24 Dec 2013 23:59:30 GMT", formatted);
    }

    @Test
    public void testGetPayloadAsBytesAndSetContentType_Form() throws Exception {
        Request request = new Request(null, null, null);
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

    @Test
    public void testGetPayloadAsBytesAndSetContentType_null() throws Exception {
        Request request = new Request(null, null, null);
        request.payload = null;

        HttpURLConnection connection = mock(HttpURLConnection.class);
        byte[] payload = WebbUtils.getPayloadAsBytesAndSetContentType(connection, request, false, -1);

        assertNull(payload);
        verify(connection, never()).setFixedLengthStreamingMode(anyInt());
        verify(connection, never()).addRequestProperty(eq(Const.HDR_CONTENT_TYPE), anyString());
    }

    @Test
    public void testGetPayloadAsBytesAndSetContentType_JSONObject() throws Exception {
        Request request = new Request(null, null, null);

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

    @Test
    public void testGetPayloadAsBytesAndSetContentType_JSONArray() throws Exception {
        Request request = new Request(null, null, null);

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

    @Test
    public void testGetPayloadAsBytesAndSetContentType_String() throws Exception {
        Request request = new Request(null, null, null);
        String strPayload = "\"München 1 Maß 10 €\"";
        request.payload = strPayload;
        byte[] expected = strPayload.getBytes("UTF-8");

        HttpURLConnection connection = mock(HttpURLConnection.class);
        byte[] payload = WebbUtils.getPayloadAsBytesAndSetContentType(connection, request, false, -1);

        assertArrayEquals(expected, payload);
        verify(connection).setFixedLengthStreamingMode(expected.length);
        verify(connection).addRequestProperty(Const.HDR_CONTENT_TYPE, Const.TEXT_PLAIN);
    }

    @Test
    public void testGetPayloadAsBytesAndSetContentType_bytes() throws Exception {
        Request request = new Request(null, null, null);
        String strPayload = "\"München 1 Maß 10 €\"";
        request.payload = strPayload.getBytes("UTF-8");
        byte[] expected = strPayload.getBytes("UTF-8");

        HttpURLConnection connection = mock(HttpURLConnection.class);
        byte[] payload = WebbUtils.getPayloadAsBytesAndSetContentType(connection, request, false, -1);

        assertArrayEquals(expected, payload);
        verify(connection).setFixedLengthStreamingMode(expected.length);
        verify(connection).addRequestProperty(Const.HDR_CONTENT_TYPE, Const.APP_BINARY);
    }

    @Test
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
}
