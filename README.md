# JL's Android ToolBox

## Log

``` Java
private static final String TAG = Log.tag(SomeClass.class);

public void test(String key, int value) {
    Log.d(TAG, "This is a log for %s: %s", key, value);
}
```

## Prefs

```Java
Preferences.init(getContext());

Preferences.set("str_config", "String");
Preferences.set("int_config", 1234);
Preferences.set("long_config", 1234567890L);
Preferences.set("bool_config", true);
Preferences.set("float_config", 123.45);

Preferences.get("str_config"， "Default");
Preferences.get("int_config"， 0);
Preferences.get("long_config"， 0L);
Preferences.get("float_config"， 0f);
Preferences.get("bool_config"， false);

class DataObject {
    String key;
    long value;
    public DataObject(String key, long value) {
        this.key = key;
        this.value = value;
    }
}

Preferences.set("obj_config", new DataObject("Level", 123));
DataObject data = Preferences.get("obj_config"， DataObject.class, null);
```

## Stream
A simple stream class for legacy code

```
int[] data_list = new int[]{1, 2, 3, 4, 5};

Stream.of(data_list)
    .map(it -> 2 * it)
    .join(","); // 2, 4, 6, 8, 10

Stream.of(data_list)
    .filter(it -> it % 2 == 0)
    .asList(); // 2, 4

Stream.of(data_list)
    .filter(it -> it % 2 == 0)
    .map(it -> 2 * it)
    .asMap(it -> String.format("Key%d", it)); // { "Key2": 2, "Key4": 4 }

```