/*
 * Copyright (C) 2014 The Android Open Source Project
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
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DataCollector {
    private static final String TAG = "AuptDataCollector";

    private final Map<LogGenerator, Long> generatorsWithIntervals = new HashMap<>();
    private final Logger mLogger = new Logger();
    private final Instrumentation instrumentation;
    private final String resultsDirectory;

    private Thread mLoggerThread;

    /**
     * Add a generator iff the interval is valid (i.e. > 0).
     */
    private void put(LogGenerator key, Long interval) {
        if (interval > 0) {
            generatorsWithIntervals.put(key, interval);
        }
    }

    public DataCollector(long bugreportInterval, long graphicsInterval,      long meminfoInterval,
                         long cpuinfoInterval,   long fragmentationInterval, long ionHeapInterval,
                         long pagetypeinfoInterval, long traceInterval,
                         File outputLocation, Instrumentation instr) {

        resultsDirectory = outputLocation.getPath();
        instrumentation = instr;

        put(LogGenerator.BUGREPORT, bugreportInterval);
        put(LogGenerator.CPU_INFO, cpuinfoInterval);
        put(LogGenerator.FRAGMENTATION, fragmentationInterval);
        put(LogGenerator.GRAPHICS_STATS, graphicsInterval);
        put(LogGenerator.ION_HEAP, ionHeapInterval);
        put(LogGenerator.MEM_INFO, meminfoInterval);
        put(LogGenerator.PAGETYPE_INFO, pagetypeinfoInterval);
        put(LogGenerator.TRACE, traceInterval);
    }

    public void start() {
        mLoggerThread = new Thread(mLogger);
        mLoggerThread.start();
    }

    public void stop() {
        mLogger.stop();
        try {
            mLoggerThread.join();
        } catch (InterruptedException e) {
            // ignore
        }
    }

    protected class Logger implements Runnable {
        private final Map<LogGenerator, Long> mLastUpdate = new HashMap<>();
        private final long mSleepInterval;
        private boolean mStopped = false;

        public Logger() {
            for (Map.Entry<LogGenerator, Long> entry : generatorsWithIntervals.entrySet()) {
                if (entry.getValue() > 0) {
                    try {
                        entry.getKey().save(instrumentation, resultsDirectory);
                    } catch (InterruptedException ex) {
                        /* Ignore interruptions */
                    } catch (IOException ex) {
                        Log.e(TAG, "Error writing results in " + resultsDirectory +
                                ": " + ex.toString());
                    }

                    mLastUpdate.put(entry.getKey(), SystemClock.uptimeMillis());
                }
            }

            mSleepInterval = gcd(generatorsWithIntervals.values());
        }

        public void stop() {
            synchronized(this) {
                mStopped = true;
                notifyAll();
            }
        }

        private long gcd(Collection<Long> values) {
            if (values.size() < 2)
                return 0;

            long gcdSoFar = values.iterator().next();

            for (Long value : values) {
                gcdSoFar = gcd(gcdSoFar, value);
            }

            return gcdSoFar;
        }

        private long gcd(long a, long b) {
            if (a == 0)
                return b;
            if (b == 0)
                return a;
            if (a > b)
                return gcd(b, a % b);
            else
                return gcd(a, b % a);
        }

        @Override
        public void run() {
            if (mSleepInterval <= 0) {
                return;
            }

            synchronized(this) {
                while (!mStopped) {
                    try {
                        for (Map.Entry<LogGenerator, Long> entry : generatorsWithIntervals.entrySet()) {
                            Long t = SystemClock.uptimeMillis() - mLastUpdate.get(entry.getKey());

                            if (entry.getValue() > 0 && t > entry.getValue()) {
                                try {
                                    entry.getKey().save(instrumentation, resultsDirectory);
                                } catch (IOException ex) {
                                    Log.e(TAG, "Error writing results in " + resultsDirectory +
                                            ": " + ex.toString());
                                }

                                mLastUpdate.put(entry.getKey(), SystemClock.uptimeMillis());
                            }
                        }
                        wait(mSleepInterval);
                    } catch (InterruptedException e) {
                        // Ignore.
                    }
                }
            }
        }
    }
}
