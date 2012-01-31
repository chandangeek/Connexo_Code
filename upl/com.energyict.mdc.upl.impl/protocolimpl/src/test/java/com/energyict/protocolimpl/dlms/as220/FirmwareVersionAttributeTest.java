package com.energyict.protocolimpl.dlms.as220;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Copyrights EnergyICT
 * Date: 31/01/12
 * Time: 8:45
 */
public class FirmwareVersionAttributeTest {

    private static final TypeEnum FIRMWARE_TYPE = new TypeEnum(2);
    private static final Unsigned8 MAJOR_VERSION = new Unsigned8(5);
    private static final Unsigned8 MINOR_VERSION = new Unsigned8(1);
    private static final Unsigned32 FIRMWARE_CRC = new Unsigned32(321123);
    private static final OctetString FIRMWARE_ID = OctetString.fromByteArray(ProtocolTools.getBytesFromHexString("$01$02$03$04"));

    @Test
    public void testFirmwareVersion() throws Exception {
        Array array = new Array(
                new Structure(
                        FIRMWARE_ID,
                        FIRMWARE_TYPE,
                        MAJOR_VERSION,
                        MINOR_VERSION,
                        FIRMWARE_CRC
                )
        );

        FirmwareVersionAttribute fw = new FirmwareVersionAttribute(array.getBEREncodedByteArray());
        assertNotNull(fw);
        assertNotNull(fw.toString());

        assertNotNull(fw.getId());
        assertNotNull(fw.getType());
        assertNotNull(fw.getMajorVersion());
        assertNotNull(fw.getMinorVersion());
        assertNotNull(fw.getVersion());
        assertNotNull(fw.getCRC());

        assertEquals("01020304", fw.getId());
        assertEquals(FIRMWARE_TYPE.getValue(), fw.getType());
        assertEquals(MAJOR_VERSION.getValue(), fw.getMajorVersion());
        assertEquals(MINOR_VERSION.getValue(), fw.getMinorVersion());
        assertEquals(MAJOR_VERSION.getValue() + "." + MINOR_VERSION.getValue(), fw.getVersion());
        assertEquals(FIRMWARE_CRC.getValue(), fw.getCRC());

    }
}
