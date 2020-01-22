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

public interface IAutoGenericAppHelper extends IAppHelper, Scrollable {
    /**
     * Set the package to open. The application will be opened using the info activity or launcher
     * activity of the package that has been injected here. Either setPackage or setLaunchActivity
     * needs to be invoked after creating an instance of this class.
     */
    void setPackage(String pkg);

    /**
     * Set the launch activity. The application will be opened directly using the provided activity.
     * Either setPackage or setLaunchActivity needs to be invoked after creating an instance of this
     * class.
     */
    void setLaunchActivity(String pkg);
}
