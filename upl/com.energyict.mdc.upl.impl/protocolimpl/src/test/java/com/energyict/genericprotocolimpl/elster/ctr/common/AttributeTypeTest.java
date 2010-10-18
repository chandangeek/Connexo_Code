package com.energyict.genericprotocolimpl.elster.ctr.common;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 1-okt-2010
 * Time: 15:22:12
 */
public class AttributeTypeTest {

    @Test
    public void testGetBytes() throws Exception {
        for (int i = 0; i < 0x10; i++) {
            byte[] array = {(byte) i};
            assertArrayEquals(array, new AttributeType().parse(array, 0).getBytes());
            assertArrayEquals(array, new AttributeType(i).getBytes());
        }
    }

    @Test
    public void testParse() throws Exception {
        byte[] allEnabled = {0x00};
        byte[] allDisabled = {0x0F};
        byte[] onlyQlf = {0x01};

        AttributeType type = new AttributeType().parse(allEnabled, 0);
        type.setHasAccessDescriptor(true);
        type.setHasDefaultValue(true);
        type.setHasQualifier(true);
        type.setHasValueFields(true);

        type = new AttributeType().parse(allDisabled, 0);
        type.setHasAccessDescriptor(false);
        type.setHasDefaultValue(false);
        type.setHasQualifier(false);
        type.setHasValueFields(false);

        type = new AttributeType().parse(onlyQlf, 0);
        type.setHasAccessDescriptor(false);
        type.setHasDefaultValue(false);
        type.setHasQualifier(true);
        type.setHasValueFields(false);

    }

    @Test
    public void testGetAttributeType() throws Exception {
        AttributeType type = new AttributeType();
        for (int i = 0; i <= 255; i++) {
            type.setAttributeType(i);
            assertEquals(0x00, type.getAttributeType() & 0xF0);
            assertEquals(i & 0x0F, type.getAttributeType());
        }
    }

    @Test
    public void testSetHasAccessDescriptor() throws Exception {
        AttributeType type = new AttributeType();
        type.setAttributeType(0x00);
        assertFalse(type.hasAccessDescriptor());

        type.setHasAccessDescriptor(true);
        type.setHasDefaultValue(false);
        type.setHasQualifier(false);
        type.setHasValueFields(false);
        assertTrue(type.hasAccessDescriptor());
        assertEquals(0x04, type.getAttributeType());

        type.setHasAccessDescriptor(false);
        type.setHasDefaultValue(true);
        type.setHasQualifier(true);
        type.setHasValueFields(true);
        assertFalse(type.hasAccessDescriptor());
        assertEquals(0x0B, type.getAttributeType());
    }

    @Test
    public void testSetHasDefaultValue() throws Exception {
        AttributeType type = new AttributeType();
        type.setAttributeType(0x00);
        assertFalse(type.hasDefaultValue());

        type.setHasDefaultValue(true);
        type.setHasAccessDescriptor(false);
        type.setHasQualifier(false);
        type.setHasValueFields(false);
        assertTrue(type.hasDefaultValue());
        assertEquals(0x08, type.getAttributeType());

        type.setHasDefaultValue(false);
        type.setHasAccessDescriptor(true);
        type.setHasQualifier(true);
        type.setHasValueFields(true);
        assertFalse(type.hasDefaultValue());
        assertEquals(0x07, type.getAttributeType());
    }

    @Test
    public void testSetHasQualifier() throws Exception {
        AttributeType type = new AttributeType();
        type.setAttributeType(0x00);
        assertFalse(type.hasQualifier());

        type.setHasQualifier(true);
        type.setHasDefaultValue(false);
        type.setHasAccessDescriptor(false);
        type.setHasValueFields(false);
        assertTrue(type.hasQualifier());
        assertEquals(0x01, type.getAttributeType());

        type.setHasQualifier(false);
        type.setHasDefaultValue(true);
        type.setHasAccessDescriptor(true);
        type.setHasValueFields(true);
        assertFalse(type.hasQualifier());
        assertEquals(0x0E, type.getAttributeType());
    }

    @Test
    public void testSetHasValueFields() throws Exception {
        AttributeType type = new AttributeType();
        type.setAttributeType(0x00);
        assertFalse(type.hasValueFields());

        type.setHasValueFields(true);
        type.setHasQualifier(false);
        type.setHasDefaultValue(false);
        type.setHasAccessDescriptor(false);
        assertTrue(type.hasValueFields());
        assertEquals(0x02, type.getAttributeType());

        type.setHasValueFields(false);
        type.setHasQualifier(true);
        type.setHasDefaultValue(true);
        type.setHasAccessDescriptor(true);
        assertFalse(type.hasValueFields());
        assertEquals(0x0D, type.getAttributeType());
    }

    @Test
    public void testSetHasIdentifier() throws Exception {
        assertFalse(new AttributeType().hasIdentifier());
        for (int i = 0; i < 255; i++) {
            AttributeType type = new AttributeType();
            type.setAttributeType(i);
            assertFalse(type.hasIdentifier());
            assertFalse(new AttributeType(i).hasIdentifier());
        }
    }

}
