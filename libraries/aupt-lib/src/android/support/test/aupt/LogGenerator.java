package android.support.test.aupt;

import android.app.Instrumentation;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

enum LogGenerator {
    BUGREPORT      (new BugreportGenerator()),
    GRAPHICS_STATS (new GraphicsGenerator()),
    MEM_INFO       (new CompactMemInfoGenerator()),
    CPU_INFO       (new CpuInfoGenerator()),
    FRAGMENTATION  (new FragmentationGenerator()),
    ION_HEAP       (new IonHeapGenerator()),
    PAGETYPE_INFO  (new PageTypeInfoGenerator()),
    TRACE          (new TraceGenerator());

    /* Utilities for Generators */

    private static final String TAG = "AuptDataCollector";
    private static final FilesystemUtil sFilesystemUtil = new FilesystemUtil();

    public static FilesystemUtil fsUtil() {
        return sFilesystemUtil;
    }

    public static class FilesystemUtil {
        void saveProcessOutput(Instrumentation instr, String command, OutputStream out)
                throws IOException {
            InputStream in = null;
            try {
                ParcelFileDescriptor pfd = instr.getUiAutomation().executeShellCommand(command);
                in = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
                byte[] buffer = new byte[4096];  //4K buffer
                int bytesRead = -1;
                while (true) {
                    bytesRead = in.read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.flush();
                }
            }
        }

        String templateToFilename(String filenameTemplate) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
            return String.format(filenameTemplate, sdf.format(new Date()));
        }

