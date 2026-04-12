package com.warmest.exception;

import com.warmest.controller.WarmestController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WarmestController.class)
@ContextConfiguration(classes = {WarmestController.class, GlobalExceptionHandler.class})
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private com.warmest.service.WarmestService warmestService;


    @Test
    void testShouldReturn400WhenValueIsNotANumber() throws Exception {
        String expected = String.format(
                ErrorMessages.INVALID_INPUT_TYPE_MISMATCH,
                "ddd"
        );
        mockMvc.perform(put("/api/a/ddd"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(expected));
    }

    @Test
    void testShouldReturn405WhenWrongHttpMethod() throws Exception {
        String expected = String.format(
                ErrorMessages.METHOD_NOT_SUPPORTED,
                "POST"
        );
        mockMvc.perform(post("/api/a/100"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").value(expected));
    }
}
