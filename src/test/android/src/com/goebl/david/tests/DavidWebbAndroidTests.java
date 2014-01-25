package com.goebl.david.tests;

import android.test.suitebuilder.TestSuiteBuilder;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Build the test suite.
 *
 * @author hgoebl
 * @since 25.01.14
 */
public class DavidWebbAndroidTests extends TestSuite {
    public static Test suite () {
        return new TestSuiteBuilder(DavidWebbAndroidTests.class).includeAllPackagesUnderHere().build();
    }
}
