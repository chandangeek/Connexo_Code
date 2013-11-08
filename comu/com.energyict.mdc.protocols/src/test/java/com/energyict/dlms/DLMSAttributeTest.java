package com.energyict.dlms;

import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 30-dec-2010
 * Time: 9:44:15
 */
public class DLMSAttributeTest {

    private static final ObisCode OBIS = ObisCode.fromString("1.1.1.8.0.255");
    private static final UniversalObject UNIVERSAL_OBJECT = new UniversalObject(OBIS.getLN(), 1, 0);

    @Test
    public void testFromString() throws Exception {
        assertEquals(1, DLMSAttribute.fromString("1:1.1.1.8.0.255:3").getClassId());
        assertEquals(DLMSClassId.DATA, DLMSAttribute.fromString("1:1.1.1.8.0.255:3").getDLMSClassId());
        assertEquals(OBIS, DLMSAttribute.fromString("1:1.1.1.8.0.255:3").getObisCode());
        assertEquals(3, DLMSAttribute.fromString("1:1.1.1.8.0.255:3").getAttribute());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingClassIdFromString() throws Exception {
        DLMSAttribute.fromString("1.1.1.8.0.255:3");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongObisCodeIdFromString() throws Exception {
        DLMSAttribute.fromString("1:1.1.8.0.255:3");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingObisCodeIdFromString() throws Exception {
        DLMSAttribute.fromString("1:3");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHalfMissingAttributeFromString() throws Exception {
        DLMSAttribute.fromString("1:1.1.1.8.0.255:");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingAttributeFromString() throws Exception {
        DLMSAttribute.fromString("1:1.1.1.8.0.255");
    }

    @Test
    public void testFromUniversalObject() throws Exception {
        assertEquals(OBIS, DLMSAttribute.fromUniversalObject(UNIVERSAL_OBJECT, 5).getObisCode());
        assertEquals(1, DLMSAttribute.fromUniversalObject(UNIVERSAL_OBJECT, 5).getClassId());
        assertEquals(DLMSClassId.DATA, DLMSAttribute.fromUniversalObject(UNIVERSAL_OBJECT, 5).getDLMSClassId());
        assertEquals(5, DLMSAttribute.fromUniversalObject(UNIVERSAL_OBJECT, 5).getAttribute());
    }

    @Test
    public void testGetListOfAttributes() throws Exception {
        List<DLMSAttribute> listOfAttributes = DLMSAttribute.getListOfAttributes("1:1.1.1.8.0.255:3", "2:1.1.1.8.1.255:3", "3:1.1.1.8.2.255:3");
        assertNotNull(listOfAttributes);
        assertEquals(3, listOfAttributes.size());
        for (DLMSAttribute attribute : listOfAttributes) {
            assertNotNull(attribute);
        }
    }

    @Test
    public void testToString() throws Exception {
        DLMSAttribute attribute = DLMSAttribute.fromString("7:1.1.1.8.0.255:3333");
        assertNotNull(attribute);
        String tostringValue = attribute.toString();
        assertNotNull(tostringValue);
        assertTrue(tostringValue.contains(OBIS.toString()));
        assertTrue(tostringValue.contains("7"));
        assertTrue(tostringValue.contains(DLMSClassId.getDescription(7)));
        assertTrue(tostringValue.contains("3333"));
    }
}
