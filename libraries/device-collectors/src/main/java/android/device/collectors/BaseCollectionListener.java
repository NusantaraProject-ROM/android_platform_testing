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
package android.device.collectors;

import android.os.Bundle;

import androidx.annotation.VisibleForTesting;

import com.android.helpers.ICollectorHelper;

import org.junit.runner.Description;

import java.util.Map;

/**
 * A {@link BaseCollectionListener} that captures metrics collected during the testing.
 *
 * Do NOT throw exception anywhere in this class. We don't want to halt the test when metrics
 * collection fails.
 */
public class BaseCollectionListener<T> extends BaseMetricListener {

    private ICollectorHelper mHelper;

    public BaseCollectionListener() {
        super();
    }

    @VisibleForTesting
    public BaseCollectionListener(Bundle args) {
        super(args);
    }

    @Override
    public void onTestStart(DataRecord testData, Description description) {
        // NO-OP in the failure case for now and proceed with the testing.
        mHelper.startCollecting();
    }

    @Override
    public void onTestEnd(DataRecord testData, Description description) {
        Map<String, T> metrics = mHelper.getMetrics();
        for (Map.Entry<String, T> entry : metrics.entrySet()) {
            testData.addStringMetric(entry.getKey(), entry.getValue().toString());
        }
        mHelper.stopCollecting();
    }

    protected void createHelperInstance(ICollectorHelper helper) {
        mHelper = helper;
    }

}
