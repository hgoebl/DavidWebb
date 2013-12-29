package com.goebl.david;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builder for an HTTP request.
 * <br/>
 * You can some "real-life" usage examples in {@link Webb} or
 * <a href="https://github.com/hgoebl/DavidWebb">github.com/hgoebl/DavidWebb</a>.
 * <br/>
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
    boolean streamPayload;
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

    /**
     * Set (or overwrite) a parameter.
     * <br/>
     * The parameter will be used to create a query string for GET-requests and as the body for POST-requests
     * with MIME-type <code>application/x-www-form-urlencoded</code>,
     * @param name the name of the parameter (it's better to use only contain ASCII characters)
     * @param value the value of the parameter; <code>null</code> will be converted to empty string, for all other
     *              objects to <code>toString()</code> method converts it to String
     * @return <code>this</code> for method chaining (fluent API)
     */
    public Request param(String name, Object value) {
        if (params == null) {
            params = new LinkedHashMap<String, Object>();
        }
        params.put(name, value);
        return this;
    }

    /**
     * Set (or overwrite) a HTTP header value.
     * <br/>
     * Setting a header this way has the highest precedence and overrides a header value set on a {@link Webb}
     * instance ({@link Webb#setDefaultHeader(String, Object)}) or a global header
     * ({@link Webb#setGlobalHeader(String, Object)}).
     * <br/>
     * Using <code>null</code> or empty String is not allowed for name and value.
     *
     * @param name name of the header (HTTP-headers are not case-sensitive, but if you want to override your own
     *             headers, you have to use identical strings for the name. There are some frequently used header
     *             names as constants in {@link Webb}, see HDR_xxx.
     * @param value the value for the header. Following types are supported, all other types use <code>toString</code>
     *              of the given object:
     *              <ul>
     *              <li>{@link java.util.Date} is converted to RFC1123 compliant String</li>
     *              <li>{@link java.util.Calendar} is converted to RFC1123 compliant String</li>
     *              </ul>
     * @return <code>this</code> for method chaining (fluent API)
     */
    public Request header(String name, Object value) {
        if (headers == null) {
            headers = new LinkedHashMap<String, Object>();
        }
        headers.put(name, value);
        return this;
    }

    /**
     * Set the payload for the request.
     * <br/>
     * Using this method together with {@link #param(String, Object)} has the effect of <code>body</code> being
     * ignored without notice. The method can be called more than once: the value will be stored and converted
     * to bytes later.
     * <br/>
     * Following types are supported for the body:
     * <ul>
     *     <li>
     *         <code>null</code> clears the body
     *     </li>
     *     <li>
     *         {@link org.json.JSONObject}, HTTP header 'Content-Type' will be set to JSON, if not set
     *     </li>
     *     <li>
     *         {@link org.json.JSONArray}, HTTP header 'Content-Type' will be set to JSON, if not set
     *     </li>
     *     <li>
     *         {@link java.lang.String}, HTTP header 'Content-Type' will be set to TEXT, if not set;
     *         Text will be converted to UTF-8 bytes.
     *     </li>
     *     <li>
     *         <code>byte[]</code> the easiest way for DavidWebb - it's just passed through.
     *         HTTP header 'Content-Type' will be set to BINARY, if not set.
     *     </li>
     *     <li>
     *         {@link java.io.File}, HTTP header 'Content-Type' will be set to BINARY, if not set;
     *         The file gets streamed to the web-server and 'Content-Length' will be set to the number
     *         of bytes of the file. There is absolutely no conversion done. So if you want to upload
     *         e.g. a text-file and convert it to another encoding than stored on disk, you have to do
     *         it by yourself.
     *
     *         TODO implement and test
     *
     *     </li>
     *     <li>
     *         {@link java.io.InputStream}, HTTP header 'Content-Type' will be set to BINARY, if not set;
     *         Similar to <code>File</code>. Content-Length cannot be set (which has some drawbacks compared
     *         to knowing the size of the body in advance).<br/>
     *         <strong>You have to care for closing the stream!</strong>
     *
     *         TODO implement and test
     *
     *     </li>
     * </ul>
     *
     * @param body the payload
     * @return <code>this</code> for method chaining (fluent API)
     */
    public Request body(Object body) {
        if (method == Method.GET) {
            throw new IllegalStateException("body not allowed for GET requests");
        }
        this.payload = body;
        this.streamPayload = body instanceof File || body instanceof InputStream;
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
