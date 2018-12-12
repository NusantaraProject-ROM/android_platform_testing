/*
 * Copyright (C) 2018 The Android Open Source Project
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

package android.platform.helpers;

/** An App Helper interface for the Notification. */
public interface INotificationHelper extends IAppHelper {
    /**
     * Setup expectations: Notification shade opened.
     *
     * <p>Opens a notification from notification shade.
     *
     * @param index The index of the notification to open.
     */
    default public void openNotificationbyIndex(int index) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /**
     * Setup Expectations: None
     *
     * <p>Posts a number of notifications to the device. Successive calls to this should post
     * new notifications to those previously posted. Note that this may fail if the helper has
     * surpassed the system-defined limit for per-package notifications.
     *
     * @param count The number of notifications to post.
     */
    default public void postNotifications(int count) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /**
     * Setup Expectations: None
     *
     * <p>Cancel any notifications posted by this helper.
     */
    default public void cancelNotifications() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }
}
