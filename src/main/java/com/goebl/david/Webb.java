package com.goebl.david;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lightweight Java HTTP-Client for calling JSON REST-Services (especially for Android).
 *
 * @author hgoebl
 */
public class Webb {
    public static final String DEFAULT_USER_AGENT = Const.DEFAULT_USER_AGENT;
    public static final String APP_FORM = Const.APP_FORM;
    public static final String APP_JSON = Const.APP_JSON;
    public static final String APP_BINARY = Const.APP_BINARY;
    public static final String TEXT_PLAIN = Const.TEXT_PLAIN;
    public static final String HDR_CONTENT_TYPE = Const.HDR_CONTENT_TYPE;
    public static final String HDR_ACCEPT = Const.HDR_ACCEPT;
    public static final String HDR_USER_AGENT = Const.HDR_USER_AGENT;

    static final Map<String, Object> globalHeaders = new LinkedHashMap<String, Object>();
    static String globalBaseUri;
    static String globalEncoding = Const.UTF8;

    static int connectTimeout = 10000;
    static int readTimeout = 60000;
    static int jsonIndentFactor = 0;

    boolean followRedirects = false;
    String baseUri;
    Map<String, Object> defaultHeaders;
    SSLSocketFactory sslSocketFactory;
    HostnameVerifier hostnameVerifier;

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

    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
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
        HttpURLConnection.setFollowRedirects(followRedirects);

        try {
            String uri = request.uri;
            if (request.method == Request.Method.GET &&
                    !uri.contains("?") &&
                    request.params != null &&
                    !request.params.isEmpty()) {
                uri += "?" + WebbUtils.queryString(request.params);
            }
            URL apiUrl = new URL(uri);
            connection = (HttpURLConnection) apiUrl.openConnection();

            prepareSslConnection(connection);
            connection.setRequestMethod(request.method.name());
            connection.setInstanceFollowRedirects(followRedirects);
            connection.setUseCaches(request.useCaches);
            connection.setConnectTimeout(request.connectTimeout != null ? request.connectTimeout : connectTimeout);
            connection.setReadTimeout(request.readTimeout != null ? request.readTimeout : readTimeout);
            if (request.ifModifiedSince != null) {
                connection.setIfModifiedSince(request.ifModifiedSince);
            }

            WebbUtils.addRequestProperties(connection, mergeHeaders(request.headers));
            if (clazz == JSONObject.class || clazz == JSONArray.class) {
                WebbUtils.ensureRequestProperty(connection, HDR_ACCEPT, APP_JSON);
            }

            byte[] requestBody = null;
            if (request.method != Request.Method.GET && request.method != Request.Method.DELETE) {
                requestBody = WebbUtils.getPayloadAsBytesAndSetContentType(connection, request, jsonIndentFactor);
                if (requestBody != null) {
                    connection.setDoOutput(true);
                }
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
            byte[] responseBody = WebbUtils.readBytes(is);

            response.connection = connection;
            response.statusCode = connection.getResponseCode();
            response.responseMessage = connection.getResponseMessage();

            if (request.ensureSuccess) {
                response.ensureSuccess();
            }

            WebbUtils.parseResponseBody(clazz, response, responseBody);

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

    private void prepareSslConnection(HttpURLConnection connection) {
        if ((hostnameVerifier != null || sslSocketFactory != null) && connection instanceof HttpsURLConnection) {
            HttpsURLConnection sslConnection = (HttpsURLConnection) connection;
            if (hostnameVerifier != null) {
                sslConnection.setHostnameVerifier(hostnameVerifier);
            }
            if (sslSocketFactory != null) {
                sslConnection.setSSLSocketFactory(sslSocketFactory);
            }
        }
    }

    Map<String, Object> mergeHeaders(Map<String, Object> requestHeaders) {
        Map<String, Object> headers = null;
        if (!globalHeaders.isEmpty()) {
            headers = new LinkedHashMap<String, Object>();
            headers.putAll(globalHeaders);
        }
        if (defaultHeaders != null) {
            if (headers == null) {
                headers = new LinkedHashMap<String, Object>();
            }
            headers.putAll(defaultHeaders);
        }
        if (requestHeaders != null) {
            if (headers == null) {
                headers = requestHeaders;
            } else {
                headers.putAll(requestHeaders);
            }
        }
        return headers;
    }

}
