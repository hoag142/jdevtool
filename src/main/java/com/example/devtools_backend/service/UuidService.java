package com.example.devtools_backend.service;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UuidService {

    private final TimeBasedEpochGenerator uuidV7Generator = Generators.timeBasedEpochGenerator();

    public Map<String, Object> generateUuidV4(int count) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (count < 1 || count > 100) {
                result.put("error", "Count must be between 1 and 100");
                result.put("success", false);
                return result;
            }

            java.util.List<String> uuids = new java.util.ArrayList<>();
            for (int i = 0; i < count; i++) {
                uuids.add(UUID.randomUUID().toString());
            }

            result.put("uuids", uuids);
            result.put("count", count);
            result.put("version", "4");
            result.put("description", "Random UUID (version 4)");
            result.put("success", true);

        } catch (Exception e) {
            result.put("error", "Failed to generate UUID v4: " + e.getMessage());
            result.put("success", false);
        }

        return result;
    }

    public Map<String, Object> generateUuidV7(int count) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (count < 1 || count > 100) {
                result.put("error", "Count must be between 1 and 100");
                result.put("success", false);
                return result;
            }

            java.util.List<String> uuids = new java.util.ArrayList<>();
            for (int i = 0; i < count; i++) {
                UUID uuid = uuidV7Generator.generate();
                uuids.add(uuid.toString());
            }

            result.put("uuids", uuids);
            result.put("count", count);
            result.put("version", "7");
            result.put("description", "Time-ordered UUID (version 7) - sortable by timestamp");
            result.put("timestamp", Instant.now().toString());
            result.put("success", true);

        } catch (Exception e) {
            result.put("error", "Failed to generate UUID v7: " + e.getMessage());
            result.put("success", false);
        }

        return result;
    }

    public Map<String, Object> parseUuid(String uuidString) {
        Map<String, Object> result = new HashMap<>();

        try {
            UUID uuid = UUID.fromString(uuidString.trim());

            result.put("uuid", uuid.toString());
            result.put("version", uuid.version());
            result.put("variant", uuid.variant());

            // UUID components
            result.put("mostSigBits", String.format("0x%016X", uuid.getMostSignificantBits()));
            result.put("leastSigBits", String.format("0x%016X", uuid.getLeastSignificantBits()));

            // Version-specific info
            if (uuid.version() == 4) {
                result.put("type", "Random UUID (version 4)");
            } else if (uuid.version() == 7) {
                result.put("type", "Time-ordered UUID (version 7)");
            } else if (uuid.version() == 1) {
                result.put("type", "Time-based UUID (version 1)");
                result.put("timestamp", uuid.timestamp());
            } else {
                result.put("type", "UUID version " + uuid.version());
            }

            result.put("success", true);

        } catch (IllegalArgumentException e) {
            result.put("error", "Invalid UUID format: " + e.getMessage());
            result.put("success", false);
        } catch (Exception e) {
            result.put("error", "Failed to parse UUID: " + e.getMessage());
            result.put("success", false);
        }

        return result;
    }
}
