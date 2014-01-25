package com.goebl.david;

import junit.framework.TestCase;
import org.json.JSONObject;

public class TestWebbRequestBin { /* extends TestCase {

    // @Ignore // only for manual, sporadic tests (uri is not stable)
    public void testStackOverflow20543115() throws Exception {
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
    */
}

