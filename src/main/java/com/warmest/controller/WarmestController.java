package com.warmest.controller;

import com.warmest.service.WarmestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class WarmestController {

    private static final Logger log = LoggerFactory.getLogger(WarmestController.class);

    private final WarmestService warmestService;

    public WarmestController(WarmestService warmestService) {
        this.warmestService = warmestService;
    }

    @PutMapping("/{key}/{value}")
    public ResponseEntity<Integer> put(@PathVariable String key, @PathVariable int value) {
        Integer previous = warmestService.put(key, value);
        log.debug("PUT key={} value={} and previousValue={}", key, value, previous);
        return ResponseEntity.ok(previous);
    }

    @GetMapping("/{key}")
    public ResponseEntity<Integer> get(@PathVariable String key) {
        Integer value = warmestService.get(key);
        log.debug("GET key={} value is {}", key, value != null ? value : "NOT FOUND");
        return value != null ? ResponseEntity.ok(value)
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Integer> remove(@PathVariable String key) {
        Integer value = warmestService.remove(key);
        log.debug("DELETE key={} is {}", key, value != null ? value : "NOT FOUND");
        return value != null ? ResponseEntity.ok(value)
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/warmest")
    public ResponseEntity<String> getWarmest() {
        String warmest = warmestService.getWarmest();
        log.debug("WARMEST is {}", warmest != null ? warmest : "EMPTY");
        return warmest != null ? ResponseEntity.ok(warmest)
                : ResponseEntity.noContent().build();
    }
}
