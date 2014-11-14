package com.goebl.david;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Static utility method and tools for HTTP traffic parsing and encoding.
 *
 * @author hgoebl
 */
public class WebbUtils {

    protected WebbUtils() {}

    /**
     * Convert a Map to a query string.
     * @param values the map with the values
     *               <code>null</code> will be encoded as empty string, all other objects are converted to
     *               String by calling its <code>toString()</code> method.
     * @return e.g. "key1=value&key2=&email=max%40example.com"
     */
    public static String queryString(Map<String, Object> values) {
        StringBuilder sbuf = new StringBuilder();
        String separator = "";

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String value = entry.getValue() == null ? "" : String.valueOf(entry.getValue());
            sbuf.append(separator);
            sbuf.append(urlEncode(entry.getKey()));
            sbuf.append('=');
            sbuf.append(urlEncode(value));
            separator = "&";
        }

        return sbuf.toString();
    }

    /**
     * Convert a byte array to a JSONObject.
     * @param bytes a UTF-8 encoded string representing a JSON object.
     * @return the parsed object
     * @throws WebbException in case of error (usually a parsing error due to invalid JSON)
     */
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

    /**
     * Convert a byte array to a JSONArray.
     * @param bytes a UTF-8 encoded string representing a JSON array.
     * @return the parsed JSON array
     * @throws WebbException in case of error (usually a parsing error due to invalid JSON)
     */
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

    /**
     * Read an <code>InputStream</code> into <code>byte[]</code> until EOF.
     * <br/>
     * Does not close the InputStream!
     *
     * @param is the stream to read the bytes from
     * @return all read bytes as an array
     * @throws IOException
     */
    public static byte[] readBytes(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        byte[] responseBody;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copyStream(is, baos);
        baos.close();
        responseBody = baos.toByteArray();
        return responseBody;
    }

    /**
     * Copy complete content of <code>InputStream</code> to <code>OutputStream</code> until EOF.
     * <br/>
     * Does not close the InputStream nor OutputStream!
     *
     * @param input the stream to read the bytes from
     * @param output the stream to write the bytes to
     * @throws IOException
     */
    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int count;
        while ((count = input.read(buffer)) != -1) {
            output.write(buffer, 0, count);
        }
    }

    /**
     * Creates a new instance of a <code>DateFormat</code> for RFC1123 compliant dates.
     * <br/>
     * Should be stored for later use but be aware that this DateFormat is not Thread-safe!
     * <br/>
     * If you have to deal with dates in this format with JavaScript, it's easy, because the JavaScript
     * Date object has a constructor for strings formatted this way.
     * @return a new instance
     */
    public static DateFormat getRfc1123DateFormat() {
        DateFormat format = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        format.setLenient(false);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format;
    }

    static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    static void addRequestProperties(HttpURLConnection connection, Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            addRequestProperty(connection, entry.getKey(), entry.getValue());
        }
    }

    static void addRequestProperty(HttpURLConnection connection, String name, Object value) {
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
            addRequestProperty(connection, name, value);
        }
    }

    static byte[] getPayloadAsBytesAndSetContentType(
            HttpURLConnection connection,
            Request request,
            boolean compress,
            int jsonIndentFactor) throws JSONException, UnsupportedEncodingException {

        byte[] requestBody = null;
        String bodyStr = null;

        if (request.params != null) {
            WebbUtils.ensureRequestProperty(connection, Const.HDR_CONTENT_TYPE, Const.APP_FORM);
            bodyStr = WebbUtils.queryString(request.params);
        } else if (request.payload == null) {
            return null;
        } else if (request.payload instanceof JSONObject) {
            WebbUtils.ensureRequestProperty(connection, Const.HDR_CONTENT_TYPE, Const.APP_JSON);
            bodyStr = jsonIndentFactor >= 0
                    ? ((JSONObject) request.payload).toString(jsonIndentFactor)
                    : request.payload.toString();
        } else if (request.payload instanceof JSONArray) {
            WebbUtils.ensureRequestProperty(connection, Const.HDR_CONTENT_TYPE, Const.APP_JSON);
            bodyStr = jsonIndentFactor >= 0
                    ? ((JSONArray) request.payload).toString(jsonIndentFactor)
                    : request.payload.toString();
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

        if (requestBody == null) {
            throw new IllegalStateException();
        }

        // only compress if the new body is smaller than uncompressed body
        if (compress && requestBody.length > Const.MIN_COMPRESSED_ADVANTAGE) {
            byte[] compressedBody = gzip(requestBody);
            if (requestBody.length - compressedBody.length > Const.MIN_COMPRESSED_ADVANTAGE) {
                requestBody = compressedBody;
                connection.setRequestProperty(Const.HDR_CONTENT_ENCODING, "gzip");
            }
        }

        connection.setFixedLengthStreamingMode(requestBody.length);

        return requestBody;
    }

    static void setContentTypeAndLengthForStreaming(
            HttpURLConnection connection,
            Request request,
            boolean compress) {

        long length;

        if (request.payload instanceof File) {
            length = compress ? -1L : ((File) request.payload).length();
        } else if (request.payload instanceof InputStream) {
            length = -1L;
        } else {
            throw new IllegalStateException();
        }

        if (length > Integer.MAX_VALUE) {
            length = -1L; // use chunked streaming mode
        }

        WebbUtils.ensureRequestProperty(connection, Const.HDR_CONTENT_TYPE, Const.APP_BINARY);
        if (length < 0) {
            connection.setChunkedStreamingMode(-1); // use default chunk size
            if (compress) {
                connection.setRequestProperty(Const.HDR_CONTENT_ENCODING, "gzip");
            }
        } else {
            connection.setFixedLengthStreamingMode((int) length);
        }
    }

    static byte[] gzip(byte[] input) {
        GZIPOutputStream gzipOS = null;
        try {
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            gzipOS = new GZIPOutputStream(byteArrayOS);
            gzipOS.write(input);
            gzipOS.flush();
            gzipOS.close();
            gzipOS = null;
            return byteArrayOS.toByteArray();
        } catch (Exception e) {
            throw new WebbException(e);
        } finally {
            if (gzipOS != null) {
                try { gzipOS.close(); } catch (Exception ignored) {}
            }
        }
    }

    static InputStream wrapStream(String contentEncoding, InputStream inputStream) throws IOException {
        if (contentEncoding == null || "identity".equalsIgnoreCase(contentEncoding)) {
            return inputStream;
        }
        if ("gzip".equalsIgnoreCase(contentEncoding)) {
            return new GZIPInputStream(inputStream);
        }
        if ("deflate".equalsIgnoreCase(contentEncoding)) {
            return new InflaterInputStream(inputStream, new Inflater(false), 512);
        }
        throw new WebbException("unsupported content-encoding: " + contentEncoding);
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

    static <T> void parseErrorResponse(Class<T> clazz, Response<T> response, byte[] responseBody)
            throws UnsupportedEncodingException {

        if (responseBody == null) {
            return;
        }

        String contentType = response.connection.getContentType();
        if (contentType == null || contentType.startsWith(Const.APP_BINARY) || clazz == Const.BYTE_ARRAY_CLASS) {
            response.errorBody = responseBody;
            return;
        }

        if (contentType.startsWith(Const.APP_JSON) && clazz == JSONObject.class) {
            try {
                response.errorBody = WebbUtils.toJsonObject(responseBody);
                return;
            } catch (Exception ignored) {
                // ignored - was just a try!
            }
        }

        // fallback to String if bytes are valid UTF-8 characters ...
        try {
            response.errorBody = new String(responseBody, Const.UTF8);
            return;
        } catch (Exception ignored) {
            // ignored - was just a try!
        }

        // last fallback - return error object as byte[]
        response.errorBody = responseBody;
    }

}