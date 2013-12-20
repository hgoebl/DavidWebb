package com.goebl.david;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Static utility method and tools for HTTP traffic parsing and encoding.
 *
 * @author hgoebl
 */
public class WebbUtils {

    public static String queryString(Map<String, Object> values) {
        StringBuilder sbuf = new StringBuilder();
        String separator = "";

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String value = entry.getValue() == null ? "" : String.valueOf(entry.getValue());
            sbuf.append(separator);
            sbuf.append(entry.getKey());
            sbuf.append('=');
            sbuf.append(urlEncode(value));
            separator = "&";
        }

        return sbuf.toString();
    }

    public static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Log.e(TAG, e.toString());
            return value;
        }
    }

    public static JSONObject toJsonObject(byte[] bytes) {
        String json;
        try {
            json = new String(bytes, Const.UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new WebbException(e);
        }

        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            throw new WebbException("payload is not a valid JSON object", e);
        }
    }

    public static JSONArray toJsonArray(byte[] bytes) {
        String json;
        try {
            json = new String(bytes, Const.UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new WebbException(e);
        }

        try {
            return new JSONArray(json);
        } catch (JSONException e) {
            throw new WebbException("payload is not a valid JSON array", e);
        }
    }

    public static byte[] readBytes(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        byte[] responseBody;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;
        while ((count = is.read(buffer)) > 0) {
            baos.write(buffer, 0, count);
        }
        baos.close();
        responseBody = baos.toByteArray();
        return responseBody;
    }

    static void addRequestProperties(HttpURLConnection connection, Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            addRequestProperties(connection, entry.getKey(), entry.getValue());
        }
    }

    static void addRequestProperties(HttpURLConnection connection, String name, Object value) {
        if (name == null || name.length() == 0 || value == null) {
            throw new IllegalArgumentException("name and value must not be empty");
        }

        String valueAsString;
        if (value instanceof Date) {
            valueAsString = getRfc1123DateFormat().format((Date) value);
        } else if (value instanceof Calendar) {
            valueAsString = getRfc1123DateFormat().format(((Calendar) value).getTime());
        } else {
            valueAsString = value.toString();
        }

        connection.addRequestProperty(name, valueAsString);
    }

    static void ensureRequestProperty(HttpURLConnection connection, String name, Object value) {
        if (!connection.getRequestProperties().containsKey(name)) {
            addRequestProperties(connection, name, value);
        }
    }

    public static DateFormat getRfc1123DateFormat() {
        DateFormat format = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
        format.setLenient(false);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format;
    }

    static byte[] getPayloadAsBytesAndSetContentType(
            HttpURLConnection connection,
            Request request,
            int jsonIndentFactor) throws JSONException, UnsupportedEncodingException {

        byte[] requestBody = null;
        String bodyStr = null;

        if (request.params != null) {
            WebbUtils.ensureRequestProperty(connection, Const.HDR_CONTENT_TYPE, Const.APP_FORM);
            bodyStr = WebbUtils.queryString(request.params);
        } else if (request.payload == null) {
            requestBody = null;
        } else if (request.payload instanceof JSONObject) {
            WebbUtils.ensureRequestProperty(connection, Const.HDR_CONTENT_TYPE, Const.APP_JSON);
            bodyStr = ((JSONObject)request.payload).toString(jsonIndentFactor);
        } else if (request.payload instanceof JSONArray) {
            WebbUtils.ensureRequestProperty(connection, Const.HDR_CONTENT_TYPE, Const.APP_JSON);
            bodyStr = ((JSONArray)request.payload).toString(jsonIndentFactor);
        } else if (request.payload instanceof byte[]) {
            WebbUtils.ensureRequestProperty(connection, Const.HDR_CONTENT_TYPE, Const.APP_BINARY);
            requestBody = (byte[]) request.payload;
        } else {
            WebbUtils.ensureRequestProperty(connection, Const.HDR_CONTENT_TYPE, Const.TEXT_PLAIN);
            bodyStr = request.payload.toString();
        }
        if (bodyStr != null) {
            requestBody = bodyStr.getBytes(Const.UTF8);
        }

        if (requestBody != null) {
            connection.setFixedLengthStreamingMode(requestBody.length);
        }
        return requestBody;
    }

    static <T> void parseResponseBody(Class<T> clazz, Response<T> response, byte[] responseBody)
            throws UnsupportedEncodingException {

        if (responseBody == null || clazz == Void.class) {
            return;
        }

        // we are ignoring headers describing the content type of the response, instead
        // try to force the content based on the type the client is expecting it (clazz)
        if (clazz == String.class) {
            response.setBody(new String(responseBody, Const.UTF8));
        } else if (clazz == Const.BYTE_ARRAY_CLASS) {
            response.setBody(responseBody);
        } else if (clazz == JSONObject.class) {
            response.setBody(WebbUtils.toJsonObject(responseBody));
        } else if (clazz == JSONArray.class) {
            response.setBody(WebbUtils.toJsonArray(responseBody));
        }
    }

}