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

import android.content.res.AssetManager;
import android.host.test.composer.profile.Configuration;
import android.host.test.composer.ProfileBase;
import android.os.Bundle;
import android.util.Log;
import androidx.test.InstrumentationRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.lang.IllegalArgumentException;

/**
 * An extension of {@link android.host.test.composer.ProfileBase} for device-side testing.
 */
public class Profile extends ProfileBase<Bundle> {
    /*
     * {@inheritDocs}
     *
     * The configuration should be passed as either the name of a configuration bundled into the APK
     * or a path to the configuration file.
     *
     * TODO(harrytczhang@): Write tests for this logic.
     */

    public Profile(Bundle args) {
        super(args);
    }

    @Override
    protected Configuration getConfigurationArgument(Bundle args) {
        // profileValue is either the name of a profile bundled with an APK or a path to a
        // profile configuration file.
        String profileValue = args.getString(PROFILE_OPTION_NAME, "");
        if (profileValue.isEmpty()) {
            return null;
        }
        // Look inside the APK assets for the profile; if this fails, try
        // using the profile argument as a path to a configuration file.
        InputStream configStream;
        try {
            AssetManager manager = InstrumentationRegistry.getContext().getAssets();
            String profileName = profileValue + PROFILE_EXTENSION;
            configStream = manager.open(profileName);
        } catch (IOException e) {
            // Try using the profile argument it as a path to a configuration file.
            try {
                File configFile = new File(profileValue);
                if (!configFile.exists()) {
                    throw new IllegalArgumentException(String.format(
                            "Profile %s does not exist.", profileValue));
                }
                configStream = new FileInputStream(configFile);
            } catch (IOException f) {
                throw new IllegalArgumentException(String.format(
                        "Profile %s cannot be opened.", profileValue));
            }
        }
        try {
            // Parse the configuration from its input stream and return it.
            return Configuration.parseFrom(configStream);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format(
                    "Cannot parse profile %s.", profileValue));
        } finally {
            try {
                configStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void logInfo(String tag, String content) {
        Log.i(tag, content);
    }
}
