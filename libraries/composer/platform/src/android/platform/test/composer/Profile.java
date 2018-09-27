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
package android.platform.test.composer;

import android.host.test.composer.profile.Configuration;
import android.host.test.composer.profile.Configuration.Scenario;
import android.host.test.composer.profile.Configuration.Scheduled;
import android.host.test.composer.profile.Configuration.Scheduled.IfEarly;
import android.host.test.composer.profile.Configuration.Scheduled.IfLate;
import android.host.test.composer.ProfileBase;
import android.os.Bundle;
import android.util.Log;

import com.google.common.io.Files;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.IllegalArgumentException;

/**
 * An extension of {@link android.host.test.composer.ProfileBase} for device-side testing.
 */
public class Profile extends ProfileBase<Bundle> {
    @Override
    protected Configuration getConfigurationArgument(Bundle args) {
        String profilePath = args.getString(PROFILE_OPTION_NAME);
        if (profilePath == null) {
            return null;
        }
        Configuration config;
        File configFile;
        FileInputStream configFileStream;
        try {
            configFile = new File(profilePath);
            configFileStream = new FileInputStream(configFile);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(String.format(
                    "Profile could not be found at %s.", profilePath));
        }
        try {
            config = Configuration.parseFrom(configFileStream);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format(
                    "Profile at %s could not be parsed.", profilePath));
        }
        try {
            configFileStream.close();
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format(
                    "IOException closing proile at %s", profilePath));
        }
        return config;
    }

    @Override
    protected void logInfo(String tag, String content) {
        Log.i(tag, content);
    }
}
