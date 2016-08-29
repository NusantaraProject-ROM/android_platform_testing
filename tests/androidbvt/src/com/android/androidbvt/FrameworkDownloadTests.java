/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.androidbvt;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.ParcelFileDescriptor;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.Until;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import java.io.IOException;
import java.util.HashSet;
import junit.framework.TestCase;

public class FrameworkDownloadTests extends TestCase {
    private static final String TEST_TAG = "AndroidBVT";
    private final String TEST_HOST = "209.119.80.137:10090/";
    private final String PHOTOS_PATH = "new_ui/all_content/photos";
    private final String APPS_PATH = "new_ui/all_content/apps";
    private final String TEST_FILE = "android_apps.jpeg";
    private final String APP_FILE = "Auto1kb.apk";
    private final int TEST_FILE_SIZE = 159709;
    private final int TEST_APK_SIZE = 16384;
    private DownloadManager mDownloadManager = null;
    private WifiManager mWifiManager = null;
    private AndroidBvtHelper mABvtHelper = null;
    private UiDevice mDevice;
    private Context mContext = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.freezeRotation();
        mContext = InstrumentationRegistry.getTargetContext();
        mABvtHelper = AndroidBvtHelper.getInstance(mDevice, mContext,
                InstrumentationRegistry.getInstrumentation().getUiAutomation());
        mDownloadManager = mABvtHelper.getDownloadManager();
        mWifiManager = mABvtHelper.getWifiManager();
    }

    @Override
    public void tearDown() throws Exception {
        mDevice.unfreezeRotation();
        mDevice.pressHome();
        mDevice.waitForIdle();
        super.tearDown();
    }

    /**
     * Following test verifies that download service is running and serves any download request
     * Enqueues a request to download a photo After download completion, compares file size that
     * mentioned in server
     */
    @LargeTest
    public void testPhotoDownloadSucceed() throws InterruptedException, IOException {
        mABvtHelper.ensureWifiEnabled();
        removeAllCurrentDownloads(); // if there are any in progress
        long downloadId = -1;
        try {
            downloadId = downloadItem(PHOTOS_PATH, TEST_FILE, TEST_FILE_SIZE);
        } finally {
            mDownloadManager.remove(downloadId);
        }
    }

    /**
     * Test to verify that user gets security message when it tries to install an apk downloaded
     * from unreliable source
     * @throws InterruptedException
     * @throws IOException
     */
    @LargeTest
    public void testInstallDownloadedAppFromUnreliableSource()
            throws InterruptedException, IOException {
        mABvtHelper.ensureWifiEnabled();
        removeAllCurrentDownloads(); // if there are any in progress
        long dlId = -1;
        try {
            dlId = downloadItem(APPS_PATH, APP_FILE, TEST_APK_SIZE);
            mABvtHelper.launchApp("com.android.documentsui", "Downloads");
            mDevice.wait(Until.findObject(By.text(APP_FILE)), mABvtHelper.LONG_TIMEOUT).click();
            assertTrue("Security message isn't shown on installing an apk downloaded from "
                    + "unreliable source",
                    mDevice.wait(Until.hasObject(By.textStartsWith("For security")),
                            mABvtHelper.LONG_TIMEOUT));
            mDevice.wait(Until.findObject(By.text("CANCEL")), mABvtHelper.LONG_TIMEOUT)
                    .clickAndWait(Until.newWindow(), mABvtHelper.LONG_TIMEOUT);
        } finally {
            mDownloadManager.remove(dlId);
        }
    }

    private long downloadItem(String path, String file, int size)
            throws InterruptedException, IOException {
        Uri downloadUri = Uri.parse(String.format("http://%s/%s/%s", TEST_HOST, path, file));
        Request request = new Request(downloadUri);
        // on enqueuing an item for download, dlMgr returns a unique id for future ref
        long dlId = mDownloadManager.enqueue(request);

        // Register receiver to listen to DownloadComplete Broadcase message
        // Wait for download to finish
        final DownloadCompleteReceiver receiver = new DownloadCompleteReceiver();
        try {
            IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            mContext.registerReceiver(receiver, intentFilter);
            Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            assertTrue("download not finished", receiver.isDownloadCompleted(dlId));
            // Verify Download file size
            ParcelFileDescriptor pfd = null;
            try {
                pfd = mDownloadManager.openDownloadedFile(dlId);
                assertTrue("File size should be same as mentioned in server",
                        pfd.getStatSize() == size);
            } finally {
                if (pfd != null) {
                    pfd.close();
                }
            }
        } finally {
            mContext.unregisterReceiver(receiver);
        }
        return dlId;
    }

    /**
     * Remove all downloads those are in progress now
     */
    private void removeAllCurrentDownloads() {
        DownloadManager downloadManager = (DownloadManager) mContext
                .getSystemService(Context.DOWNLOAD_SERVICE);
        Cursor cursor = downloadManager.query(new Query());
        try {
            if (cursor.moveToFirst()) {
                do {
                    int index = cursor.getColumnIndex(DownloadManager.COLUMN_ID);
                    long downloadId = cursor.getLong(index);
                    downloadManager.remove(downloadId);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * DownloadManager broadcasts 'DownloadManager.ACTION_DOWNLOAD_COMPLETE' once download is
     * complete and copied from cache to Downloads folder Following receiver to intercept download
     * intent to parse out the downloaded id to ensure that the item has been downloaded that was
     * initiated in the test. Please note that when a download action is enqueued, DownloadManager
     * provides a download id
     */
    private class DownloadCompleteReceiver extends BroadcastReceiver {
        private HashSet<Long> mCompleteIds = new HashSet<>();

        public DownloadCompleteReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized (mCompleteIds) {
                mCompleteIds.add(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1));
                Log.i(TEST_TAG, "Request Id = "
                        + intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1));
                mCompleteIds.notifyAll();
            }
        }

        // Tries 5 times/5 secs for download to be completed
        public boolean isDownloadCompleted(long id)
                throws InterruptedException {
            int counter = 10;
            while (--counter > 0) {
                synchronized (mCompleteIds) {
                    mCompleteIds.wait(mABvtHelper.LONG_TIMEOUT);
                    if (mCompleteIds.contains(id)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
