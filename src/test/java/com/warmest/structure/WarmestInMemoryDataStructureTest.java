package com.warmest.structure;

class WarmestInMemoryDataStructureTest extends WarmestDataStructureContractTest {

    @Override
    void setUp() {
        ds = new WarmestInMemoryDataStructure();
    }
}