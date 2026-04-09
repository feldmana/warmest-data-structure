package com.warmest.controller;

import com.warmest.service.WarmestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class WarmestController {

    private final WarmestService warmestService;

    public WarmestController(WarmestService warmestService) {
        this.warmestService = warmestService;
    }

    @PutMapping("/{key}/{value}")
    public ResponseEntity<Integer> put(@PathVariable String key, @PathVariable int value) {
        return ResponseEntity.ok(warmestService.put(key, value));
    }

    @GetMapping("/{key}")
    public ResponseEntity<Integer> get(@PathVariable String key) {
        Integer value = warmestService.get(key);
        return value != null ? ResponseEntity.ok(value)
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Integer> remove(@PathVariable String key) {
        Integer value = warmestService.remove(key);
        return value != null ? ResponseEntity.ok(value)
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/warmest")
    public ResponseEntity<String> getWarmest() {
        String warmest = warmestService.getWarmest();
        return warmest != null ? ResponseEntity.ok(warmest)
                : ResponseEntity.noContent().build();
    }
}
