package com.goebl.david;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight Java HTTP-Client for calling JSON REST-Services (especially for Android).
 *
 * @author hgoebl
 */
public class Webb {
    public static final String DEFAULT_USER_AGENT = "com.goebl.david.Webb/1.0";
    public static final String APP_FORM = "application/x-www-form-urlencoded";
    public static final String APP_JSON = "application/json";
    public static final String APP_BINARY = "application/octet-stream";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String HDR_CONTENT_TYPE = "Content-Type";
    public static final String HDR_ACCEPT = "Accept";
    public static final String HDR_USER_AGENT = "User-Agent";

    static final Class BYTE_ARRAY_CLASS = (new byte[0]).getClass();
    static final String UTF8 = "utf-8";

    static final Map<String, Object> globalHeaders = new HashMap<String, Object>();
    static String globalBaseUri;
    static String globalEncoding = UTF8;

    static int connectTimeout = 10000;
    static int readTimeout = 60000;
    static int jsonIndentFactor = 0;

    boolean followRedirects = false;
    String baseUri;
    Map<String, Object> defaultHeaders;

    public static Webb create() {
        return new Webb();
    }

    public static void setGlobalHeader(String name, Object value) {
        globalHeaders.put(name, value);
    }

    public static void setGlobalBaseUri(String globalBaseUri) {
        Webb.globalBaseUri = globalBaseUri;
    }

    public static void setGlobalEncoding(String encoding) {
        Webb.globalEncoding = encoding;
    }

    public static void setJsonIndentFactor(int indentFactor) {
        Webb.jsonIndentFactor = indentFactor;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public void setDefaultHeader(String name, Object value) {
        if (defaultHeaders == null) {
            defaultHeaders = new HashMap<String, Object>();
        }
        defaultHeaders.put(name, value);
    }

    public Request get(String pathOrUri) {
        return new Request(this, Request.Method.GET, buildPath(pathOrUri));
    }

    public Request post(String pathOrUri) {
        return new Request(this, Request.Method.POST, buildPath(pathOrUri));
    }

    public Request put(String pathOrUri) {
        return new Request(this, Request.Method.PUT, buildPath(pathOrUri));
    }

    public Request delete(String pathOrUri) {
        return new Request(this, Request.Method.DELETE, buildPath(pathOrUri));
    }

    private String buildPath(String pathOrUri) {
        String myBaseUri = baseUri != null ? baseUri : globalBaseUri;
        return myBaseUri == null ? pathOrUri : myBaseUri + pathOrUri;
    }

    <T> Response<T> execute(Request request, Class<T> clazz) {
        Response<T> response = new Response<T>(request);

        InputStream is = null;
        OutputStream os = null;
        HttpURLConnection connection = null;
        HttpURLConnection.setFollowRedirects(false);

        try {
            String uri = request.uri;
            if (request.method == Request.Method.GET &&
                    !uri.contains("?") &&
                    request.params != null &&
                    !request.params.isEmpty()) {
                uri += "?" + Utils.queryString(request.params);
            }
            URL apiUrl = new URL(uri);
            connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod(request.method.name());
            if (request.method != Request.Method.DELETE) {
                connection.setDoOutput(true);
            }
            connection.setInstanceFollowRedirects(followRedirects);
            connection.setUseCaches(request.useCaches);
            connection.setConnectTimeout(request.connectTimeout != null ? request.connectTimeout : connectTimeout);
            connection.setReadTimeout(request.readTimeout != null ? request.readTimeout : readTimeout);
            if (request.ifModifiedSince != null) {
                connection.setIfModifiedSince(request.ifModifiedSince);
            }

            if (request.method != Request.Method.GET && request.payload != null) {
                connection.setDoInput(true);
            }

            // TODO create and obey an overwrite rule for headers
            Utils.addRequestProperties(connection, globalHeaders);
            Utils.addRequestProperties(connection, defaultHeaders);
            Utils.addRequestProperties(connection, request.headers);
            if (clazz == JSONObject.class || clazz == JSONArray.class) {
                Utils.ensureRequestProperty(connection, HDR_ACCEPT, APP_JSON);
            }

            byte[] requestBody = null;
            if (request.method != Request.Method.GET) {
                requestBody = getPayloadAsBytesAndSetContentType(connection, request);
            }

            connection.connect();

            // write the body (of course headers are written first by HUC)
            if (requestBody != null) {
                os = connection.getOutputStream();
                os.write(requestBody);
                os.flush();
                os.close();
                os = null;
            }

            // get the response body (if any)
            is = connection.getInputStream();
            byte[] responseBody = Utils.readBytes(is);

            response.connection = connection;
            response.statusCode = connection.getResponseCode();
            response.responseMessage = connection.getResponseMessage();

            if (request.ensureSuccess) {
                response.ensureSuccess();
            }

            parseResponseBody(clazz, response, responseBody);

            return response;

        } catch (WebbException e) {

            throw e;

        } catch (Exception e) {

            throw new WebbException(e);

        } finally {
            if (is != null) {
                try { is.close(); } catch (Exception ignored) {}
            }
            if (os != null) {
                try { os.close(); } catch (Exception ignored) {}
            }
            if (connection != null) {
                try { connection.disconnect(); } catch (Exception ignored) {}
            }
        }
    }

    private <T> void parseResponseBody(Class<T> clazz, Response<T> response, byte[] responseBody)
            throws UnsupportedEncodingException {

        if (responseBody == null || clazz == Void.class) {
            return;
        }

        // we are ignoring headers describing the content type of the response, instead
        // try to force the content based on the type the client is expecting it (clazz)
        if (clazz == String.class) {
            response.setBody(new String(responseBody, UTF8));
        } else if (clazz == BYTE_ARRAY_CLASS) {
            response.setBody(responseBody);
        } else if (clazz == JSONObject.class) {
            response.setBody(Utils.toJsonObject(responseBody));
        } else if (clazz == JSONArray.class) {
            response.setBody(Utils.toJsonArray(responseBody));
        }
    }

    private byte[] getPayloadAsBytesAndSetContentType(HttpURLConnection connection, Request request) throws JSONException, UnsupportedEncodingException {
        byte[] requestBody = null;
        String bodyStr = null;

        if (request.params != null) {
            Utils.ensureRequestProperty(connection, HDR_CONTENT_TYPE, APP_FORM);
            bodyStr = Utils.queryString(request.params);
        } else if (request.payload == null) {
            requestBody = null;
        } else if (request.payload instanceof JSONObject) {
            Utils.ensureRequestProperty(connection, HDR_CONTENT_TYPE, APP_JSON);
            bodyStr = ((JSONObject)request.payload).toString(jsonIndentFactor);
        } else if (request.payload instanceof JSONArray) {
            Utils.ensureRequestProperty(connection, HDR_CONTENT_TYPE, APP_JSON);
            bodyStr = ((JSONArray)request.payload).toString(jsonIndentFactor);
        } else if (request.payload instanceof byte[]) {
            Utils.ensureRequestProperty(connection, HDR_CONTENT_TYPE, APP_BINARY);
            requestBody = (byte[]) request.payload;
        } else {
            Utils.ensureRequestProperty(connection, HDR_CONTENT_TYPE, TEXT_PLAIN);
            bodyStr = request.payload.toString();
        }
        if (bodyStr != null) {
            requestBody = bodyStr.getBytes(UTF8);
        }

        if (requestBody != null) {
            connection.setFixedLengthStreamingMode(requestBody.length);
        }
        return requestBody;
    }

}
