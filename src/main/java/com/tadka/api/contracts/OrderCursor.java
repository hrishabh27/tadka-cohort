package com.tadka.api.contracts;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

public record OrderCursor(Instant createdAt, UUID id) {

    public String encode() {
        String raw = createdAt.toString() + "_" + id.toString();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static OrderCursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(cursor);
            String raw = new String(decodedBytes, StandardCharsets.UTF_8);
            int separatorIndex = raw.lastIndexOf('_');
            if (separatorIndex == -1) {
                throw new IllegalArgumentException("Malformed cursor");
            }
            Instant createdAt = Instant.parse(raw.substring(0, separatorIndex));
            UUID id = UUID.fromString(raw.substring(separatorIndex + 1));
            return new OrderCursor(createdAt, id);
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed cursor: " + cursor, e);
        }
    }
}
