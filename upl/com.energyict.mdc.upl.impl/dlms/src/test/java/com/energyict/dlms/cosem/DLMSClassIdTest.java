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

    @Test
    public void testIsTypeX() throws Exception {
        assertTrue(DLMSClassId.REGISTER.isRegister());
        assertTrue(DLMSClassId.EXTENDED_REGISTER.isExtendedRegister());
        assertTrue(DLMSClassId.DEMAND_REGISTER.isDemandRegister());
        assertTrue(DLMSClassId.CLOCK.isClock());
        assertTrue(DLMSClassId.DATA.isData());
        assertTrue(DLMSClassId.PROFILE_GENERIC.isProfileGeneric());

        assertFalse(DLMSClassId.REGISTER.isExtendedRegister());
        assertFalse(DLMSClassId.REGISTER.isDemandRegister());
        assertFalse(DLMSClassId.REGISTER.isClock());
        assertFalse(DLMSClassId.REGISTER.isData());
        assertFalse(DLMSClassId.REGISTER.isProfileGeneric());

        assertFalse(DLMSClassId.EXTENDED_REGISTER.isRegister());
        assertFalse(DLMSClassId.EXTENDED_REGISTER.isDemandRegister());
        assertFalse(DLMSClassId.EXTENDED_REGISTER.isClock());
        assertFalse(DLMSClassId.EXTENDED_REGISTER.isData());
        assertFalse(DLMSClassId.EXTENDED_REGISTER.isProfileGeneric());

        assertFalse(DLMSClassId.DEMAND_REGISTER.isRegister());
        assertFalse(DLMSClassId.DEMAND_REGISTER.isExtendedRegister());
        assertFalse(DLMSClassId.DEMAND_REGISTER.isClock());
        assertFalse(DLMSClassId.DEMAND_REGISTER.isData());
        assertFalse(DLMSClassId.DEMAND_REGISTER.isProfileGeneric());

        assertFalse(DLMSClassId.CLOCK.isRegister());
        assertFalse(DLMSClassId.CLOCK.isExtendedRegister());
        assertFalse(DLMSClassId.CLOCK.isDemandRegister());
        assertFalse(DLMSClassId.CLOCK.isData());
        assertFalse(DLMSClassId.CLOCK.isProfileGeneric());

        assertFalse(DLMSClassId.DATA.isRegister());
        assertFalse(DLMSClassId.DATA.isExtendedRegister());
        assertFalse(DLMSClassId.DATA.isDemandRegister());
        assertFalse(DLMSClassId.DATA.isClock());
        assertFalse(DLMSClassId.DATA.isProfileGeneric());


        assertFalse(DLMSClassId.PROFILE_GENERIC.isRegister());
        assertFalse(DLMSClassId.PROFILE_GENERIC.isExtendedRegister());
        assertFalse(DLMSClassId.PROFILE_GENERIC.isDemandRegister());
        assertFalse(DLMSClassId.PROFILE_GENERIC.isClock());
        assertFalse(DLMSClassId.PROFILE_GENERIC.isData());

    }
}
