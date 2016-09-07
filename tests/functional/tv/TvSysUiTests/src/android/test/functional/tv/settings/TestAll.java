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
 * limitations under the License
 */

package android.test.functional.tv.settings;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * adb shell am instrument -w -r \
 * -e class android.test.functional.tv.settings.TestAll \
 * android.test.functional.tv.sysui/android.support.test.runner.AndroidJUnitRunner
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
})
public class TestAll {
    // the class remains empty,
    // used only as a holder for the above annotations
}
