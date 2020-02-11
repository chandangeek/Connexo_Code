package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.cosem.MBusClient;
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
        assertEquals(0x00, MBusClientAttributes.LOGICAL_NAME.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());
        assertEquals(0x10, MBusClientAttributes.MBUS_PORT_REFERENCE.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());
        assertEquals(0x18, MBusClientAttributes.CAPTURE_DEFINITION.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());
        assertEquals(0x20, MBusClientAttributes.CAPTURE_PERIOD.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());
        assertEquals(0x28, MBusClientAttributes.PRIMARY_ADDRESS.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());
        assertEquals(0x30, MBusClientAttributes.IDENTIFICATION_NUMBER.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());
        assertEquals(0x38, MBusClientAttributes.MANUFACTURER_ID.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());
        assertEquals(0x40, MBusClientAttributes.VERSION.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());
        assertEquals(0x48, MBusClientAttributes.DEVICE_TYPE.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());
        assertEquals(0x50, MBusClientAttributes.ACCESS_NUMBER.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());
        assertEquals(0x58, MBusClientAttributes.STATUS.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());
        assertEquals(0x60, MBusClientAttributes.ALARM.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());

        assertEquals(0x00, MBusClientAttributes.LOGICAL_NAME.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
        assertEquals(0x08, MBusClientAttributes.MBUS_PORT_REFERENCE.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
        assertEquals(0x10, MBusClientAttributes.CAPTURE_DEFINITION.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
        assertEquals(0x18, MBusClientAttributes.CAPTURE_PERIOD.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
        assertEquals(0x20, MBusClientAttributes.PRIMARY_ADDRESS.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
        assertEquals(0x28, MBusClientAttributes.IDENTIFICATION_NUMBER.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
        assertEquals(0x30, MBusClientAttributes.MANUFACTURER_ID.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
        assertEquals(0x38, MBusClientAttributes.VERSION.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
        assertEquals(0x40, MBusClientAttributes.DEVICE_TYPE.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
        assertEquals(0x48, MBusClientAttributes.ACCESS_NUMBER.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
        assertEquals(0x50, MBusClientAttributes.STATUS.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
        assertEquals(0x58, MBusClientAttributes.ALARM.forVersion(MBusClient.VERSION.VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
    }

}
