package com.energyict.dlms.cosem;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jme
 */
public class DLMSClassIdTest {

    private static final int NON_EXISTING_CLASSID = 1234;

    /**
     * Test method for {@link com.energyict.dlms.cosem.DLMSClassId#getClassId()}.
     */
    @Test
    public final void testGetClassId() {
        DLMSClassId[] values = DLMSClassId.values();
        for (DLMSClassId dlmsClassIdToCheck : values) {
            for (DLMSClassId dlmsClassId : values) {
                if ((dlmsClassId.getClassId() == dlmsClassIdToCheck.getClassId()) && (!dlmsClassId.equals(dlmsClassIdToCheck))) {
                    String message = "The class ID number should be unique for each class type, but ";
                    message += dlmsClassId + " and " + dlmsClassIdToCheck;
                    message += " both have a value of " + dlmsClassId.getClassId();
                    fail(message);
                }
            }
        }
    }

    /**
     * Test method for {@link com.energyict.dlms.cosem.DLMSClassId#findById(int)}.
     */
    @Test
    public final void testfindById() {
        DLMSClassId[] values = DLMSClassId.values();
        for (DLMSClassId dlmsClassId : values) {
            assertEquals(dlmsClassId, DLMSClassId.findById(dlmsClassId.getClassId()));
        }

        DLMSClassId invalidClassID = DLMSClassId.findById(NON_EXISTING_CLASSID);
        assertNotNull(invalidClassID);
        assertEquals(DLMSClassId.UNKNOWN, invalidClassID);
        assertEquals(-1, invalidClassID.getClassId());
    }

}
