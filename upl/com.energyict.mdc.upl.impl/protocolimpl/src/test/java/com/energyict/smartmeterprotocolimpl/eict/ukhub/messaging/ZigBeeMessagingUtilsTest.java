package com.energyict.smartmeterprotocolimpl.eict.ukhub.messaging;

import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 31/08/11
 * Time: 12:17
 */
public class ZigBeeMessagingUtilsTest {

    @Test
    public void testValidateAndFormatIeeeAddressIllegalCharacters() throws Exception {
        assertEquals("AB00CD11EF22FF01", ZigBeeMessagingUtils.validateAndFormatIeeeAddress(" &é\"'§\"!'  A b'0!è§0CD 1-1-poE F t2r 2 FF01"));
        assertEquals("AB00CD11EF22FF01", ZigBeeMessagingUtils.validateAndFormatIeeeAddress("AB00CD11EF22FF01"));
        assertEquals("AB00CD11EF22FF01", ZigBeeMessagingUtils.validateAndFormatIeeeAddress("$AB$00$CD$11$EF$22$FF$01"));
        assertEquals("AB00CD11EF22FF01", ZigBeeMessagingUtils.validateAndFormatIeeeAddress("AB-00-CD-11-EF-22-FF-01"));
        assertEquals("AB00CD11EF22FF01", ZigBeeMessagingUtils.validateAndFormatIeeeAddress("AB:00:CD:11:EF:22:FF:01"));
        assertEquals("AB00CD11EF22FF01", ZigBeeMessagingUtils.validateAndFormatIeeeAddress("AB 00 CD 11 EF 22 FF 01"));
    }

    @Test
    public void testValidateAndFormatIeeeAddressUpperCase() throws Exception {
        assertEquals("AB00CD11EF22FF01", ZigBeeMessagingUtils.validateAndFormatIeeeAddress("ab00cd11ef22ff01"));
        assertEquals("AB00CD11EF22FF01", ZigBeeMessagingUtils.validateAndFormatIeeeAddress("$ab$00$cd$11$ef$22$ff$01"));
        assertEquals("AB00CD11EF22FF01", ZigBeeMessagingUtils.validateAndFormatIeeeAddress("ab-00-cd-11-ef-22-ff-01"));
        assertEquals("AB00CD11EF22FF01", ZigBeeMessagingUtils.validateAndFormatIeeeAddress("ab:00:cd:11:ef:22:ff:01"));
        assertEquals("AB00CD11EF22FF01", ZigBeeMessagingUtils.validateAndFormatIeeeAddress("ab 00 cd 11 ef 22 ff 01"));
    }

    @Test
    public void testValidateAndFormatIeeeAddressIncorrectLength() throws Exception {
        try {
            ZigBeeMessagingUtils.validateAndFormatIeeeAddress("ab0cd11ef22ff01");
            fail("Expected an IOException, because the length is incorrect.");
        } catch (Exception e) {
            assertTrue(e instanceof IOException);
        }

        try {
            ZigBeeMessagingUtils.validateAndFormatIeeeAddress("ab00cd101ef22ff01");
            fail("Expected an IOException, because the length is incorrect.");
        } catch (Exception e) {
            assertTrue(e instanceof IOException);
        }
    }

    @Test
    public void testValidateAndFormatIeeeAddressNullRequired() throws Exception {
        try {
            ZigBeeMessagingUtils.validateAndFormatIeeeAddress(null);
            fail("Expected an IOException, because the address is required but was 'null'.");
        } catch (Exception e) {
            assertTrue(e instanceof IOException);
        }
    }

    @Test
    public void testValidateAndFormatIeeeAddressNull() throws Exception {
        assertNull(ZigBeeMessagingUtils.validateAndFormatIeeeAddress(null, false));
    }

}
