package com.energyict.protocolimpl.dlms.as220.plc.events;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.apache.axis.types.UnsignedInt;
import org.junit.Test;

import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 20/07/11
 * Time: 16:24
 */
public class PLCEventTest {

    /*
    * sequence {
*      time:           octet-string    = OctetString
*      channel:        unsigned        = Unsigned8
*      master-address: long-unsigned   = Unsigned16
*      rejected:       boolean         = BooleanObject
*      s0:             long-unsigned   = Unsigned16
*      n0:             long-unsigned   = Unsigned16
*      s1:             long-unsigned   = Unsigned16
*      n1:             long-unsigned   = Unsigned16
*      gain:           unsigned        = Unsigned8
*      method:         unsigned        = Unsigned8
* }
    */
    @Test
    public void testGetDescription() throws Exception {
        // 1. Test of "Start of plc scan" Event
        Structure structure = new Structure(
                new OctetString(ProtocolTools.getBytesFromHexString("07DB0607020B192F00800000", "")),
                new Unsigned8(0),
                new Unsigned16(0),
                new BooleanObject(false),
                new Unsigned16(0),
                new Unsigned16(0),
                new Unsigned16(0),
                new Unsigned16(0),
                new Unsigned8(0),
                new Unsigned8(0)
        );

        byte[] berEncodedByteArray = structure.getBEREncodedByteArray();
        PLCEvent plcEvent = new PLCEvent(berEncodedByteArray, TimeZone.getDefault());
        String actualDescription = plcEvent.getDescription();
        assertEquals("Start of plc scan", actualDescription);

        // 2. Test of "End of plc scan. Selected [masterid] on channel [channelid]" Event
        structure = new Structure(
                new OctetString(ProtocolTools.getBytesFromHexString("07DB0607020B192F00800000", "")),
                new Unsigned8(1),       // !=0
                new Unsigned16(5),      // !=0
                new BooleanObject(false),
                new Unsigned16(0),
                new Unsigned16(0),
                new Unsigned16(0),
                new Unsigned16(0),
                new Unsigned8(0),
                new Unsigned8(0)
        );

        berEncodedByteArray = structure.getBEREncodedByteArray();
        plcEvent = new PLCEvent(berEncodedByteArray, TimeZone.getDefault());
        actualDescription = plcEvent.getDescription();
        assertEquals("End of plc scan. Selected 5 on channel 2", actualDescription);
    }
}
