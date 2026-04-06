package com.warmest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WarmestDataStructureTest {

    private WarmestDataStructure ds;

    @BeforeEach
    void setUp() {
        ds = new WarmestDataStructure();
    }

    @Test
    void testInitialGetWarmestIsNull() {
        assertNull(ds.getWarmest());
    }

    @Test
    void testPutNewKeyReturnsNull() {
        assertNull(ds.put("a", 100));
    }

    @Test
    void testGetWarmestAfterPut() {
        ds.put("a", 100);
        assertEquals("a", ds.getWarmest());
    }

    @Test
    void testPutExistingKeyReturnsPreviousValue() {
        ds.put("a", 100);
        assertEquals(100, ds.put("a", 101));
    }

    @Test
    void testPutSameValueReturnsCurrentValue() {
        ds.put("a", 100);
        ds.put("a", 101);
        assertEquals(101, ds.put("a", 101));
    }

    @Test
    void testGetReturnsCorrectValue() {
        ds.put("a", 100);
        ds.put("a", 101);
        assertEquals(101, ds.get("a"));
    }

    @Test
    void testGetWarmestAfterGet() {
        ds.put("a", 101);
        ds.get("a");
        assertEquals("a", ds.getWarmest());
    }

    @Test
    void testRemoveExistingKeyReturnsValue() {
        ds.put("a", 101);
        assertEquals(101, ds.remove("a"));
    }

    @Test
    void testRemoveAlreadyRemovedKeyReturnsNull() {
        ds.put("a", 101);
        ds.remove("a");
        assertNull(ds.remove("a"));
    }

    @Test
    void testGetWarmestAfterRemovingOnlyKey() {
        ds.put("a", 100);
        ds.remove("a");
        assertNull(ds.getWarmest());
    }

    @Test
    void testGetWarmestWithMultipleKeysIsLastPut() {
        ds.put("a", 100);
        ds.put("b", 200);
        ds.put("c", 300);
        assertEquals("c", ds.getWarmest());
    }

    @Test
    void testRemoveNonWarmestKeyDoesNotChangeWarmest() {
        ds.put("a", 100);
        ds.put("b", 200);
        ds.put("c", 300);
        ds.remove("b");
        assertEquals("c", ds.getWarmest());
    }

    @Test
    void testRemoveWarmestFallsBackToPreviousKey() {
        ds.put("a", 100);
        ds.put("b", 200);
        ds.put("c", 300);
        ds.remove("b");
        ds.remove("c");
        assertEquals("a", ds.getWarmest());
    }

    @Test
    void testFullSequence() {
        assertNull(ds.getWarmest());
        assertNull(ds.put("a", 100));
        assertEquals("a", ds.getWarmest());
        assertEquals(100, ds.put("a", 101));
        assertEquals(101, ds.put("a", 101));
        assertEquals(101, ds.get("a"));
        assertEquals("a", ds.getWarmest());
        assertEquals(101, ds.remove("a"));
        assertNull(ds.remove("a"));
        assertNull(ds.getWarmest());
        assertNull(ds.put("a", 100));
        assertNull(ds.put("b", 200));
        assertNull(ds.put("c", 300));
        assertEquals("c", ds.getWarmest());
        assertEquals(200, ds.remove("b"));
        assertEquals("c", ds.getWarmest());
        assertEquals(300, ds.remove("c"));
        assertEquals("a", ds.getWarmest());
        assertEquals(100, ds.remove("a"));
        assertNull(ds.getWarmest());
        assertNull(ds.remove("a"));
    }
}
