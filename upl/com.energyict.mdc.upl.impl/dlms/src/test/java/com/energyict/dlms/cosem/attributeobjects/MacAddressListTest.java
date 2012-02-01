package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.Array;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Copyrights EnergyICT
 * Date: 31/01/12
 * Time: 16:56
 */
public class MacAddressListTest {

    @Test
    public void testParseEmpty() throws Exception {
        MacAddressList list = new MacAddressList(new Array().getBEREncodedByteArray(), 0, 0);
        assertNotNull(list.toString());
        assertEquals(0, list.getLevel());
        assertEquals(0, list.nrOfDataTypes());
    }

    @Test
    public void testParseSingle() throws Exception {
        MacAddress macAddress = new MacAddress(12345);
        Array array = new Array(macAddress);
        MacAddressList list = new MacAddressList(array.getBEREncodedByteArray(), 0, 0);
        assertNotNull(list.toString());
        assertEquals(0, list.getLevel());
        assertEquals(1, list.nrOfDataTypes());
        assertEquals(macAddress.toString(), list.getArrayItem(0).toString());
    }

    @Test
    public void testParseMultiple() throws Exception {
        Array array = new Array();
        for (int i = 0; i < 10; i++) {
            array.addDataType(new MacAddress(i));
        }
        MacAddressList list = new MacAddressList(array.getBEREncodedByteArray(), 0, 0);
        assertNotNull(list.toString());
        assertEquals(0, list.getLevel());
        assertEquals(10, list.nrOfDataTypes());
        for (int i = 0; i < 10; i++) {
            assertEquals(new MacAddress(i).toString(), list.getArrayItem(i).toString());
        }
    }


}
