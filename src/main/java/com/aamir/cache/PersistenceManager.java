
package com.aamir.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class PersistenceManager {

    // Identifies an AamirCache persistence file
    private static final int MAGIC_NUMBER = 0x41414D52;

    // Current binary format version
    private static final int FORMAT_VERSION = 1;

    // Supported value type identifiers
    private static final byte TYPE_NULL = 0;
    private static final byte TYPE_STRING = 1;
    private static final byte TYPE_INTEGER = 2;
    private static final byte TYPE_LONG = 3;
    private static final byte TYPE_DOUBLE = 4;
    private static final byte TYPE_BOOLEAN = 5;

    // Safety limits when loading files
    private static final int MAX_ENTRIES = 1_000_000;
    private static final int MAX_STRING_BYTES = 64 * 1024 * 1024;


    // Save cache data to a binary file
    public void save(
            Map<String, Object> storage,
            Map<String, Long> expirationTimes,
            Path file
    ) throws IOException {

        Path absoluteFile = file.toAbsolutePath();
        Path parent = absoluteFile.getParent();

        // Create the parent directory if necessary
        Files.createDirectories(parent);

        // Create a temporary file beside the destination
        Path temporaryFile = Files.createTempFile(
                parent,
                "aamir-cache-",
                ".tmp"
        );

        boolean savedSuccessfully = false;

        try {

            long currentTime = System.currentTimeMillis();

            // Count entries that have not expired
            int entryCount = 0;

            for (String key : storage.keySet()) {

                Long expirationTime = expirationTimes.get(key);

                if (expirationTime == null ||
                        expirationTime > currentTime) {
                    entryCount++;
                }
            }

            // Write binary data
            try (DataOutputStream output = new DataOutputStream(
                    new BufferedOutputStream(
                            Files.newOutputStream(temporaryFile)
                    )
            )) {

                // Write file header
                output.writeInt(MAGIC_NUMBER);
                output.writeInt(FORMAT_VERSION);

                // Write number of entries
                output.writeInt(entryCount);

                // Write each cache entry
                for (Map.Entry<String, Object> entry :
                        storage.entrySet()) {

                    String key = entry.getKey();
                    Object value = entry.getValue();

                    Long expirationTime = expirationTimes.get(key);

                    // Skip expired entries
                    if (expirationTime != null &&
                            expirationTime <= currentTime) {
                        continue;
                    }

                    // Write key
                    writeString(output, key);

                    // Write value and its type
                    writeValue(output, value);

                    // Write expiration information
                    if (expirationTime != null) {
                        output.writeBoolean(true);
                        output.writeLong(expirationTime);
                    } else {
                        output.writeBoolean(false);
                    }
                }
            }

            // Replace the destination file with the completed snapshot
            try {
                Files.move(
                        temporaryFile,
                        absoluteFile,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(
                        temporaryFile,
                        absoluteFile,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            savedSuccessfully = true;

        } finally {

            // Remove temporary file if saving failed
            if (!savedSuccessfully) {
                Files.deleteIfExists(temporaryFile);
            }
        }
    }


    // Load cache data from a binary file
    public CacheSnapshot load(Path file) throws IOException {

        Map<String, Object> storage = new LinkedHashMap<>();
        Map<String, Long> expirationTimes = new LinkedHashMap<>();

        try (DataInputStream input = new DataInputStream(
                new BufferedInputStream(
                        Files.newInputStream(file)
                )
        )) {

            // Validate file identity
            int magicNumber = input.readInt();

            if (magicNumber != MAGIC_NUMBER) {
                throw new IOException(
                        "Invalid AamirCache persistence file."
                );
            }

            // Validate format version
            int version = input.readInt();

            if (version != FORMAT_VERSION) {
                throw new IOException(
                        "Unsupported persistence format version: "
                                + version
                );
            }

            // Read number of entries
            int entryCount = input.readInt();

            if (entryCount < 0 || entryCount > MAX_ENTRIES) {
                throw new IOException(
                        "Invalid entry count: " + entryCount
                );
            }

            long currentTime = System.currentTimeMillis();

            // Read each entry
            for (int i = 0; i < entryCount; i++) {

                // Read key
                String key = readString(input);

                if (storage.containsKey(key)) {
                    throw new IOException(
                            "Duplicate cache key: " + key
                    );
                }

                // Read value
                Object value = readValue(input);

                // Read expiration information
                boolean hasExpiration = input.readBoolean();

                Long expirationTime = null;

                if (hasExpiration) {
                    expirationTime = input.readLong();
                }

                // Ignore entries that have already expired
                if (expirationTime != null &&
                        expirationTime <= currentTime) {
                    continue;
                }

                storage.put(key, value);

                if (expirationTime != null) {
                    expirationTimes.put(key, expirationTime);
                }
            }

            // Reject unexpected trailing data
            if (input.read() != -1) {
                throw new IOException(
                        "Unexpected data at the end of persistence file."
                );
            }
        }

        return new CacheSnapshot(storage, expirationTimes);
    }


    // Write a String using UTF-8 encoding
    private void writeString(
            DataOutputStream output,
            String value
    ) throws IOException {

        if (value == null) {
            throw new IOException("Cache keys cannot be null.");
        }

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        output.writeInt(bytes.length);
        output.write(bytes);
    }


    // Read a String using UTF-8 encoding
    private String readString(DataInputStream input)
            throws IOException {

        int length = input.readInt();

        if (length < 0 || length > MAX_STRING_BYTES) {
            throw new IOException(
                    "Invalid string length: " + length
            );
        }

        byte[] bytes = new byte[length];

        input.readFully(bytes);

        return new String(bytes, StandardCharsets.UTF_8);
    }


    // Write a value along with its type identifier
    private void writeValue(
            DataOutputStream output,
            Object value
    ) throws IOException {

        if (value == null) {

            output.writeByte(TYPE_NULL);

        } else if (value instanceof String) {

            output.writeByte(TYPE_STRING);
            writeString(output, (String) value);

        } else if (value instanceof Integer) {

            output.writeByte(TYPE_INTEGER);
            output.writeInt((Integer) value);

        } else if (value instanceof Long) {

            output.writeByte(TYPE_LONG);
            output.writeLong((Long) value);

        } else if (value instanceof Double) {

            output.writeByte(TYPE_DOUBLE);
            output.writeDouble((Double) value);

        } else if (value instanceof Boolean) {

            output.writeByte(TYPE_BOOLEAN);
            output.writeBoolean((Boolean) value);

        } else {

            throw new IOException(
                    "Unsupported cache value type: "
                            + value.getClass().getName()
            );
        }
    }


    // Read a value using its type identifier
    private Object readValue(DataInputStream input)
            throws IOException {

        byte type = input.readByte();

        switch (type) {

            case TYPE_NULL:
                return null;

            case TYPE_STRING:
                return readString(input);

            case TYPE_INTEGER:
                return input.readInt();

            case TYPE_LONG:
                return input.readLong();

            case TYPE_DOUBLE:
                return input.readDouble();

            case TYPE_BOOLEAN:
                return input.readBoolean();

            default:
                throw new IOException(
                        "Unknown cache value type: " + type
                );
        }
    }


    // Holds the data loaded from a persistence file
    public static class CacheSnapshot {

        private final Map<String, Object> storage;
        private final Map<String, Long> expirationTimes;

        private CacheSnapshot(
                Map<String, Object> storage,
                Map<String, Long> expirationTimes
        ) {
            this.storage = new LinkedHashMap<>(storage);
            this.expirationTimes =
                    new LinkedHashMap<>(expirationTimes);
        }

        public Map<String, Object> getStorage() {
            return Collections.unmodifiableMap(storage);
        }

        public Map<String, Long> getExpirationTimes() {
            return Collections.unmodifiableMap(expirationTimes);
        }
    }
}
