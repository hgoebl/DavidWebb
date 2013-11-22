package com.goebl.david;

import java.net.HttpURLConnection;

/**
 * Holds data about the response message returning from HTTP request.
 *
 * @author hgoebl
 */
public class Response<T> {
    final Request request;

    int statusCode;
    String responseMessage;
    T body;
    HttpURLConnection connection;

    Response(Request request) {
        this.request = request;
    }

    void setBody(Object body) {
        this.body = (T) body;
    }

    public Request getRequest() {
        return request;
    }

    public T getBody() {
        return body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusLine() {
        return connection.getHeaderField(0);
    }

    public boolean isSuccess() {
        return (statusCode / 100) == 2; // 200, 201, 204, ...
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public String getContentType() {
        return connection.getContentType();
    }

    public long getDate() {
        return connection.getDate();
    }

    public long getExpiration() {
        return connection.getExpiration();
    }

    public long getLastModified() {
        return connection.getLastModified();
    }

    public String getHeaderField (String key) {
        return connection.getHeaderField(key);
    }

    public long getHeaderFieldDate (String field, long defaultValue) {
        return connection.getHeaderFieldDate(field, defaultValue);
    }

    public int getHeaderFieldInt(String field, int defaultValue) {
        return connection.getHeaderFieldInt(field, defaultValue);
    }

    public HttpURLConnection getConnection() {
        return connection;
    }

    public void ensureSuccess() {
        if (!isSuccess()) {
            throw new WebbException("Request failed: " + statusCode + " " + responseMessage);
        }
    }
}
