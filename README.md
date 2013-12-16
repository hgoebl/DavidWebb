# DavidWebb

Lightweight Java HTTP-Client for calling JSON REST-Services (especially made for Android).

---

```text
           _              _             _                       _
 _ _ ___ _| |___ ___    _| |___ _ _ ___| |___ ___ _____ ___ ___| |_
| | |   | . | -_|  _|  | . | -_| | | -_| | . | . |     | -_|   |  _|
|___|_|_|___|___|_|    |___|___|\_/|___|_|___|  _|_|_|_|___|_|_|_|
                                             |_|
```

---

## Problem

If you have to call a RESTful Webservice from Java, especially if you are on Android, you have some options:

 * Use `DefaultHttpClient` or `AndroidHttpClient`. It is already deployed on Android and it's easy to use.
   But wait a moment -
   [Google doesn't recommend using it](http://android-developers.blogspot.de/2011/09/androids-http-clients.html),
   only on very old Android versions.
 * Use `HttpUrlConnection`. This is what Google recommends for newer Android versions (>= Gingerbread).
   It is part of JDK, but it's cumbersome to use (if not to say a nightmare).
 * Add `Unirest`, `Restlet` or some other "all-you-can-eat", universal, multi-part, File-upload and all-cases
   supporting library which adds some hundred KB of jars to your APK.

## Solved

**DavidWebb** is a small wrapper around
[HttpUrlConnection](http://docs.oracle.com/javase/7/docs/api/java/net/HttpURLConnection.html).
It supports most HTTP communication cases when you talk to REST services and your data is JSON. It is very
lightweight (~14 KB jar) and super-easy to use.

## Features ###

  * Supports GET, POST, PUT, DELETE
  * add HTTP headers (per request, per client or globally)
  * convert params to `www-form-urlencoded` body or URI search params
  * fluent API
  * org.json support (JSONObject, JSONArray) as payload in both directions
  * wraps all Exceptions in a WebbException (a RuntimeException)
  * automatically sets many boiler-plate HTTP headers (like 'Accept', 'Content-Type', 'Content-Length')
  * supports HTTPS and enables relaxing SSL-handshake (self-signed certificates, hostname verification)
  * pass-through to "real" connection for special cases (e.g. get client certificate)

# Usage Examples

This is some code from a SyncAdapter of an Android App:

```java
// create the client (one-time, can be used from different threads)
Webb webb = Webb.create();
webb.setBaseUri(SyncPreferences.REST_ENDPOINT);
webb.setDefaultHeader(Webb.HDR_USER_AGENT, Const.UA);

// later we authenticate
Response<JSONObject> response = webb
        .post("/session")
        .param("authentication", createAuthentication(syncPreferences))
        .param("deviceId", syncPrefs.getDeviceId())
        .ensureSuccess()
        .asJsonObject();

JSONObject apiResult = response.getBody();

AccessToken accessToken = new AccessToken();
accessToken.token = apiResult.getString("token");
accessToken.validUntil = apiResult.getLong("validUntil");

webb.setDefaultHeader(HDR_ACCESS_TOKEN, accessToken.token);

JSONObject sync = webb.post("/startSync")
        .param("lastSync", syncPrefs.getLastSync())
        .ensureSuccess()
        .asJsonObject()
        .getBody();

// ... etc. etc.

// releaseAccessToken
webb.delete("/session").asVoid();
accessToken = null;
```

Using Google Directions API:

```java
Webb webb = Webb.create();
JSONObject result = webb
        .get("http://maps.googleapis.com/maps/api/directions/json")
        .param("origin", new GeoPoint(47.8227, 12.096933))
        .param("destination", new GeoPoint(47.8633, 12.215533))
        .param("mode", "walking")
        .param("sensor", "true")
        .ensureSuccess()
        .asJsonObject()
        .getBody();

JSONArray routes = result.getJSONArray("routes");
```

If you want to see more examples, just have a look at the JUnit TestCase (src/test/java/...).

## Special Case Android < Froyo

You should add this if you build for legacy Android devices:

```java
if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
    System.setProperty("http.keepAlive", "false");
}
```

# Background

## Not for you?

If **DavidWebb** is too lightweight and you're missing features, you can have a look at:

  * [RESTDroid](https://github.com/PCreations/RESTDroid)
  * [RoboSpice](https://github.com/octo-online/robospice)
  * [android-rest-client](https://github.com/darko1002001/android-rest-client)
  * [unirest](http://unirest.io/)
  * [Restlet Framework](http://restlet.org/)
  * [Volley](https://android.googlesource.com/platform/frameworks/volley) and
    [Volley Example](http://www.technotalkative.com/android-volley-library-example/)
  * [DataDroid](http://datadroid.foxykeep.com/) - an Android library for Data Management
  * [More Alternatives (on RoboSpice)](https://github.com/octo-online/robospice#alternatives-to-robospice-)
  * (tell me if I missed your award-winning REST-client library!)

## The Name!?

David **Webb** is the real name of **Jason** Bourne.

So **JSON** and **Web**, did you get it? OK, might be silly, but Bourne 1-3 are my favorite films and so at
least I can remember the name.

From Wikipedia:

> [Jason Bourne](http://en.wikipedia.org/wiki/Jason_Bourne) is a fictional character and the protagonist
of a series of novels by Robert Ludlum and subsequent film adaptations

# License

MIT License, see LICENSE file

# Testing

The Unit-Tests do not mock any network-libraries, but depend on a small Express-application running.

## Setup

```
cd src/test/api-test-server
npm install
node .
```

If you don't want to do this, just skip the tests in Maven build `-DskipTests`

And if you don't want to build the library, just take the jar from the `dist` folder.

# TODO

## Bugfix
  * overwrite / effective headers (merge headers)

## Features
  * unprefixJson <http://haacked.com/archive/2008/11/20/anatomy-of-a-subtle-json-vulnerability.aspx>
  * decorator beforeSend - provide hooks to manipulate request before send
  * decorator afterReceive - provide hooks to manipulate raw response after receiving it
  * basicAuth(name, password) - set header and encode base64
  * rawFile, textFile(File, encoding) - process body(File)

## Documentation / Distribution
  * Write JavaDoc
  * Generate JavaDoc and publish on gh-pages
  * Extend Tests, test against httpbin.org
  * Upload maven artifact to central repository (OMG how easy this would be with node.js and npm!)
