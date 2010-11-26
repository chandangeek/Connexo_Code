package com.energyict.dlms.cosem.attributes;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 25-nov-2010
 * Time: 15:47:30
 */
public class MbusClientMethodsTest {

    private static final int version9 = 9;
    private static final int version10 = 10;

    @Test
    public final void forVersionTest() {
        assertEquals(0x68, MbusClientMethods.SLAVE_INSTALL.forVersion(version9).getShortName());
        assertEquals(0x70, MbusClientMethods.SLAVE_DEINSTALL.forVersion(version9).getShortName());
        assertEquals(0x78, MbusClientMethods.CAPTURE.forVersion(version9).getShortName());
        assertEquals(0x80, MbusClientMethods.RESET_ALARM.forVersion(version9).getShortName());
        assertEquals(0x88, MbusClientMethods.SYNCHRONIZE_CLOCK.forVersion(version9).getShortName());
        assertEquals(0x90, MbusClientMethods.DATA_SEND.forVersion(version9).getShortName());
        assertEquals(0x98, MbusClientMethods.SET_ENCRYPTION_KEY.forVersion(version9).getShortName());
        assertEquals(0xA0, MbusClientMethods.TRANSFER_KEY.forVersion(version9).getShortName());

        assertEquals(0x60, MbusClientMethods.SLAVE_INSTALL.forVersion(version10).getShortName());
        assertEquals(0x68, MbusClientMethods.SLAVE_DEINSTALL.forVersion(version10).getShortName());
        assertEquals(0x70, MbusClientMethods.CAPTURE.forVersion(version10).getShortName());
        assertEquals(0x78, MbusClientMethods.RESET_ALARM.forVersion(version10).getShortName());
        assertEquals(0x80, MbusClientMethods.SYNCHRONIZE_CLOCK.forVersion(version10).getShortName());
        assertEquals(0x88, MbusClientMethods.DATA_SEND.forVersion(version10).getShortName());
        assertEquals(0x90, MbusClientMethods.SET_ENCRYPTION_KEY.forVersion(version10).getShortName());
        assertEquals(0x98, MbusClientMethods.TRANSFER_KEY.forVersion(version10).getShortName());
    }

}