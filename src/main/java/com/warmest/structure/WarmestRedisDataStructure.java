package com.warmest.structure;


import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Profile("redis")
public class WarmestRedisDataStructure implements WarmestDataStructureInterface {

    private static final String DATA_KEY = "warmest:data";
    private static final String ORDER_KEY = "warmest:order";
    private final RedisTemplate<String, Object> redisTemplate;

    public WarmestRedisDataStructure(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Integer put(String key, int value) {
        Integer returnValue = null;
        Object prev = redisTemplate.opsForHash().get(DATA_KEY, key);
        if (prev != null) {
            returnValue = Integer.parseInt(prev.toString());
        }

        redisTemplate.opsForHash().put(DATA_KEY, key, value);
        redisTemplate.opsForZSet().add(ORDER_KEY, key, System.currentTimeMillis());
        return returnValue;
    }

    @Override
    public Integer remove(String key) {
        Object prev = redisTemplate.opsForHash().get(DATA_KEY, key);
        if (prev == null) {
            return null;
        }
        redisTemplate.opsForHash().delete(DATA_KEY, key);
        redisTemplate.opsForZSet().remove(ORDER_KEY, key);
        return Integer.parseInt(prev.toString());
    }

    @Override
    public Integer get(String key) {
        Object prev = redisTemplate.opsForHash().get(DATA_KEY, key);
        if (prev == null) {
            return null;
        }
        redisTemplate.opsForZSet().add(ORDER_KEY, key, System.currentTimeMillis());
        return Integer.parseInt(prev.toString());
    }

    @Override
    public String getWarmest() {
        Set<Object> result = redisTemplate.opsForZSet().reverseRange(ORDER_KEY, 0, 0);
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.iterator().next().toString();
    }
}
