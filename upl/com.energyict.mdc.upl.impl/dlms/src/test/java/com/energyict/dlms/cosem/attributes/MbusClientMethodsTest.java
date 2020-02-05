package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.cosem.methods.MBusClientMethods;
import org.junit.Test;

import static com.energyict.dlms.cosem.MBusClient.VERSION.VERSION0_BLUE_BOOK_10TH_EDITION;
import static com.energyict.dlms.cosem.MBusClient.VERSION.VERSION0_BLUE_BOOK_9TH_EDITION;
import static com.energyict.dlms.cosem.MBusClient.VERSION.VERSION0_D_S_M_R_23_SPEC;
import static com.energyict.dlms.cosem.MBusClient.VERSION.VERSION1;
import static junit.framework.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 25-nov-2010
 * Time: 15:47:30
 */
public class MbusClientMethodsTest {

    @Test
    public final void forVersionTest() {
        assertEquals(0x68, MBusClientMethods.SLAVE_INSTALL.forVersion(VERSION0_D_S_M_R_23_SPEC).getShortName());
        assertEquals(0x70, MBusClientMethods.SLAVE_DEINSTALL.forVersion(VERSION0_D_S_M_R_23_SPEC).getShortName());
        assertEquals(0x78, MBusClientMethods.CAPTURE.forVersion(VERSION0_D_S_M_R_23_SPEC).getShortName());
        assertEquals(0x80, MBusClientMethods.RESET_ALARM.forVersion(VERSION0_D_S_M_R_23_SPEC).getShortName());
        assertEquals(0x88, MBusClientMethods.SYNCHRONIZE_CLOCK.forVersion(VERSION0_D_S_M_R_23_SPEC).getShortName());
        assertEquals(0x90, MBusClientMethods.DATA_SEND.forVersion(VERSION0_D_S_M_R_23_SPEC).getShortName());
        assertEquals(0x98, MBusClientMethods.SET_ENCRYPTION_KEY.forVersion(VERSION0_D_S_M_R_23_SPEC).getShortName());

        assertEquals(0x68, MBusClientMethods.SLAVE_INSTALL.forVersion(VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());
        assertEquals(0x70, MBusClientMethods.SLAVE_DEINSTALL.forVersion(VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());
        assertEquals(0x78, MBusClientMethods.CAPTURE.forVersion(VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());
        assertEquals(0x80, MBusClientMethods.RESET_ALARM.forVersion(VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());
        assertEquals(0x88, MBusClientMethods.SYNCHRONIZE_CLOCK.forVersion(VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());
        assertEquals(0x90, MBusClientMethods.DATA_SEND.forVersion(VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());
        assertEquals(0x98, MBusClientMethods.SET_ENCRYPTION_KEY.forVersion(VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());
        assertEquals(0xA0, MBusClientMethods.TRANSFER_KEY.forVersion(VERSION0_BLUE_BOOK_9TH_EDITION).getShortName());

        assertEquals(0x60, MBusClientMethods.SLAVE_INSTALL.forVersion(VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
        assertEquals(0x68, MBusClientMethods.SLAVE_DEINSTALL.forVersion(VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
        assertEquals(0x70, MBusClientMethods.CAPTURE.forVersion(VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
        assertEquals(0x78, MBusClientMethods.RESET_ALARM.forVersion(VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
        assertEquals(0x80, MBusClientMethods.SYNCHRONIZE_CLOCK.forVersion(VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
        assertEquals(0x88, MBusClientMethods.DATA_SEND.forVersion(VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
        assertEquals(0x90, MBusClientMethods.SET_ENCRYPTION_KEY.forVersion(VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());
        assertEquals(0x98, MBusClientMethods.TRANSFER_KEY.forVersion(VERSION0_BLUE_BOOK_10TH_EDITION).getShortName());

        assertEquals(0x70, MBusClientMethods.SLAVE_INSTALL.forVersion(VERSION1).getShortName());
        assertEquals(0x78, MBusClientMethods.SLAVE_DEINSTALL.forVersion(VERSION1).getShortName());
        assertEquals(0x80, MBusClientMethods.CAPTURE.forVersion(VERSION1).getShortName());
        assertEquals(0x88, MBusClientMethods.RESET_ALARM.forVersion(VERSION1).getShortName());
        assertEquals(0x90, MBusClientMethods.SYNCHRONIZE_CLOCK.forVersion(VERSION1).getShortName());
        assertEquals(0x98, MBusClientMethods.DATA_SEND.forVersion(VERSION1).getShortName());
        assertEquals(0xA0, MBusClientMethods.SET_ENCRYPTION_KEY.forVersion(VERSION1).getShortName());
        assertEquals(0xA8, MBusClientMethods.TRANSFER_KEY.forVersion(VERSION1).getShortName());
    }

}