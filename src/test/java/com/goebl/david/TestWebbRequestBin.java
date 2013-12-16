package com.goebl.david;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class TestWebbRequestBin {

    @Ignore // only for manual, sporadic tests (uri is not stable)
    @Test public void stackOverflow20543115() throws Exception {
        Webb webb = Webb.create();
        webb.setBaseUri("http://requestb.in");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("string", "string");
        String json = jsonObject.toString(); // {"string":"string"}
        // WRONG json = "{'string':'string'}";

        Response<String> response = webb
                .post("/1g7afwn1")
                .header("Accept", "application/json")
                .header("Content-type", "application/json")
                .header("hmac", "some-hmac-just-a-test")
                .body(json)
                .asString();

        assertEquals(200, response.getStatusCode());
        assertTrue(response.isSuccess());

        String body = response.getBody();
        assertEquals("ok\n", body);
    }

}

