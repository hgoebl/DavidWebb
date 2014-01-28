package com.goebl.david;

import junit.framework.TestCase;

public abstract class AbstractTestWebb extends TestCase {
    static final String SIMPLE_ASCII = "Hello/World & Co.?";
    static final String COMPLEX_UTF8 = "München 1 Maß 10 €";
    static final String HTTP_MESSAGE_OK = "OK";
    static final String HOST_IP = "192.168.0.147"; // set you're own address (`hostname -I`)

    protected Webb webb;

    protected static boolean isAndroid() {
        return System.getProperty("ANDROID") != null;
    }

    protected static boolean isEmulator() {
        return System.getProperty("EMULATOR") != null;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        String host = isAndroid()
                ? (isEmulator() ? "10.0.2.2" : HOST_IP)
                : "localhost";

        Webb.setGlobalHeader(Webb.HDR_USER_AGENT, Webb.DEFAULT_USER_AGENT);
        webb = Webb.create();
        webb.setBaseUri("http://" + host + ":3003");
    }

    protected void assertArrayEquals(byte[] expected, byte[] bytes) {
        assertEquals("array length mismatch", expected.length, bytes.length);
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != bytes[i]) {
                fail(String.format("array different at index %d expected: %d, is: %d", i, expected[i], bytes[i]));
            }
        }
    }

}
