package com.example.demo.shared.infrastructure.out.repository.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class RedisCachedTemplate {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisCacheProperties cacheProperties;
    private final String strTtlInMillis;

    public RedisCachedTemplate(
            RedisTemplate<String, Object> redisTemplate,
            RedisCacheProperties cacheProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.cacheProperties = cacheProperties;
        this.strTtlInMillis = String.valueOf(cacheProperties.timeToLive().toMillis());
    }

    public void setForValue(String key, Object value) {
        redisTemplate.opsForValue().set(
                key,
                value,
                cacheProperties.timeToLive()
        );
    }

    public Object getForValue(String key) {
        return redisTemplate.opsForValue().get(
                key
        );
    }

    public Long addPagesKeyData(String redisPagesKey, String redisDataKey, Object data) {
        if (data == null && !cacheProperties.cacheNullValues()) {
            return 0L;
        }
        String luaScript = STR."""
                                redis.call('SET', KEYS[1], ARGV[1], 'PX', \{strTtlInMillis})

                                local exists = redis.call('EXISTS', KEYS[2])
                                redis.call('SADD', KEYS[2], KEYS[1])

                                if exists == 0 then
                                    redis.call('PEXPIRE', KEYS[2], \{strTtlInMillis})
                                end

                                return 1
                                """;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);
        return execute(script,
                List.of(redisDataKey, redisPagesKey),
                data);
    }

    public Long invalidateByPagesKey(String redisPagesKey) {
        String luaScript = """
                local cursor = "0"
                local deleted = 0
                local batchSize = 500
                
                repeat
                    local result = redis.call('SSCAN', KEYS[1], cursor, 'COUNT', batchSize)
                    cursor = result[1]
                    local keys = result[2]
                
                    if #keys > 0 then
                        deleted = deleted + redis.call('UNLINK', unpack(keys))
                    end
                until cursor == "0"
                
                deleted = deleted + redis.call('UNLINK', KEYS[1])
                
                return deleted
                """;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);
        return execute(script, Collections.singletonList(redisPagesKey));
    }

    private <T> T execute(RedisScript<T> script, List<String> keys, Object... args) {
        return redisTemplate.
                execute(script,
                        keys,
                        args);
    }

    public Long addForSet(String key, Object value) {
        if (value == null && !cacheProperties.cacheNullValues()) {
            return 0L;
        }

        return redisTemplate.opsForSet().add(
                key,
                value
        );
    }

    public Set<Object> getForSet(String key) {
        return redisTemplate.opsForSet().members(
                key
        );
    }

    public Boolean deleteKey(String key) {
        return redisTemplate.delete(key);
    }

    public Long deleteKeys(Set<String> keys) {
        return redisTemplate.delete(keys);
    }
}

