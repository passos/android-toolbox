package com.ioenv.android.toolbox;

import android.annotation.TargetApi;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jinyu LIU <simon.jinyu.liu@gmail.com>
 */
public class Log {
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("\\$\\d+$");
    private static String tagPrefix = "";
    private static int MAX_MESSAGE_LENGTH = 4000; // adb logcat -g

    public static void setIsDebug(boolean isDebug) {
        Log.isDebug = isDebug;
    }

    private static boolean isDebug = true;
    private static SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss.SSS", Locale.getDefault());

    private Log() {
        // empty
    }

    private static long threadId() {
        return Thread.currentThread().getId();
    }

    public static void i(String tag, String msg, Object... args) {
        if (isDebug) {
            println(android.util.Log.INFO, tag, formatString(msg, args), null);
        }
    }

    public static void v(String tag, String msg, Object... args) {
        if (isDebug) {
            println(android.util.Log.VERBOSE, tag, formatString(msg, args), null);
        }
    }

    public static void d(String tag, String msg, Object... args) {
        if (isDebug) {
            println(android.util.Log.DEBUG, tag, formatString(msg, args), null);
        }
    }

    public static void w(String tag, String msg, Object... args) {
        if (isDebug) {
            println(android.util.Log.WARN, tag, formatString(msg, args), null);
        }
    }

    public static void w(String tag, Throwable th) {
        if (isDebug) {
            println(android.util.Log.WARN, tag, "", th);
        }
    }

    public static void e(String tag, String msg, Object... args) {
        if (isDebug) {
            println(android.util.Log.ERROR, tag, formatString(msg, args), null);
        }
    }

    public static void e(String tag, String msg, Throwable th) {
        if (isDebug) {
            println(android.util.Log.ERROR, tag, msg, th);
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public static void wtf(String tag, String msg, Object... args) {
        if (isDebug) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                android.util.Log.wtf(tagPrefix + tag, formatString(msg, args));
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public static void wtf(String tag, String msg, Throwable th) {
        if (isDebug) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                android.util.Log.wtf(tagPrefix + tag, msg, th);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public static void wtfStack(String tag, String msg, Throwable th) {
        if (isDebug) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                android.util.Log.wtf(tagPrefix + tag, formatString(msg));
                android.util.Log.wtf(tagPrefix + tag, android.util.Log.getStackTraceString(th));
            }
        }
    }

    public static void printStackTrace(Throwable th) {
        if (isDebug && th != null) {
            th.printStackTrace();
        }
    }

    public static String getStackTraceString(Throwable th) {
        return android.util.Log.getStackTraceString(th);
    }

    private static String getCallerInfo() {
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        for (int i = 2; i < callStack.length; i++) {
            StackTraceElement e = callStack[i];
            if (!Log.class.getName().equals(e.getClassName())) {
                return String.format("%s:%d", e.getMethodName(), e.getLineNumber());
            }
        }

        return "";
    }

    private static String formatString(String msg, Object... args) {
        String threadId = String.format(Locale.getDefault(), "%s [%s][%d] ",
            getCallerInfo(), sdf.format(Calendar.getInstance().getTimeInMillis()), threadId());
        if (args.length > 0) {
            return threadId + String.format(msg, args);
        } else {
            return threadId + msg;
        }
    }

    private static void println(int priority, String tag, String message, Throwable t) {
        if (!isDebug) {
            return;
        }
        if (message == null || message.length() == 0) {
            return;
        }
        if (t != null) {
            message += "\n" + android.util.Log.getStackTraceString(t);
        }
        if (message.length() < MAX_MESSAGE_LENGTH) {
            android.util.Log.println(priority, tagPrefix + tag, message);
        } else {
            // It's rare that the message will be this large, so we're ok with the perf hit of splitting
            // and calling Log.println N times. It's possible but unlikely that a single line will be
            // longer than 4000 characters: we're explicitly ignoring this case here.
            String[] lines = message.split("\n");
            for (String line : lines) {
                android.util.Log.println(priority, tagPrefix + tag, line);
            }
        }
    }

    private static List<String> splitLongString(String str) {
        if (str == null) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>(str.length() / MAX_MESSAGE_LENGTH + 1);
        for (int offset = 0; offset < str.length(); ) {
            int size = Math.min(MAX_MESSAGE_LENGTH, str.length() - offset);
            result.add(str.substring(offset, offset + size));
            offset += size;
        }

        return result;
    }

    public static String defaultTag() {
        String tag = new Throwable().getStackTrace()[5].getClassName();
        Matcher m = ANONYMOUS_CLASS.matcher(tag);
        if (m.find()) {
            tag = m.replaceAll("");
        }
        return tag.substring(tag.lastIndexOf('.') + 1);
    }

    public static String tag(Class clazz) {
        return clazz.getName();
    }

    public static String tagSimple(Class clazz) {
        return clazz.getSimpleName();
    }

    public static void setTagPrefix(String prefix) {
        tagPrefix = prefix;
    }

    public static void dumpCallStack(String message) {
        new Throwable(message).printStackTrace();
    }

    public static void dumpCallStack(String tag, int count) {
        Log.d(tag, "Current call stack:\n");
        StackTraceElement[] stes = Thread.currentThread().getStackTrace();
        for (int i = 3; i < count + 3 && i < stes.length; i++) {
            StackTraceElement ste = stes[i];
            Log.d(tag, "\t-> %s.%s(%s:%d)",
                ste.getClassName(), ste.getMethodName(), ste.getFileName(), ste.getLineNumber());
        }
    }
}

