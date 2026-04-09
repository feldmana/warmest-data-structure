package com.warmest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WarmestConcurrentTest {

    public static final String PREFIX = "thread";
    public static final String API = "/api/";
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void cleanUp() throws Exception {
        // all tests use thread{i}_key{j} — cover max range (test 4 has 20 ops)
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 20; j++) {
                mockMvc.perform(delete(API + PREFIX + i + "_key" + j));
            }
        }
        // test 2: race_key
        mockMvc.perform(delete(API + "race_key"));
    }

    @Test
    void testShouldHandleMixedOperationsWhenFiveUsersConcurrent() throws Exception {
        int userCount = 5;
        int requestsEach = 10;

        ExecutorService executor = Executors.newFixedThreadPool(userCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(userCount);
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < userCount; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < requestsEach; j++) {
                        String key = PREFIX + userId + "_key" + j;
                        int value = userId * 100 + j;

                        mockMvc.perform(put(API + key + "/" + value))
                                .andExpect(status().isOk());

                        mockMvc.perform(get(API + key))
                                .andExpect(status().isOk());

                        mockMvc.perform(get(API + "warmest"))
                                .andExpect(status().is2xxSuccessful());

                        mockMvc.perform(delete(API + key))
                                .andExpect(status().isOk());
                    }
                } catch (Exception e) {
                    errors.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean finished = doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished);
        assertTrue(errors.isEmpty(), "Errors: " + errors);
    }


    @Test
    void testShouldNotCorruptDataWhenSameKeyRaceCondition() throws Exception {

        int threadCount = 10;
        String key = "race_key";

        List<Integer> values = Arrays.asList(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;

            executor.submit(() -> {
                try {
                    startLatch.await();

                    mockMvc.perform(put(API + key + "/" + values.get(idx)))
                            .andExpect(status().isOk());

                } catch (Exception e) {
                    errors.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean finished = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished);
        assertTrue(errors.isEmpty(), "Errors: " + errors);

        // FINAL VALUE MUST BE ONE OF THE INPUT VALUES (no corruption)
        MvcResult result = mockMvc.perform(get(API + key))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        int finalValue = Integer.parseInt(body);

        assertTrue(values.contains(finalValue),
                "Race condition corruption detected: " + finalValue);
    }

    @Test
    void testShouldRemainValidWhenConcurrentUpdates() throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(5);
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 5; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    for (int j = 0; j < 10; j++) {
                        mockMvc.perform(put(API + PREFIX + userId + "_key" + j + "/" + (userId * 10 + j)))
                                .andExpect(status().isOk());
                    }

                } catch (Exception e) {
                    errors.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean finished = doneLatch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished);
        assertTrue(errors.isEmpty(), "Errors: " + errors);

        // warmest must NOT crash and must return valid response
        mockMvc.perform(get(API + "warmest"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void testShouldNotLoseDataWhenConcurrentPuts() throws Exception {

        int threads = 5;
        int ops = 20;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        List<String> keys = Collections.synchronizedList(new ArrayList<>());
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threads; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < ops; j++) {
                        String k = PREFIX + userId + "_key" + j;
                        keys.add(k);
                        mockMvc.perform(put(API + k + "/" + j))
                                .andExpect(status().isOk());
                    }

                } catch (Exception e) {
                    errors.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean finished = doneLatch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished);
        assertTrue(errors.isEmpty(), "Errors: " + errors);

        //verify NO lost updates
        for (String k : keys) {
            try {
                mockMvc.perform(get(API + k))
                        .andExpect(status().isOk());
            } catch (Exception e) {
                throw new RuntimeException("Missing key: " + k, e);
            }
        }
    }
}
