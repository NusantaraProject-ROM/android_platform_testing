package com.android.functional.otatests;

import android.test.InstrumentationTestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class VersionCheckingTest extends InstrumentationTestCase {

    protected static final String OLD_VERSION = "/sdcard/otatest/version.old";
    protected static final String NEW_VERSION = "/sdcard/otatest/version.new";
    protected static final String KEY_BUILD_ID = "ro.build.version.incremental";
    protected static final String KEY_BOOTLOADER = "ro.bootloader";
    protected static final String KEY_BASEBAND = "ro.build.expect.baseband";

    protected VersionInfo mOldVersion;
    protected VersionInfo mNewVersion;

    @Override
    public void setUp() throws Exception {
        try {
            mOldVersion = VersionInfo.parseFromFile(OLD_VERSION);
            mNewVersion = VersionInfo.parseFromFile(NEW_VERSION);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Couldn't find version file; was this test run with VersionCachePreparer?", e);
        }
    }

    protected void assertNotUpdated() throws IOException {
        assertEquals(mOldVersion.getBuildId(), getProp(KEY_BUILD_ID));
        assertEquals(mOldVersion.getBasebandVersion(), getProp(KEY_BASEBAND));
        assertEquals(mOldVersion.getBootloaderVersion(), getProp(KEY_BOOTLOADER));
    }

    protected void assertUpdated() throws IOException {
        assertEquals(mNewVersion.getBuildId(), getProp(KEY_BUILD_ID));
        assertEquals(mNewVersion.getBasebandVersion(), getProp(KEY_BASEBAND));
        assertEquals(mNewVersion.getBootloaderVersion(), getProp(KEY_BOOTLOADER));
    }

    private String getProp(String key) throws IOException {
        Process p = Runtime.getRuntime().exec("getprop " + key);
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String ret = r.readLine().trim();
        r.close();
        return ret;
    }
}
