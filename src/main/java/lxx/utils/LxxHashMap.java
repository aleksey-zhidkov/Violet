package lxx.utils;

import lxx.utils.func.F1;

import java.util.HashMap;

public class LxxHashMap<K, V> extends HashMap<K, V> {

    private final F1<K, V> factory;

    public LxxHashMap(F1<K, V> factory) {
        this.factory = factory;
    }

    @Override
    public V get(Object key) {
        V value = super.get(key);

        if (value == null) {
            value = factory.f((K) key);
            put((K) key, value);
        }

        return value;
    }

}
