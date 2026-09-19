package com.example.demo.shared.application.port.in;

import com.example.demo.shared.domain.exception.InvalidSearchTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;

import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public record Page(String token, int size) implements Serializable {

    public record Keys(
            String timeKey,
            String idKey
    ) {
        public static final Keys CREATED_AT_AND_ID = new Keys("createdAt", "id");
    }

    public record Params(Instant time, UUID id) implements Serializable {
    }

    public KeysetScrollPosition parseTimeAndIdScrollPosition(Keys keys) {
        Params params = tokenToTimeAndId();
        return params == null ? ScrollPosition.keyset() : tokenToTimeAndIdScrollPosition(keys, params);
    }

    private KeysetScrollPosition tokenToTimeAndIdScrollPosition(Keys keys, Params params) {
        try {
            Map<String, Object> compositeKeysMap = new LinkedHashMap<>();
            compositeKeysMap.put(keys.timeKey, params.time);
            compositeKeysMap.put(keys.idKey, params.id);
            return ScrollPosition.forward(compositeKeysMap);
        } catch (Exception e) {
            throw new InvalidSearchTokenException("Invalid token.");
        }
    }

    public Params tokenToTimeAndId() {
        try {
            if (token == null || token.isBlank()) {
                return null;
            }
            String[] parts = token.split("_");
            Instant time = Instant.parse(parts[0]);
            UUID id = UUID.fromString(parts[1]);
            return new Params(time, id);
        } catch (Exception e) {
            throw new InvalidSearchTokenException("Invalid token.");
        }
    }
}

