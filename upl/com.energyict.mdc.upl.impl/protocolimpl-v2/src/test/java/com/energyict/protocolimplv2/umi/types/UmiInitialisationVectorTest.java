package com.energyict.protocolimplv2.umi.types;

import com.energyict.protocolimplv2.umi.util.Limits;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class UmiInitialisationVectorTest {
    @Test
    public void createUmiInitialisationVector() {
        UmiId id = new UmiId("12345");
        UmiInitialisationVector iv = new UmiInitialisationVector(id);
        assertEquals(0, iv.getCounter()[0]);
        assertEquals(id, iv.getCommanderId());

        iv.increment();
        assertEquals(1, iv.getCounter()[0]);
    }

    @Test
    public void recreateUmiInitialisationVectorFromRaw() {
        UmiId id = new UmiId("12345");
        UmiInitialisationVector iv = new UmiInitialisationVector(id);
        UmiInitialisationVector iv1 = new UmiInitialisationVector(iv.getRaw());

        assertArrayEquals(iv.getCounter(), iv1.getCounter());
        assertEquals(iv.getCommanderId(), iv1.getCommanderId());
    }

    @Test
    @Ignore // ignore it as it takes a lot of time
    public void counterTest() {
        UmiInitialisationVector iv = new UmiInitialisationVector(new UmiId("12345"));
        long incrementCount = Limits.MAX_UNSIGNED_INT;
        for (long increment = 0; increment < incrementCount; ++increment) {
            assertEquals(increment, iv.getCounterAsNumber());
            assertTrue(iv.increment());
        }
        assertFalse(iv.increment());
    }
} 
