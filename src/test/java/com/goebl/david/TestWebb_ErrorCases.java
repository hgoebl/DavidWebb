package com.goebl.david;

import org.json.JSONObject;

public class TestWebb_ErrorCases extends AbstractTestWebb {

    public void testGetWithBody() throws Exception {
        try {
            webb.get("/does_not_exist").body("some text").asVoid();
            fail();
        } catch (IllegalStateException expected) {
            // body with get is not allowed
        }
    }

    public void testDeleteWithBody() throws Exception {
        try {
            webb.delete("/does_not_exist").body("some text").asVoid();
            fail();
        } catch (IllegalStateException expected) {
            // body with get is not allowed
        }
    }

    public void testUriNull() throws Exception {
        try {
            webb.get(null).asVoid();
            fail();
        } catch (IllegalArgumentException expected) {
            // body with get is not allowed
        }
    }

    public void testError404NoContent() throws Exception {
        Response<String> response = webb
                .get("/error/404")
                .asString();

        assertFalse(response.isSuccess());
        assertEquals(404, response.getStatusCode());
        assertEquals("Not Found", response.getResponseMessage());
        assertNull(response.getBody());
        assertEquals(String.class, response.getErrorBody().getClass());
    }

    public void testError400NoContent() throws Exception {
        Response<String> response = webb
                .get("/error/400/no-content")
                .asString();

        assertFalse(response.isSuccess());
        assertEquals(400, response.getStatusCode());
        assertNull(response.getBody());
        assertEquals("Bad Request", response.getErrorBody());
    }

    public void testError400WithContent() throws Exception {
        Response<JSONObject> response = webb
                .get("/error/400/with-content")
                .asJsonObject();

        assertFalse(response.isSuccess());
        assertEquals(400, response.getStatusCode());
        assertNull(response.getBody());
        assertNotNull(response.getErrorBody());
        assertEquals(JSONObject.class, response.getErrorBody().getClass());
        JSONObject errorObject = (JSONObject) response.getErrorBody();
        assertNotNull(errorObject.optString("msg"));
    }

    public void testPostError500WithContent_JSON() throws Exception {
        Response<JSONObject> response = webb
                .post("/error/500/with-content")
                .body("This is some content")
                .asJsonObject();

        assertFalse(response.isSuccess());
        assertEquals(500, response.getStatusCode());
        assertNull(response.getBody());
        assertEquals(JSONObject.class, response.getErrorBody().getClass());
        JSONObject errorObject = (JSONObject) response.getErrorBody();
        assertEquals("an error has occurred", errorObject.optString("msg"));
    }

    public void testPostError500WithContent_String() throws Exception {
        Response<String> response = webb
                .post("/error/500/with-content")
                .body("This is some content")
                .asString();

        assertFalse(response.isSuccess());
        assertEquals(500, response.getStatusCode());
        assertNull(response.getBody());
        assertEquals(String.class, response.getErrorBody().getClass());
        String error = (String) response.getErrorBody();
        assertTrue(error.contains("an error has occurred"));
    }

    public void testEnsureSuccessFailedWithContent() throws Exception {
        try {
            JSONObject result = webb
                    .get("/error/500/with-content")
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
            fail("should throw exception");
        } catch (WebbException expected) {
            Response response = expected.getResponse();
            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertEquals(500, response.getStatusCode());
            assertNull(response.getBody());
            assertNotNull(response.getErrorBody());
            assertEquals(JSONObject.class, response.getErrorBody().getClass());
            JSONObject errorObject = (JSONObject) response.getErrorBody();
            assertEquals("an error has occurred", errorObject.optString("msg"));
        }
    }

    public void testEnsureSuccessFailedNoContent() throws Exception {
        try {
            JSONObject result = webb
                    .get("/error/500/no-content")
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
            fail("should throw exception");
        } catch (WebbException expected) {
            Response response = expected.getResponse();
            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertEquals(500, response.getStatusCode());
            assertNull(response.getBody());
            assertNotNull(response.getErrorBody());
            assertEquals("Internal Server Error", response.getErrorBody());
        }
    }

}
