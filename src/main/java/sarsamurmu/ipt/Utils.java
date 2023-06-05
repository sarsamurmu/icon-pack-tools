package sarsamurmu.ipt;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static MouseListener onClick(Runnable click) {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                click.run();
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        };
    }

    public static String chooseXMLFile(@Nullable File defaultPath) {
        MemoryStack stack = MemoryStack.stackPush();

        PointerBuffer filters = stack.mallocPointer(1);
        filters.put(stack.UTF8("*.xml"));
        filters.flip();

        String defaultString = null;
        if (defaultPath != null) {
            defaultString = defaultPath.getAbsolutePath();
            if (defaultPath.isDirectory() && !defaultString.endsWith(File.separator)) {
                defaultString += File.separator;
            }
        }

        String result = TinyFileDialogs.tinyfd_openFileDialog(
                "Choose file", defaultString, filters, "XML files", false);

        stack.pop();

        return result;
    }

    public static String chooseDirectory(@Nullable String defaultPath) {
        return TinyFileDialogs.tinyfd_selectFolderDialog("Choose folder", defaultPath != null ? defaultPath : "");
    }

    public static JDialog makeDialog(String title, Dimension dim) {
        JDialog dialog = new JDialog(Main.frame, title, true);
        dialog.setSize(dim);
        Point loc = Main.frame.getLocation();
        Dimension size = Main.frame.getSize();
        Dimension dsize = dialog.getSize();
        dialog.setLocation(
                loc.x + size.width / 2 - dsize.width / 2,
                loc.y + size.height / 2 - dsize.height / 2 - 10);
        return dialog;
    }

    public static InputStream loadResource(String name) {
        String prop = System.getProperty("sarsa.debug");
        boolean debug = prop != null && prop.equals("true");

        try {
            if (debug) {
                return new FileInputStream("src/main/res/" + name);
            } else {
                URL url = Utils.class.getResource("/res/" + name);
                assert url != null;
                return url.openStream();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Empty stream
            return new InputStream() {
                @Override
                public int read() {
                    return -1;
                }
            };
        }
    }

    // This class is taken from - https://stackoverflow.com/a/20978973/11061906
    public static class Debouncer {
        private final ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
        private final ConcurrentHashMap<String, TimerTask> delayedMap = new ConcurrentHashMap<String, TimerTask>();
        private final Runnable callback;
        private final int interval;

        public Debouncer(Runnable c, int interval) {
            this.callback = c;
            this.interval = interval;
        }

        public void call() {
            TimerTask task = new TimerTask();
            TimerTask prev;
            do {
                prev = delayedMap.putIfAbsent("main", task);
                if (prev == null)
                    sched.schedule(task, interval, TimeUnit.MILLISECONDS);
            } while (prev != null && !prev.extend()); // Exit only if new task was added to map, or existing task was extended successfully
        }

        public void terminate() {
            sched.shutdownNow();
        }

        // The task that wakes up when the wait time elapses
        private class TimerTask implements Runnable {
            private long dueTime;
            private final Object lock = new Object();

            public TimerTask() {
                extend();
            }

            public boolean extend() {
                synchronized (lock) {
                    if (dueTime < 0) // Task has been shutdown
                        return false;
                    dueTime = System.currentTimeMillis() + interval;
                    return true;
                }
            }

            public void run() {
                synchronized (lock) {
                    long remaining = dueTime - System.currentTimeMillis();
                    if (remaining > 0) { // Re-schedule task
                        sched.schedule(this, remaining, TimeUnit.MILLISECONDS);
                    } else { // Mark as terminated and invoke callback
                        dueTime = -1;
                        try {
                            callback.run();
                        } finally {
                            delayedMap.remove("main");
                        }
                    }
                }
            }
        }
    }
}
