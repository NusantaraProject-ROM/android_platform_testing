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

package android.support.test.aupt;

import android.app.Instrumentation;
import android.content.Context;
import android.content.ContextWrapper;
import android.test.AndroidTestRunner;
import android.util.Log;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestSuite;

/**
 * A DexTestRunner runs tests by name from a given list of JARs,
 * with the following additional magic:
 *
 * - Custom ClassLoading from given dexed Jars
 * - Custom test scheduling (via Scheduler)
 *
 * In addition to the parameters in the constructor, be sure to run setTest or setTestClassName
 * before attempting to runTest.
 */
class DexTestRunner extends AndroidTestRunner {
    /* Constants */
    static final String DEFAULT_JAR_PATH = "/data/local/tmp/";
    static final String DEX_OPT_PATH = "dex-test-opt";

    /* Private fields */
    private final List<TestListener> mTestListeners = new ArrayList<>();
    private final DexClassLoader mLoader;

    /* State */
    private TestResult mTestResult = new TestResult();
    private List<TestCase> mTestCases = new ArrayList<>();
    private String mTestClassName;
    private Instrumentation mInstrumentation;
    private Scheduler mScheduler;

    /* Field initialization */
    DexTestRunner(Instrumentation instrumentation, Scheduler scheduler, List<String> jars) {
        super();

        mInstrumentation = instrumentation;
        mScheduler = scheduler;
        mLoader = makeLoader(jars);
    }

    /* Main methods */

    @Override
    public void runTest() {
        runTest(new TestResult());
    }

    @Override
    public void runTest(TestResult testResult) {
        for (TestCase testCase : mScheduler.apply(mTestCases)) {
            try {
                onTestStart(testCase);
                testCase.run(testResult);
            } catch (Exception ex) {
                onError(testCase, ex);

                if (ex instanceof AuptTerminator) {
                    throw ex;
                }
            } finally {
                onTestFinish(testCase);
            }
        }
    }

    /* TestCase Initialization */

    @Override
    public void setTestClassName(String className, String methodName) {
        mTestCases.clear();
        addTestClassByName(className, methodName);
    }

    void addTestClassByName(final String className, final String methodName) {
        try {
            final Class<?> testClass = mLoader.loadClass(className);

            if (Test.class.isAssignableFrom(testClass)) {
                Test test = null;

                try {
                    // Make sure it works
                    test = (Test) testClass.getConstructor().newInstance();
                } catch (Exception e1) { /* If we fail, test will just stay null */ }

                try {
                    test = (Test) testClass.getConstructor(String.class).newInstance(methodName);
                } catch (Exception e2) { /* If we fail, test will just stay null */ }

                addTest(test);
            } else {
                throw new RuntimeException("Test class not found: " + className);
            }
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Class not found: " + ex.getMessage());
        }

        if (mTestCases.isEmpty()) {
            throw new RuntimeException("No tests found in " + className + "#" + methodName);
        }
    }

    @Override
    public void setTest(Test test) {
        mTestCases.clear();
        addTest(test);

        // Update our test class name.
        if (TestSuite.class.isAssignableFrom(test.getClass())) {
            mTestClassName = ((TestSuite) test).getName();
        } else if (TestCase.class.isAssignableFrom(test.getClass())) {
            mTestClassName = ((TestCase) test).getName();
        } else {
            mTestClassName = test.getClass().getSimpleName();
        }
    }

    public void addTest(Test test) {
        if (test instanceof TestCase) {

            mTestCases.add((TestCase) test);

        } else if (test instanceof TestSuite) {
            Enumeration<Test> tests = ((TestSuite) test).tests();

            while (tests.hasMoreElements()) {
                addTest(tests.nextElement());
            }
        } else {
            throw new RuntimeException("Tried to add invalid test: " + test.toString());
        }
    }

    /* State Manipulation Methods */

    @Override
    public void clearTestListeners() {
        mTestListeners.clear();
    }

    @Override
    public void addTestListener(TestListener testListener) {
        if (testListener != null) {
            mTestListeners.add(testListener);
        }
    }

    void addTestListenerIf(Boolean cond, TestListener testListener) {
        if (cond && testListener != null) {
            mTestListeners.add(testListener);
        }
    }

    @Override
    public List<TestCase> getTestCases() {
        return mTestCases;
    }

    @Override
    public void setInstrumentation(Instrumentation instrumentation) {
        mInstrumentation = instrumentation;
    }

    @Override
    public TestResult getTestResult() {
        return mTestResult;
    }

    @Override
    protected TestResult createTestResult() {
        return new TestResult();
    }

    @Override
    public String getTestClassName() {
        return mTestClassName;
    }

    /* Listener Callbacks */

    void onTestStart(Test test) {
        for (TestListener listener : mTestListeners) {
            listener.startTest(test);
        }
    }

    void onTestFinish(Test test) {
        for (TestListener listener : mTestListeners) {
            listener.endTest(test);
        }
    }

    void onError(Test test, Throwable t) {
        if (t instanceof AssertionFailedError) {
            for (TestListener listener : mTestListeners) {
                listener.addFailure(test, (AssertionFailedError) t);
            }
        } else {
            for (TestListener listener : mTestListeners) {
                listener.addError(test, t);
            }
        }
    }

    /* Package-private Utilities */

    static List<String> parseDexedJarPaths(String jarString) {
        List<String> jars = new ArrayList<>();

        for (String jar : jarString.split(":")) {
            // Check that jar isn't empty, but don't fail because String::split will yield
            // spurious empty results if, for example, we don't specify any jars, accidentally
            // start with a leading colon, etc.
            if (!jar.trim().isEmpty()) {
                File jarFile = jar.startsWith("/")
                        ? new File(jar)
                        : new File(DEFAULT_JAR_PATH + jar);

                if (jarFile.exists()) {
                    jars.add(jarFile.getAbsolutePath());
                } else {
                    throw new RuntimeException("Can't find jar file " + jarFile);
                }
            }
        }

        return jars;
    }

    DexClassLoader getDexClassLoader() {
        return mLoader;
    }

    DexClassLoader makeLoader(List<String> jars) {
        StringBuilder jarFiles = new StringBuilder();

        for (String jar : jars) {
            if (new File(jar).exists() && new File(jar).canRead()) {
                if (jarFiles.length() != 0) {
                    jarFiles.append(File.pathSeparator);
                }

                jarFiles.append(jar);
            } else {
                throw new IllegalArgumentException(
                        "Jar file does not exist or not accessible: "  + jar);
            }
        }

        File optDir = new File(mInstrumentation.getTargetContext().getCacheDir(), DEX_OPT_PATH);

        if (optDir.exists() || optDir.mkdirs()) {
            return new DexClassLoader(
                    jarFiles.toString(),
                    optDir.getAbsolutePath(),
                    null,
                    DexTestRunner.class.getClassLoader());
        } else {
            throw new RuntimeException(
                    "Failed to create dex optimization directory: " + optDir.getAbsolutePath());
        }
    }
}
