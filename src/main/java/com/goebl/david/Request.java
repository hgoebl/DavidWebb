package com.goebl.david;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builder and executor for an HTTP request.
 *
 * @author hgoebl
 */
public class Request {
    public enum Method {
        GET, POST, PUT, DELETE
    }

    private final Webb webb;
    final Method method;
    final String uri;

    Map<String, Object> params;
    Map<String, Object> headers;
    Object payload;
    boolean useCaches;
    Integer connectTimeout;
    Integer readTimeout;
    Long ifModifiedSince;
    boolean ensureSuccess;

    Request(Webb webb, Method method, String uri) {
        this.webb = webb;
        this.method = method;
        this.uri = uri;
    }

    public Request param(String name, Object value) {
        if (params == null) {
            params = new LinkedHashMap<String, Object>();
        }
        params.put(name, value);
        return this;
    }

    public Request header(String name, Object value) {
        if (headers == null) {
            headers = new LinkedHashMap<String, Object>();
        }
        headers.put(name, value);
        return this;
    }

    public Request body(Object body) {
        if (method == Method.GET) {
            throw new IllegalStateException("body not allowed for GET requests");
        }
        this.payload = body;
        return this;
    }

    public Request useCaches(boolean useCaches) {
        this.useCaches = useCaches;
        return this;
    }

    public Request ifModifiedSince(long ifModifiedSince) {
        this.ifModifiedSince = ifModifiedSince;
        return this;
    }

    public Request connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public Request readTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public Request ensureSuccess() {
        this.ensureSuccess = true;
        return this;
    }

    public Response<String> asString() {
        Response<String> response = webb.execute(this, String.class);
        return response;
    }

    public Response<JSONObject> asJsonObject() {
        Response<JSONObject> response = webb.execute(this, JSONObject.class);
        return response;
    }

    public Response<JSONArray> asJsonArray() {
        Response<JSONArray> response = webb.execute(this, JSONArray.class);
        return response;
    }

    public Response<byte[]> asBytes() {
        Response<byte[]> response = (Response<byte[]>) webb.execute(this, Const.BYTE_ARRAY_CLASS);
        return response;
    }

    public Response<Void> asVoid() {
        Response<Void> response = webb.execute(this, Void.class);
        return response;
    }

}
