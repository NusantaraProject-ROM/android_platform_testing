/*
 * Copyright (C) 2019 The Android Open Source Project
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

/**
 * Helper class for functional tests of App Info settings
 */

public interface IAutoAppInfoSettingsHelper extends IAppHelper {
    /**
     * Setup expectation: App info setting is open
     *
     * This method is to open an application in App info setting
     *
     * @param application - name of the application
     */
    void selectApp(String application);

    /**
     * Setup expectation: An application in App info setting is open
     *
     * This method is to enable/disable an application
     *
     * @param enable - true: to enable, false: to disable
     */
    void enableDisableApplication(boolean enable);

    /**
     * Setup expectation: An application in App info setting is open
     *
     * This method is to check whether an application is running in background from UI
     */
    boolean isCurrentApplicationRunning();

    /**
     * Setup expectation: An application in App info setting is open
     *
     * This method is to force stop the application
     */
    void forceStop();

    /**
     * Setup expectation: App info setting is open
     *
     * This method is to add a permission to an application
     *
     * @param permission - name of the permission
     *
     * @param enable - true: to enable, false: to disable
     */
    void setAppPermission(String permission, boolean enable);

    /**
     * Setup expectation: An application in Apps & notifications setting is open
     *
     * Get the current enabled permission summary in String format for an application
     */
    String getCurrentPermissions();
}
