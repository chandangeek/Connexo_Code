package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 31/01/12
 * Time: 16:16
 */
public class GenericDlmsClassAttributeTest {

    @Test
    public void testGetAttributeNumber() throws Exception {
        for (int attributeNumber = 0; attributeNumber < 10; attributeNumber++) {
            assertEquals(attributeNumber, new GenericDlmsClassAttribute(0, DLMSClassId.REGISTER, attributeNumber).getAttributeNumber());
            assertEquals(attributeNumber, new GenericDlmsClassAttribute(0, new DLMSAttribute("1.0.8.0.0.255", attributeNumber, DLMSClassId.REGISTER)).getAttributeNumber());
        }
    }


    @Test
    public void testGetDLMSAttribute() throws Exception {
        for (DLMSClassId classId : DLMSClassId.values()) {
            DLMSAttribute expected = new DLMSAttribute("1.0.8.0.0.255", 1, classId);
            assertEquals(expected, new GenericDlmsClassAttribute(0, classId, 1).getDLMSAttribute(ObisCode.fromString("1.0.8.0.0.255")));
            assertEquals(expected, new GenericDlmsClassAttribute(0, new DLMSAttribute("0.0.8.0.0.255", 1, classId)).getDLMSAttribute(ObisCode.fromString("1.0.8.0.0.255")));
        }
    }

    @Test
    public void testGetDlmsClassId() throws Exception {
        for (DLMSClassId classId : DLMSClassId.values()) {
            DLMSAttribute attribute = new DLMSAttribute("1.0.8.0.0.255", 1, classId);
            assertEquals(classId, new GenericDlmsClassAttribute(0, classId, 1).getDlmsClassId());
            assertEquals(classId, new GenericDlmsClassAttribute(0, attribute).getDlmsClassId());
        }
    }

    @Test
    public void testGetShortName() throws Exception {
        for (int shortName = 0; shortName < 10; shortName++) {
            assertEquals(shortName, new GenericDlmsClassAttribute(shortName, DLMSClassId.REGISTER, 0).getShortName());
            assertEquals(shortName, new GenericDlmsClassAttribute(shortName, new DLMSAttribute("1.0.8.0.0.255", 0, DLMSClassId.REGISTER)).getShortName());
        }
    }
}
