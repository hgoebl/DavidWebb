package com.goebl.david;

public class TestWebb_Retry extends AbstractTestWebb {

    public void testRetryCount() throws Exception {
        long start = System.currentTimeMillis();
        String successAnswer = webb
                .get("/error/503/" + System.currentTimeMillis() + "/2")
                .retry(2, true)
                .ensureSuccess()
                .asString()
                .getBody();

        assertEquals("Now it works", successAnswer);
        long duration = System.currentTimeMillis() - start;
        assertTrue("Should last longer than 3 seconds", duration > 3000);
    }

    public void testRetryCountArguments() throws Exception {
        webb.get("/simple.txt")
                .retry(2, false)
                .ensureSuccess()
                .asString()
                .getBody();
        try {
            webb.get("/simple.txt")
                    .retry(3, false)
                    .ensureSuccess()
                    .asString()
                    .getBody();
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // good!
        }
    }

}
