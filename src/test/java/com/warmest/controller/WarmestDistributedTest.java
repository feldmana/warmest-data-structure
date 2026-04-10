package com.warmest.controller;

import com.warmest.WarmestApplication;
import org.junit.jupiter.api.*;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Proves that 3 independent app instances sharing one Redis
 * all see the same data in real time.
 * Redis is started in Docker via Testcontainers.
 * The 3 Spring Boot instances run in-process on ports 8080-8082.
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WarmestDistributedTest {

    private static final List<ConfigurableApplicationContext> instances = new ArrayList<>();
    private static final List<String> urls = new ArrayList<>();      // base URL per instance
    // RestTemplate that never throws on 4xx/5xx — lets us assert status ourselves
    private static final RestTemplate http = buildHttp();
    // One Redis container shared by all 3 app instances
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @BeforeAll
    static void startThreeInstances() {
        String host = redis.getHost();
        int port = redis.getMappedPort(6379);

        for (int appPort : new int[]{8080, 8081, 8082}) {
            instances.add(
                    new SpringApplicationBuilder(WarmestApplication.class)
                            .run(
                                    "--server.port=" + appPort,
                                    "--spring.profiles.active=redis",
                                    "--spring.data.redis.host=" + host,
                                    "--spring.data.redis.port=" + port,
                                    "--spring.main.banner-mode=off",
                                    "--logging.level.root=WARN"
                            )
            );
            urls.add("http://localhost:" + appPort + "/api");
        }
    }

    @AfterAll
    static void stopAllInstances() {
        instances.forEach(ConfigurableApplicationContext::close);
        instances.clear();
        urls.clear();
    }

    private static RestTemplate buildHttp() {
        RestTemplate rt = new RestTemplate();
        rt.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(HttpStatusCode statusCode) {
                return false;  // let tests assert status themselves
            }
        });
        return rt;
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @BeforeEach
    void cleanRedis() throws Exception {
        // cleab the shared Redis so every test starts with a blank slate
        redis.execInContainer("redis-cli", "FLUSHALL");
    }

    /**
     * PUT on instance 1 → GET on instances 2 and 3 both return the same value.
     */
    @Test
    void testShouldSharePutAcrossAllInstances() {
        http.put(urls.get(0) + "/key1/100", null);         // write on :8080

        ResponseEntity<Integer> from2 = http.getForEntity(urls.get(1) + "/key1", Integer.class);
        ResponseEntity<Integer> from3 = http.getForEntity(urls.get(2) + "/key1", Integer.class);

        assertEquals(HttpStatus.OK, from2.getStatusCode());
        assertEquals(100, from2.getBody());
        assertEquals(HttpStatus.OK, from3.getStatusCode());
        assertEquals(100, from3.getBody());
    }

    /**
     * Writes from 3 different instances → all 3 agree on who is warmest.
     */
    @Test
    void testShouldShareWarmestAcrossAllInstances() throws InterruptedException {
        http.put(urls.get(0) + "/key1/100", null);   // instance 1 writes
        Thread.sleep(5);
        http.put(urls.get(1) + "/key2/200", null);   // instance 2 writes
        Thread.sleep(5);
        http.put(urls.get(2) + "/key3/300", null);   // instance 3 writes last

        ResponseEntity<String> w1 = http.getForEntity(urls.get(0) + "/warmest", String.class);
        ResponseEntity<String> w2 = http.getForEntity(urls.get(1) + "/warmest", String.class);
        ResponseEntity<String> w3 = http.getForEntity(urls.get(2) + "/warmest", String.class);

        assertEquals(HttpStatus.OK, w1.getStatusCode());
        assertEquals("key3", w1.getBody());   // instance 1 sees key3
        assertEquals("key3", w2.getBody());   // instance 2 sees key3
        assertEquals("key3", w3.getBody());   // instance 3 sees key3
    }

    /**
     * DELETE on instance 2 → GET on instance 3 returns 404.
     */
    @Test
    void testShouldShareRemoveAcrossAllInstances() {
        http.put(urls.get(0) + "/key1/100", null);    // write on :8080
        http.delete(urls.get(1) + "/key1");           // delete on :8081

        ResponseEntity<Integer> resp = http.getForEntity(urls.get(2) + "/key1", Integer.class);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());   // visible on :8082
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * GET on one instance promotes that key to warmest — all instances see the change.
     */
    @Test
    void testShouldUpdateWarmestOnGetAcrossAllInstances() throws InterruptedException {
        http.put(urls.get(0) + "/a/1", null);
        Thread.sleep(5);
        http.put(urls.get(1) + "/b/2", null);
        Thread.sleep(5);

        // GET "a" on instance 3 — promotes "a" back to warmest
        http.getForEntity(urls.get(2) + "/a", Integer.class);

        ResponseEntity<String> w1 = http.getForEntity(urls.get(0) + "/warmest", String.class);
        ResponseEntity<String> w2 = http.getForEntity(urls.get(1) + "/warmest", String.class);

        assertEquals("a", w1.getBody());
        assertEquals("a", w2.getBody());
    }
}

