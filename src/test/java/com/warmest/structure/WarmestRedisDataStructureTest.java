package com.warmest.structure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@SpringBootTest
@ActiveProfiles("redis")
@Testcontainers
class WarmestRedisDataStructureTest extends WarmestDataStructureContractTest {

    private static final String DATA_KEY = "warmest:data";
    private static final String ORDER_KEY = "warmest:order";

    // Testcontainers starts a real Redis in Docker — no manual Redis needed
    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Autowired
    private WarmestRedisDataStructure redisDs;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    void setUp() {
        redisTemplate.delete(List.of(DATA_KEY, ORDER_KEY));
        ds = redisDs;
    }
}
