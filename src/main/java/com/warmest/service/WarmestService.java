package com.warmest.service;

import com.warmest.structure.WarmestDataStructureInterface;
import org.springframework.stereotype.Service;

@Service
public class WarmestService {

    private final WarmestDataStructureInterface warmestStructure;

    public WarmestService(WarmestDataStructureInterface warmestStructure) {
        this.warmestStructure = warmestStructure;
    }

    public Integer put(String key, int value) {
        return warmestStructure.put(key, value);
    }

    public Integer get(String key) {
        return warmestStructure.get(key);
    }

    public Integer remove(String key) {
        return warmestStructure.remove(key);
    }

    public String getWarmest() {
        return warmestStructure.getWarmest();
    }
}
