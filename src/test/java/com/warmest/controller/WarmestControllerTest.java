package com.warmest.controller;

import com.warmest.service.WarmestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(WarmestController.class)
class WarmestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WarmestService warmestService;

    @Test
    void testShouldReturnEmptyWhenPuttingNewKey() throws Exception {
        when(warmestService.put("a", 100)).thenReturn(null);

        mockMvc.perform(put("/api/a/100"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void testShouldReturnOldValueWhenUpdatingExistingKey() throws Exception {
        when(warmestService.put("a", 101)).thenReturn(100);

        mockMvc.perform(put("/api/a/101"))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));
    }

    @Test
    void testShouldReturnValueWhenGettingExistingKey() throws Exception {
        when(warmestService.get("a")).thenReturn(100);

        mockMvc.perform(get("/api/a"))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));
    }

    @Test
    void testShouldReturn404WhenGettingMissingKey() throws Exception {
        when(warmestService.get("x")).thenReturn(null);

        mockMvc.perform(get("/api/x"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testShouldReturnValueWhenRemovingExistingKey() throws Exception {
        when(warmestService.remove("a")).thenReturn(100);

        mockMvc.perform(delete("/api/a"))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));
    }

    @Test
    void testShouldReturn404WhenRemovingMissingKey() throws Exception {
        when(warmestService.remove("x")).thenReturn(null);

        mockMvc.perform(delete("/api/x"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testShouldReturnKeyWhenGettingWarmest() throws Exception {
        when(warmestService.getWarmest()).thenReturn("a");

        mockMvc.perform(get("/api/warmest"))
                .andExpect(status().isOk())
                .andExpect(content().string("a"));
    }

    @Test
    void testShouldReturn204WhenWarmestIsEmpty() throws Exception {
        when(warmestService.getWarmest()).thenReturn(null);

        mockMvc.perform(get("/api/warmest"))
                .andExpect(status().isNoContent());
    }
}