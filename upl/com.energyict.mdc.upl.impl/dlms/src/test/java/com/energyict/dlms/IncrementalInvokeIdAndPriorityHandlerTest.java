package com.energyict.dlms;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author sva
 * @since 19/03/13 - 16:22
 */
public class IncrementalInvokeIdAndPriorityHandlerTest {

    @Test
    public void testIncrementingOfInvokeId() {
        IncrementalInvokeIdAndPriorityHandler iiapHandler = new IncrementalInvokeIdAndPriorityHandler((byte) 0xFD);  // All priority bits are set - these should not be modified when the invoke id is incremented

        assertEquals("InvokeIdAndPriority does not match.", (byte) 0xFD, iiapHandler.getCurrentInvokeIdAndPriority());
        assertEquals("InvokeIdAndPriority should be incremented.", (byte) 0xFE, iiapHandler.getNextInvokeIdAndPriority());
        assertEquals("InvokeIdAndPriority should be incremented.", (byte) 0xFF, iiapHandler.getNextInvokeIdAndPriority());
        assertEquals("InvokeIdAndPriority should be incremented.", (byte) 0xF0, iiapHandler.getNextInvokeIdAndPriority());
        assertEquals("InvokeIdAndPriority does not match.", (byte) 0xF0, iiapHandler.getCurrentInvokeIdAndPriority());
    }

    @Test
    public void validateInvokeIdTest() {
        IncrementalInvokeIdAndPriorityHandler iiapHandler = new IncrementalInvokeIdAndPriorityHandler((byte) 0xFD);

        assertTrue(iiapHandler.validateInvokeId((byte) 0x05, (byte) 0x05));
        assertTrue(iiapHandler.validateInvokeId((byte) 0xF5, (byte) 0x05)); // Only the 4 invoke-id bits should match - the priority bits may be different
        assertTrue(iiapHandler.validateInvokeId((byte) 0x45, (byte) 0x25));
        assertFalse(iiapHandler.validateInvokeId((byte) 0x05, (byte) 0x04));
    }
}