        void saveBugreport(Instrumentation instr, String filename)
                throws IOException, InterruptedException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Spaces matter in the following command line. Make sure there are no spaces
            // in the filename and around the '>' sign.
            String cmdline = String.format("/system/bin/sh -c /system/bin/bugreport>%s",
                    templateToFilename(filename));
            saveProcessOutput(instr, cmdline, baos);
            baos.close();
        }

        void saveProcessOutput(Instrumentation instr, String command, String filenameTemplate)
                throws IOException, FileNotFoundException {
            String outFilename = templateToFilename(filenameTemplate);
            File file = new File(outFilename);
            Log.d(TAG, String.format("Saving command \"%s\" output into file %s",
                    command, file.getAbsolutePath()));

            OutputStream out = new FileOutputStream(file);
            saveProcessOutput(instr, command, out);
            out.close();
        }

        public void dumpMeminfo(Instrumentation instr, String notes) {
            long epochSeconds = System.currentTimeMillis() / 1000;
            File outputDir = new File(Environment.getExternalStorageDirectory(), "meminfo");
            Log.i(TAG, outputDir.toString());
            if (!outputDir.exists()) {
                boolean yes  = outputDir.mkdirs();
                Log.i(TAG, yes ? "created" : "not created");
            }
            File outputFile = new File(outputDir, String.format("%d.txt", epochSeconds));
            Log.i(TAG, outputFile.toString());
            FileOutputStream fos = null;

            try {
                fos = new FileOutputStream(outputFile);
                fos.write(String.format("notes: %s\n\n", notes).getBytes());

                saveProcessOutput(instr, "dumpsys meminfo -c", fos);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "exception while dumping meminfo", e);
            } catch (IOException e) {
                Log.e(TAG, "exception while dumping meminfo", e);
            }
        }
    }

    /* Generator Types */

    protected interface Generator {
        void save(Instrumentation instrumentation, String resultsDirectory)
                throws IOException, InterruptedException;
    }

    private static class CompactMemInfoGenerator implements Generator {
        @Override
        public void save(Instrumentation instrumentation, String resultsDirectory)
                throws IOException, InterruptedException {
            try {
                fsUtil().saveProcessOutput(instrumentation, "dumpsys meminfo -c -S",
                        resultsDirectory + "/compact-meminfo-%s.txt");
            } catch (IOException ioe) {
                Log.w(TAG, "Error while saving dumpsys meminfo -c: " + ioe.getMessage());
            }
        }
    }

    private static class CpuInfoGenerator implements Generator {
        @Override
        public void save(Instrumentation instrumentation, String resultsDirectory)
                throws IOException, InterruptedException {
            try {
                fsUtil().saveProcessOutput(instrumentation, "dumpsys cpuinfo",
                        resultsDirectory + "/cpuinfo-%s.txt");
            } catch (IOException ioe) {
                Log.w(TAG, "Error while saving dumpsys cpuinfo : " + ioe.getMessage());
            }
        }
    }

    private static class BugreportGenerator implements Generator {
        @Override
        public void save(Instrumentation instrumentation, String resultsDirectory)
                throws IOException, InterruptedException {
            try {
                fsUtil().saveBugreport(instrumentation, resultsDirectory + "/bugreport-%s.txt");
            } catch (IOException e) {
                Log.w(TAG, String.format("Failed to take bugreport: %s", e.getMessage()));
            }
        }
    }

    private static class FragmentationGenerator implements Generator {
        @Override
        public void save(Instrumentation instrumentation, String resultsDirectory)
                throws IOException, InterruptedException {
            try {
                fsUtil().saveProcessOutput(instrumentation, "cat /d/extfrag/unusable_index",
                        resultsDirectory + "/unusable-index-%s.txt");
            } catch (IOException e) {
                Log.w(TAG, String.format("Failed to save frangmentation: %s", e.getMessage()));
            }
        }
    }

    private static class GraphicsGenerator implements Generator {
        @Override
        public void save(Instrumentation instrumentation, String resultsDirectory)
                throws IOException, InterruptedException {
            try {
                fsUtil().saveProcessOutput(instrumentation, "dumpsys graphicsstats",
                        resultsDirectory + "/graphics-%s.txt");
            } catch (IOException e) {
                Log.w(TAG, String.format("Failed to save graphicsstats: %s", e.getMessage()));
            }
        }
    }

    private static class IonHeapGenerator implements Generator {
        @Override
        public void save(Instrumentation instrumentation, String resultsDirectory)
                throws IOException, InterruptedException {
            try {
                fsUtil().saveProcessOutput(instrumentation, "cat /d/ion/heaps/audio",
                        resultsDirectory + "/ion-audio-%s.txt");

                fsUtil().saveProcessOutput(instrumentation, "cat /d/ion/heaps/system",
                        resultsDirectory + "/ion-system-%s.txt");
            } catch (IOException e) {
                Log.w(TAG, String.format("Failed to save ION heap: %s", e.getMessage()));
            }
        }
    }

    private static class PageTypeInfoGenerator implements Generator {
        @Override
        public void save(Instrumentation instrumentation, String resultsDirectory)
                throws IOException, InterruptedException {
            try {
                fsUtil().saveProcessOutput(instrumentation, "cat /proc/pagetypeinfo",
                        resultsDirectory + "/pagetypeinfo-%s.txt");
            } catch (IOException e) {
                Log.w(TAG, String.format("Failed to save pagetypeinfo: %s", e.getMessage()));
            }
        }
    }

    private static class TraceGenerator implements Generator {
        @Override
        public void save(Instrumentation instrumentation, String resultsDirectory)
                throws IOException, InterruptedException {
            try {
                fsUtil().saveProcessOutput(instrumentation, "cat /sys/kernel/debug/tracing/trace",
                        resultsDirectory + "/trace-%s.txt");
            } catch (IOException e) {
                Log.w(TAG, String.format("Failed to save trace: %s", e.getMessage()));
            }
        }
    }

    // Individual LogGenerator instance methods
    private final Generator mGenerator;

    LogGenerator (Generator generator) {
        mGenerator = generator;
    }

    public void save(Instrumentation instrumentation, String resultsDirectory)
            throws IOException, InterruptedException {
        mGenerator.save(instrumentation, resultsDirectory);
    }
}
