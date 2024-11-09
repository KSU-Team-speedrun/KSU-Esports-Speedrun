package edu.Kennesaw.ksumcspeedrun.Utilities;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentTwoWayMap<K, V> {
    private final ConcurrentHashMap<K, V> keyToValue = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<V, K> valueToKey = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock(); // Lock to ensure atomic operations

    // Put a key-value pair (ensures 1-to-1 mapping)
    public void put(K key, V value) {
        lock.lock();
        try {
            if (keyToValue.containsKey(key) || valueToKey.containsKey(value)) {
                throw new IllegalArgumentException("Duplicate key or value not allowed.");
            }
            keyToValue.put(key, value);
            valueToKey.put(value, key);
        } finally {
            lock.unlock();
        }
    }

    // Get value by key
    public V getByKey(K key) {
        return keyToValue.get(key);
    }

    // Get key by value
    public K getByValue(V value) {
        return valueToKey.get(value);
    }

    // Remove by key
    public void removeByKey(K key) {
        lock.lock();
        try {
            V value = keyToValue.remove(key);
            if (value != null) {
                valueToKey.remove(value);
            }
        } finally {
            lock.unlock();
        }
    }

    // Remove by value
    public void removeByValue(V value) {
        lock.lock();
        try {
            K key = valueToKey.remove(value);
            if (key != null) {
                keyToValue.remove(key);
            }
        } finally {
            lock.unlock();
        }
    }

    // Check if the map contains a key
    public boolean containsKey(K key) {
        return keyToValue.containsKey(key);
    }

    // Check if the map contains a value
    public boolean containsValue(V value) {
        return valueToKey.containsKey(value);
    }
}
