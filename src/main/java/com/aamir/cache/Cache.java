
package com.aamir.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class Cache {

    // Stores actual key-value pairs
    private final Map<String, Object> storage;

    // Stores expiration timestamps
    private final Map<String, Long> expirationTimes;

    // Constructor
    public Cache() {
        storage = new HashMap<>();
        expirationTimes = new HashMap<>();
    }

    // Store a key-value pair
    public void set(String key, Object value) {
        storage.put(key, value);

        // A normal SET clears any previous expiration
        expirationTimes.remove(key);
    }

    // Retrieve a value by key
    public Object get(String key) {
        removeIfExpired(key);

        return storage.get(key);
    }

    // Delete a key-value pair
    public void delete(String key) {
        storage.remove(key);
        expirationTimes.remove(key);
    }

    // Check whether a key exists
    public boolean exists(String key) {
        removeIfExpired(key);

        return storage.containsKey(key);
    }

    // Return the number of key-value pairs
    public int size() {
        cleanExpiredKeys();

        return storage.size();
    }

    // Set expiration time in seconds
    public boolean expire(String key, long seconds) {

        if (!storage.containsKey(key)) {
            return false;
        }

        if (seconds <= 0) {
            delete(key);
            return true;
        }

        long expirationTime =
                System.currentTimeMillis() + (seconds * 1000);

        expirationTimes.put(key, expirationTime);

        return true;
    }

    // Get remaining time to live in seconds
    public long ttl(String key) {

        removeIfExpired(key);

        // -2 means key does not exist
        if (!storage.containsKey(key)) {
            return -2;
        }

        // -1 means key has no expiration
        if (!expirationTimes.containsKey(key)) {
            return -1;
        }

        long remainingTime =
                expirationTimes.get(key) - System.currentTimeMillis();

        return remainingTime / 1000;
    }

    // Remove a key if it has expired
    private void removeIfExpired(String key) {

        Long expirationTime = expirationTimes.get(key);

        if (expirationTime != null &&
                System.currentTimeMillis() >= expirationTime) {

            delete(key);
        }
    }

    // Clean all expired keys
    private void cleanExpiredKeys() {

        long currentTime = System.currentTimeMillis();

        Iterator<Map.Entry<String, Long>> iterator =
                expirationTimes.entrySet().iterator();

        while (iterator.hasNext()) {

            Map.Entry<String, Long> entry = iterator.next();

            if (currentTime >= entry.getValue()) {

                storage.remove(entry.getKey());
                iterator.remove();
            }
        }
    }
}
