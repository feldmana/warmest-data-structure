package com.warmest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("in-memory")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class WarmestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testRunIntegrationScenario() throws Exception {
        // getWarmest on empty → 204
        mockMvc.perform(get("/api/warmest"))
                .andExpect(status().isNoContent());

        // put("a", 100) → null (new key)
        mockMvc.perform(put("/api/a/100"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        // getWarmest → "a"
        mockMvc.perform(get("/api/warmest"))
                .andExpect(status().isOk())
                .andExpect(content().string("a"));

        // put("a", 101) → 100 (existing key)
        mockMvc.perform(put("/api/a/101"))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));

        // put("a", 101) again → 101
        mockMvc.perform(put("/api/a/101"))
                .andExpect(status().isOk())
                .andExpect(content().string("101"));

        // get("a") → 101
        mockMvc.perform(get("/api/a"))
                .andExpect(status().isOk())
                .andExpect(content().string("101"));

        // getWarmest → "a"
        mockMvc.perform(get("/api/warmest"))
                .andExpect(status().isOk())
                .andExpect(content().string("a"));

        // remove("a") → 101
        mockMvc.perform(delete("/api/a"))
                .andExpect(status().isOk())
                .andExpect(content().string("101"));

        // remove("a") again → 404
        mockMvc.perform(delete("/api/a"))
                .andExpect(status().isNotFound());

        // getWarmest on empty again → 204
        mockMvc.perform(get("/api/warmest"))
                .andExpect(status().isNoContent());

        // put a, b, c — all new keys → return empty (null)
        mockMvc.perform(put("/api/a/100")).andExpect(status().isOk()).andExpect(content().string(""));
        mockMvc.perform(put("/api/b/200")).andExpect(status().isOk()).andExpect(content().string(""));
        mockMvc.perform(put("/api/c/300")).andExpect(status().isOk()).andExpect(content().string(""));

        // getWarmest → "c"
        mockMvc.perform(get("/api/warmest"))
                .andExpect(status().isOk())
                .andExpect(content().string("c"));

        // remove("b") → 200
        mockMvc.perform(delete("/api/b"))
                .andExpect(status().isOk())
                .andExpect(content().string("200"));

        // getWarmest still "c"
        mockMvc.perform(get("/api/warmest"))
                .andExpect(status().isOk())
                .andExpect(content().string("c"));

        // remove("c") → 300
        mockMvc.perform(delete("/api/c"))
                .andExpect(status().isOk())
                .andExpect(content().string("300"));

        // getWarmest → "a"
        mockMvc.perform(get("/api/warmest"))
                .andExpect(status().isOk())
                .andExpect(content().string("a"));

        // remove("a") → 100
        mockMvc.perform(delete("/api/a"))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));

        // getWarmest → 204
        mockMvc.perform(get("/api/warmest"))
                .andExpect(status().isNoContent());

        // remove("a") → 404
        mockMvc.perform(delete("/api/a"))
                .andExpect(status().isNotFound());
    }
}
