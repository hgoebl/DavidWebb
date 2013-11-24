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

    /**
     * The first line returned by the web-server, like "HTTP/1.1 200 OK".
     * @return first header
     */
    public String getStatusLine() {
        return connection.getHeaderField(0);
    }

    /**
     * Was the request successful (returning a 2xx status code)?
     * @return <code>true</code> when status code is between 200 and 299, else <code>false</code>
     */
    public boolean isSuccess() {
        return (statusCode / 100) == 2; // 200, 201, 204, ...
    }

    /**
     * Returns the text explaining the status code.
     * @return e.g. "Moved Permanently", "Created", ...
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * Returns the MIME-type of the response body.
     * @return e.g. "application/json", "text/plain", ...
     */
    public String getContentType() {
        return connection.getContentType();
    }

    /**
     * Returns the date when the request was created (server-time).
     * @return the parsed "Date" header or <code>null</code> if this header was not set.
     */
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
