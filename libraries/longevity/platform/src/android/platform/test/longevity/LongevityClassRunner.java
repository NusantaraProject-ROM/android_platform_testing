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

package android.platform.test.longevity;

import androidx.annotation.VisibleForTesting;

import java.util.List;
import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * A {@link BlockJUnit4ClassRunner} that runs the test class's {@link BeforeClass} methods as {@link
 * Before} methods and {@link AfterClass} methods as {@link After} methods for metric collection in
 * longevity tests.
 */
public class LongevityClassRunner extends BlockJUnit4ClassRunner {
    public LongevityClassRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    /**
     * Override the parent {@code withBeforeClasses} method to be a no-op.
     *
     * <p>The {@link BeforeClass} methods will be included later as {@link Before} methods.
     */
    @Override
    protected Statement withBeforeClasses(Statement statement) {
        return statement;
    }

    /**
     * Override the parent {@code withAfterClasses} method to be a no-op.
     *
     * <p>The {@link AfterClass} methods will be included later as {@link After} methods.
     */
    @Override
    protected Statement withAfterClasses(Statement statement) {
        return statement;
    }

    /**
     * Runs the {@link BeforeClass} methods before running all the {@link Before} methods of the
     * test class.
     */
    @Override
    protected Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
        List<FrameworkMethod> allBeforeMethods = new ArrayList<FrameworkMethod>();
        allBeforeMethods.addAll(getTestClass().getAnnotatedMethods(BeforeClass.class));
        allBeforeMethods.addAll(getTestClass().getAnnotatedMethods(Before.class));
        return allBeforeMethods.isEmpty()
                ? statement
                : getRunBefores(statement, allBeforeMethods, target);
    }

    /**
     * Runs the {@link AfterClass} methods after running all the {@link After} methods of the test
     * class.
     */
    @Override
    protected Statement withAfters(FrameworkMethod method, Object target, Statement statement) {
        List<FrameworkMethod> allAfterMethods = new ArrayList<FrameworkMethod>();
        allAfterMethods.addAll(getTestClass().getAnnotatedMethods(After.class));
        allAfterMethods.addAll(getTestClass().getAnnotatedMethods(AfterClass.class));
        return allAfterMethods.isEmpty()
                ? statement
                : getRunAfters(statement, allAfterMethods, target);
    }

    /** Factory method to return the {@link RunBefores} object. Exposed for testing only. */
    @VisibleForTesting
    protected RunBefores getRunBefores(
            Statement statement, List<FrameworkMethod> befores, Object target) {
        return new RunBefores(statement, befores, target);
    }

    /** Factory method to return the {@link RunBefores} object. Exposed for testing only. */
    @VisibleForTesting
    protected RunAfters getRunAfters(
            Statement statement, List<FrameworkMethod> afters, Object target) {
        return new RunAfters(statement, afters, target);
    }
}
