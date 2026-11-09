
package com.aamir.cache;

public class Main {

    public static void main(String[] args) {

        // Create our cache
        Cache cache = new Cache();

        // Store information
        cache.set("fighter", "Aamir");
        cache.set("weight_class", "Light Heavyweight");
        cache.set("record", "17-0");
        cache.set("stance", "Southpaw");

        // Retrieve information
        System.out.println("Fighter: " + cache.get("fighter"));
        System.out.println("Weight Class: " + cache.get("weight_class"));
        System.out.println("Record: " + cache.get("record"));
        System.out.println("Stance: " + cache.get("stance"));

        // Check whether a key exists
        System.out.println("Does fighter exist? "
                + cache.exists("fighter"));

        // Check cache size
        System.out.println("Cache size: " + cache.size());

        // Delete an entry
        cache.delete("record");

        // Check the result
        System.out.println("Record exists after deletion? "
                + cache.exists("record"));

        System.out.println("Final cache size: " + cache.size());

        // Test expiration
        cache.set("temporary", "This will disappear");

        cache.expire("temporary", 10);

        System.out.println("TTL: " + cache.ttl("temporary"));

        // Wait 11 seconds
        try {
            Thread.sleep(11000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check whether the key expired
        System.out.println("Value after expiration: "
                + cache.get("temporary"));

        System.out.println("Does temporary exist? "
                + cache.exists("temporary"));
    }
}
