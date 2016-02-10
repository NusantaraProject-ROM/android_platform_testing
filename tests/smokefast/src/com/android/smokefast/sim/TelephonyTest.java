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

package com.android.smokefast.sim;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.telecom.TelecomManager;
import android.telephony.SmsManager;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Basic tests for phone and SMS functionality.
 */
public class TelephonyTest extends InstrumentationTestCase {

    private static final String DIALER_PACKAGE = "com.android.dialer";
    private static final String SMS_SENT_INTENT = "android.test.smokefast.SMS_SENT";
    private static final String TEL_NO = "12345";
    private static final int WAIT_DELAY = 5000;

    private BroadcastReceiver mReceiver;
    private HandlerThread mBroadcastThread;
    private volatile boolean mMessageSuccess = false;
    private Object mSmsBroadcastLock = new Object();
    private Context mContext;
    private UiDevice mDevice;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getContext();
        mDevice = UiDevice.getInstance(getInstrumentation());
        mDevice.freezeRotation();

        IntentFilter filter = new IntentFilter();
        filter.addAction(SMS_SENT_INTENT);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (SMS_SENT_INTENT.equals(intent.getAction())) {
                    mMessageSuccess = Activity.RESULT_OK == getResultCode();
                    synchronized(mSmsBroadcastLock) {
                        mSmsBroadcastLock.notifyAll();
                    }
                }
            }
        };
        // Register mReceiver on a different thread, so the main thread can block while we wait
        // for the SMS to finish sending
        mBroadcastThread = new HandlerThread(getClass().getName() + "-listener");
        mBroadcastThread.start();
        Handler handler = new Handler(mBroadcastThread.getLooper());
        getInstrumentation().getContext().registerReceiver(mReceiver, filter, null, handler);
    }

    @Override
    public void tearDown() throws Exception {
        mDevice.unfreezeRotation();
        getInstrumentation().getContext().unregisterReceiver(mReceiver);
        mBroadcastThread.quit();
        super.tearDown();
    }

    /**
     * Test that the device can make a phone call.
     */
    @LargeTest
    public void testMakeCall() {
        Uri uri = Uri.fromParts("tel", TEL_NO, null);
        Bundle extras = new Bundle();
        TelecomManager telecomManager = (TelecomManager) mContext.getSystemService(Context.TELECOM_SERVICE);
        telecomManager.placeCall(uri, extras);
        SystemClock.sleep(WAIT_DELAY);
        if (telecomManager.isInCall()) {
            UiObject2 endButton = mDevice.wait(Until.findObject(By.res(DIALER_PACKAGE,
                "floating_end_call_action_button")), WAIT_DELAY);
            assertNotNull("Call end button can't be null", endButton);
            endButton.click();
        } else {
            fail("Phone call wasn't successful");
        }
    }

    /**
     * Test that the device can send an SMS.
     * @throws InterruptedException
     */
    @LargeTest
    public void testSendSms() throws InterruptedException {
        SmsManager sms = SmsManager.getDefault();
        Intent baseIntent = new Intent(SMS_SENT_INTENT);
        PendingIntent pi = PendingIntent.getBroadcast(getInstrumentation().getContext(),
                100, baseIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        sms.sendTextMessage(TEL_NO, null, "test", pi, null);
        // Confirm SendSMS appears only for first time message sent
        UiObject2 confirmSendSMS = mDevice.wait(Until.findObject(
                By.text(Pattern.compile("SEND", Pattern.CASE_INSENSITIVE))), WAIT_DELAY);
        if (confirmSendSMS != null) {
            confirmSendSMS.click();
        }

        synchronized (mSmsBroadcastLock) {
            mSmsBroadcastLock.wait(WAIT_DELAY);
        }
        assertTrue("Text message could not be sent", mMessageSuccess);
    }
}
