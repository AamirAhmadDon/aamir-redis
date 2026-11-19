package.com.aamir.cache;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
package com.aamir.cache;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CacheTest {

    // Test storing and retrieving a value
    @Test
    void shouldStoreAndRetrieveValue() {
        Cache cache = new Cache();
        cache.set("fighter", "Aamir");
        assertEquals("Aamir", cache.get("fighter"));
    }

    // Test deleting a key
    @Test
    void shouldDeleteKey() {
        Cache cache = new Cache();

        cache.set("fighter", "Aamir");
        cache.delete("fighter");

        assertNull(cache.get("fighter"));
    }
    // Test whether a key exists
    @Test
    void shouldCheckIfKeyExists() {
        Cache cache = new Cache();
        cache.set("fighter", "Aamir");
        assertTrue(cache.exists("fighter"));
        assertFalse(cache.exists("unknown"));
    }
    // Test cache size
    @Test
    void shouldReturnCorrectSize() {
        Cache cache = new Cache();

        cache.set("fighter", "Aamir");
        cache.set("weight_class", "Light Heavyweight");

        assertEquals(2, cache.size());
    }
}
