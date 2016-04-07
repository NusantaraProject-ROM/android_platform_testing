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

package com.google.android.wearable.support;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.wearable.support.R;

/**
 * Receiver to generate notification cards.
 */
public class CustomPostNotificationReceiver extends BroadcastReceiver {
    public static final String CONTENT_KEY = "contentTitle";
    public static final String CONTENT_APPEND_KEY = "contentAppend";
    private static final int NOTIFICATION_CARDS_COUNT = 10;

    public CustomPostNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String contentTitle = intent.getStringExtra(CONTENT_KEY);
        //When a broadcast action is called via adb contentTitle will be null,so set the value here.
        if (contentTitle == null || contentTitle.isEmpty()) {
            contentTitle = context.getResources().getString(R.string.title);
        }
        for (int i = 1; i <= NOTIFICATION_CARDS_COUNT; i++) {
            Intent displayIntent = new Intent(context, CustomNotificationDisplayActivity.class)
                    .putExtra(CONTENT_APPEND_KEY, " from iteration:" + i); // Append this to
                                                                           // notification content.
            Notification notification = new Notification.Builder(context)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(contentTitle + "-" + i) // Notification title
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .extend(new Notification.WearableExtender()
                            .setDisplayIntent(PendingIntent.getActivity(context, i, displayIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT)))
                    .build();

            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(i,
                    notification);
        }

        Toast.makeText(context, context.getString(R.string.notification_posted), Toast.LENGTH_SHORT)
                .show(); // Show a toast once notifications cards are posted.
    }
}
