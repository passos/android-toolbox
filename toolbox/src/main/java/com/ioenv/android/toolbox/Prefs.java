package com.ioenv.android.toolbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liujinyu <simon.jinyu.liu@gmail.com>
 */
public class Prefs {
    private static final String TAG = Prefs.class.getSimpleName();
    private static PreferenceStorage storage;

    public static void init(Context context) {
        init(context, null);
    }

    public static void init(Context context, String name) {
        storage = new DefaultPreferenceStorage(context, name);
    }

    public static void init(PreferenceStorage preferenceStorage) {
        storage = preferenceStorage;
    }

    private static void assertInitialized() {
        if (storage == null) {
            throw new RuntimeException("call Preferences.init() first before use it");
        }
    }

    public static void set(String key, String value) {
        assertInitialized();
        if (value == null) {
            storage.remove(key);
        } else {
            storage.set(key, value);
        }
        Log.d(TAG, String.format("set %s: %s", key, value));
    }

    public static void set(String key, boolean value) {
        set(key, Boolean.toString(value));
    }

    public static void set(String key, int value) {
        set(key, Integer.toString(value));
    }

    public static void set(String key, long value) {
        set(key, Long.toString(value));
    }

    public static void set(String key, float value) {
        set(key, Float.toString(value));
    }

    public static void set(String key, Object value) {
        set(key, new Gson().toJson(value));
    }

    public static void setStrings(String key, List<String> value) {
        set(key, new Gson().toJson(new ArrayList<String>(value)));
    }

    public static boolean has(String key) {
        assertInitialized();
        return storage.has(key);
    }

    public static Map<String, String> getAll() {
        assertInitialized();
        Map<String, ?> map = storage.getAll();
        Map<String, String> result = new HashMap<String, String>(map.size());

        for (String key : map.keySet()) {
            result.put(key, String.valueOf(map.get(key)));
        }
        return result;
    }

    public static String get(String key) {
        return get(key, "");
    }

    public static String get(String key, String defValue) {
        assertInitialized();
        String value = storage.get(key, null);
        Log.d(TAG, String.format("get %s: %s", key, value));
        return value == null ? defValue : value;
    }

    public static boolean get(String key, boolean defValue) {
        String value = get(key);
        return has(key) ? "true".equalsIgnoreCase(value) : defValue;
    }

    public static int get(String key, int defValue) {
        try {
            return Integer.parseInt(get(key));
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static long get(String key, long defValue) {
        try {
            return Long.parseLong(get(key));
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static float get(String key, float defValue) {
        try {
            return Float.parseFloat(get(key));
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static <T> T get(String key, Class<T> clz, T defValue) {
        try {
            return new Gson().fromJson(get(key), clz);
        } catch (JsonSyntaxException e) {
            return defValue;
        }
    }

    public static <T> T get(String key, Type typeOfT, T defValue) {
        try {
            String json = get(key, "{}");
            T result = new Gson().fromJson(json, typeOfT);
            return result == null ? defValue : result;
        } catch (JsonSyntaxException e) {
            return defValue;
        }
    }

    public static List<String> getStrings(String key) {
        List<String> defValue = Collections.emptyList();
        return get(key, new TypeToken<ArrayList<String>>() {
        }.getType(), defValue);
    }

    public static <T> List<T> getList(String key, Type typeOfT) {
        return get(key, typeOfT, new ArrayList<T>(0));
    }

    public static void clear() {
        assertInitialized();
        storage.clear();
    }

    public static void remove(String key) {
        assertInitialized();
        storage.remove(key);
    }

    public static String dump() {
        assertInitialized();
        return toJson(storage.getAll());
    }

    public static String toJson(Object o) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(o);
    }

    public interface OnPreferenceChangeListener {
        void OnPreferenceChange(PreferenceStorage preferenceStorage, String key);
    }

    public interface PreferenceStorage {

        boolean has(String key);

        void set(String key, String value);

        String get(String key, String defValue);

        Map<String, ?> getAll();

        void remove(String key);

        void clear();
    }

    public static class DefaultPreferenceStorage implements PreferenceStorage {
        private SharedPreferences sharedPreferences;

        public DefaultPreferenceStorage(Context context, String name) {
            if (TextUtils.isEmpty(name)) {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            } else {
                sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
            }
        }

        @Override
        public boolean has(String key) {
            return sharedPreferences.contains(key);
        }

        public void set(String key, String value) {
            sharedPreferences.edit().putString(key, value).apply();
        }

        @Override
        public String get(String key, String defValue) {
            return sharedPreferences.getString(key, defValue);
        }

        @Override
        public Map<String, ?> getAll() {
            return sharedPreferences.getAll();
        }

        @Override
        public void remove(String key) {
            sharedPreferences.edit().remove(key).apply();
        }

        @Override
        public void clear() {
            sharedPreferences.edit().clear().apply();
        }
    }
}

