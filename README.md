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
   But wait a moment - Google doesn't recommend to use it, only on very old Android versions.
 * Use `HttpUrlConnection`. This is what Google recommends for newer Android versions (>= Gingerbread).
   It is part of JDK, but it's cumbersome to use (if not to say a nightmare).
 * Add `Unirest`, `Restlet` or some other "all-you-can-eat", universal, multi-part, File-upload and all-cases
   supporting library which adds some hundred KB of jars to your APK.

## Solved

**DavidWebb** is a small wrapper around `HttpUrlConnection`. It supports most HTTP communication cases when
you talk to REST services and your data is JSON. It is very lightweight (~12 KB jar) and super-easy to use.

### Features ###

  * Supports GET, POST, PUT, DELETE
  * add HTTP headers (per request, per client or globally)
  * convert params to `www-form-urlencoded` body or URI search params
  * fluent API
  * org.json support (JSONObject, JSONArray) as payload in both directions
  * wraps all Exceptions in a WebbException (a RuntimeException)
  * automatically sets many boiler-plate HTTP headers (like 'Accept', 'Content-Type', 'Content-Length')

## Not for you?

If **DavidWebb** is too lightweight and you're missing features, you can have a look at:

  * [RESTDroid](https://github.com/PCreations/RESTDroid)
  * [RoboSpice](https://github.com/octo-online/robospice)
  * [android-rest-client](https://github.com/darko1002001/android-rest-client)
  * [unirest](http://unirest.io/)
  * (tell me if I missed your award-winning REST-client library!)

## The Name!?

David **Webb** is the real name of **Jason** Bourne.

So **JSON** and **Web**, did you get it? OK, might be silly, but Bourne 1-3 are my favorite films and so at
least I can remember the name.

From Wikipedia:

> [Jason Bourne](http://en.wikipedia.org/wiki/Jason_Bourne) is a fictional character and the protagonist
of a series of novels by Robert Ludlum and subsequent film adaptations

# Usage

```java
    // create the client (one-time, can be used from different threads)
    Webb webb = Webb.create();
    webb.setBaseUri(SyncPreferences.REST_ENDPOINT);
    webb.setDefaultHeader(Webb.HDR_USER_AGENT, Const.UA);

    // later we authenticate
    Response<JSONObject> response = webb
            .post("/session")
            .param("authentication", createAuthentication(syncPreferences))
            .param("deviceId", syncPreferences.getDeviceId())
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

**TODO** add more examples - for now just have a look in the JUnit TestCase (src/test/java/...)

## Special Case Android < Froyo

You should add this if you build for legacy Android devices:

    if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
        System.setProperty("http.keepAlive", "false");
    }

# License

MIT License, see LICENSE file

# Testing

The Unit-Test do not mock any network-libraries, but depend on a small Express-application running.

## Setup

```
cd src/test/api-test-server
npm install
node .
```

If you don't want to do this, just skip the tests in Maven build `-DskipTests`

# TODO

  * Support HTTPS (coming soon!)
  * Write example code for README.md
  * Write JavaDoc
  * Generate JavaDoc and publish on gh-pages
  * Extend Tests
  * Upload maven artifact to central repository (OMG how easy this would be with node.js and npm!)
