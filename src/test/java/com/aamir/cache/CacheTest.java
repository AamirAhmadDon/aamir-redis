
package com.aamir.cache;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CacheTest {

    @Test
    void shouldStoreAndRetrieveValue() {
        Cache cache = new Cache();
        cache.set("fighter", "Aamir");

        assertEquals("Aamir", cache.get("fighter"));
    }

    @Test
    void shouldDeleteKey() {
        Cache cache = new Cache();
        cache.set("fighter", "Aamir");

        cache.delete("fighter");

        assertNull(cache.get("fighter"));
    }

    @Test
    void shouldCheckIfKeyExists() {
        Cache cache = new Cache();
        cache.set("fighter", "Aamir");

        assertTrue(cache.exists("fighter"));
        assertFalse(cache.exists("unknown"));
    }

    @Test
    void shouldReturnCorrectSize() {
        Cache cache = new Cache();
        cache.set("fighter", "Aamir");
        cache.set("weight_class", "Light Heavyweight");

        assertEquals(2, cache.size());
    }

    @Test
    void shouldSaveAndLoadCache() throws IOException {
        Path file = Files.createTempFile("aamir-cache", ".dat");

        try {
            Cache originalCache = new Cache();

            originalCache.set("fighter", "Aamir");
            originalCache.set("record", "17-0");
            originalCache.set("weight", 96);

            originalCache.save(file);

            Cache restoredCache = new Cache();
            restoredCache.load(file);

            assertEquals("Aamir", restoredCache.get("fighter"));
            assertEquals("17-0", restoredCache.get("record"));
            assertEquals(96, restoredCache.get("weight"));
            assertEquals(3, restoredCache.size());

        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void shouldPreserveExpirationAfterLoading() throws IOException {
        Path file = Files.createTempFile("aamir-cache", ".dat");

        try {
            Cache originalCache = new Cache();

            originalCache.set("temporary", "Training data");
            originalCache.expire("temporary", 60);

            originalCache.save(file);

            Cache restoredCache = new Cache();
            restoredCache.load(file);

            assertTrue(restoredCache.exists("temporary"));
            assertTrue(restoredCache.ttl("temporary") > 0);

        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void shouldNotRestoreExpiredEntries()
            throws IOException, InterruptedException {

        Path file = Files.createTempFile("aamir-cache", ".dat");

        try {
            Cache originalCache = new Cache();

            originalCache.set("temporary", "This should disappear");
            originalCache.expire("temporary", 1);

            // Save while the key is still alive
            originalCache.save(file);

            // Wait until the saved key expires
            Thread.sleep(1100);

            // Load after its expiration timestamp has passed
            Cache restoredCache = new Cache();
            restoredCache.load(file);

            assertFalse(restoredCache.exists("temporary"));
            assertEquals(0, restoredCache.size());

        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void shouldRejectCorruptedPersistenceFile() throws IOException {
        Path file = Files.createTempFile("aamir-cache", ".dat");
        try {
            Files.writeString(file, "This is not a valid AamirCache file");
            Cache cache = new Cache();
            assertThrows(IOException.class, () -> cache.load(file));
        } finally {
            Files.deleteIfExists(file);
        }
    }
}