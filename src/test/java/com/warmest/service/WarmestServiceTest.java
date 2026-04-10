package com.warmest.service;

import com.warmest.structure.WarmestInMemoryDataStructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WarmestServiceTest {

    private WarmestService service;

    @BeforeEach
    void setUp() {
        WarmestInMemoryDataStructure ds = new WarmestInMemoryDataStructure();
        service = new WarmestService(ds);
    }

    @Test
    void testShouldReturnNullWhenPuttingNewKey() {
        assertNull(service.put("a", 100));
    }

    @Test
    void testShouldReturnOldValueWhenUpdatingExistingKey() {
        service.put("a", 100);
        assertEquals(100, service.put("a", 200));
    }

    @Test
    void testShouldReturnValueWhenGettingExistingKey() {
        service.put("a", 100);
        assertEquals(100, service.get("a"));
    }

    @Test
    void testShouldReturnNullWhenGettingNonExistentKey() {
        assertNull(service.get("nonexistent"));
    }

    @Test
    void testShouldReturnValueWhenRemovingExistingKey() {
        service.put("a", 100);
        assertEquals(100, service.remove("a"));
    }

    @Test
    void testShouldReturnNullWhenRemovingNonExistentKey() {
        assertNull(service.remove("nonexistent"));
    }

    @Test
    void testShouldReturnNullWhenStructureIsEmpty() {
        assertNull(service.getWarmest());
    }

    @Test
    void testShouldReturnKeyWhenPutSingleKey() {
        service.put("a", 100);
        assertEquals("a", service.getWarmest());
    }

    @Test
    void testShouldReturnKeyWhenKeyIsAccessedViaGet() {
        service.put("a", 100);
        service.put("b", 200);
        service.get("a");
        assertEquals("a", service.getWarmest());
    }

    @Test
    void testShouldReturnNullWhenLastKeyIsRemoved() {
        service.put("a", 100);
        service.remove("a");
        assertNull(service.getWarmest());
    }

    @Test
    void testShouldNotLoseDataWhenMultipleThreadsWriteConcurrently() throws InterruptedException {
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    service.put("key_" + threadId + "_" + j, threadId * 1000 + j);
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        int count = 0;

        for (int i = 0; i < threadCount; i++) {
            for (int j = 0; j < 100; j++) {
                if (service.get("key_" + i + "_" + j) != null) {
                    count++;
                }
            }
        }

        assertEquals(500, count, "Some keys were lost due to race condition");
    }
}