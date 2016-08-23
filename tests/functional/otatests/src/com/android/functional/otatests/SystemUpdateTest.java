package com.android.functional.otatests;

import org.junit.Test;

/**
 * A basic test case to assert that the system was updated to the expected version.
 */
public class SystemUpdateTest extends VersionCheckingTest {

    public SystemUpdateTest(String testPath) {
        super(testPath);
    }

    @Test
    public void testIsUpdated() throws Exception {
        assertUpdated();
    }
}
