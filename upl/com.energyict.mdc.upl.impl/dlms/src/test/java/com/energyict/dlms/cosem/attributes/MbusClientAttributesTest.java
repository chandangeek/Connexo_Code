package com.energyict.dlms.cosem.attributes;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 25-nov-2010
 * Time: 15:47:17
 */
public class MbusClientAttributesTest {

    private static final int version9 = 9;
    private static final int version10 = 10;

    @Test
    public final void forVersionTest() {
        assertEquals(0x00, MbusClientAttributes.LOGICAL_NAME.forVersion(version9).getShortName());
        assertEquals(0x10, MbusClientAttributes.MBUS_PORT_REFERENCE.forVersion(version9).getShortName());
        assertEquals(0x18, MbusClientAttributes.CAPTURE_DEFINITION.forVersion(version9).getShortName());
        assertEquals(0x20, MbusClientAttributes.CAPTURE_PERIOD.forVersion(version9).getShortName());
        assertEquals(0x28, MbusClientAttributes.PRIMARY_ADDRESS.forVersion(version9).getShortName());
        assertEquals(0x30, MbusClientAttributes.IDENTIFICATION_NUMBER.forVersion(version9).getShortName());
        assertEquals(0x38, MbusClientAttributes.MANUFACTURER_ID.forVersion(version9).getShortName());
        assertEquals(0x40, MbusClientAttributes.VERSION.forVersion(version9).getShortName());
        assertEquals(0x48, MbusClientAttributes.DEVICE_TYPE.forVersion(version9).getShortName());
        assertEquals(0x50, MbusClientAttributes.ACCESS_NUMBER.forVersion(version9).getShortName());
        assertEquals(0x58, MbusClientAttributes.STATUS.forVersion(version9).getShortName());
        assertEquals(0x60, MbusClientAttributes.ALARM.forVersion(version9).getShortName());

        assertEquals(0x00, MbusClientAttributes.LOGICAL_NAME.forVersion(version10).getShortName());
        assertEquals(0x08, MbusClientAttributes.MBUS_PORT_REFERENCE.forVersion(version10).getShortName());
        assertEquals(0x10, MbusClientAttributes.CAPTURE_DEFINITION.forVersion(version10).getShortName());
        assertEquals(0x18, MbusClientAttributes.CAPTURE_PERIOD.forVersion(version10).getShortName());
        assertEquals(0x20, MbusClientAttributes.PRIMARY_ADDRESS.forVersion(version10).getShortName());
        assertEquals(0x28, MbusClientAttributes.IDENTIFICATION_NUMBER.forVersion(version10).getShortName());
        assertEquals(0x30, MbusClientAttributes.MANUFACTURER_ID.forVersion(version10).getShortName());
        assertEquals(0x38, MbusClientAttributes.VERSION.forVersion(version10).getShortName());
        assertEquals(0x40, MbusClientAttributes.DEVICE_TYPE.forVersion(version10).getShortName());
        assertEquals(0x48, MbusClientAttributes.ACCESS_NUMBER.forVersion(version10).getShortName());
        assertEquals(0x50, MbusClientAttributes.STATUS.forVersion(version10).getShortName());
        assertEquals(0x58, MbusClientAttributes.ALARM.forVersion(version10).getShortName());
    }

}
