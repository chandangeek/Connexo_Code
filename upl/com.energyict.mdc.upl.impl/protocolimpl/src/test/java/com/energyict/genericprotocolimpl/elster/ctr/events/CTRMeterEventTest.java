package com.energyict.genericprotocolimpl.elster.ctr.events;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectFactory;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.protocolimpl.utils.ProtocolTools;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertArrayEquals;

/**
 * Copyrights EnergyICT
 * Date: 23-nov-2010
 * Time: 10:21:53
 */
public class CTRMeterEventTest extends TestCase {

    @Test
    public void testConvertToMeterEvents() throws Exception {
        CTRMeterEvent meterEvent = new CTRMeterEvent(TimeZone.getDefault());
        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType attributeType = new AttributeType(0xFF);
        attributeType.setHasIdentifier(true);

        List<byte[]> bytes = new ArrayList<byte[]>();

        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$30$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$31$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$32$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$33$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$34$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$35$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$36$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$37$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$38$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$3A$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$3B$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$3C$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$3D$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$3E$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$3F$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$40$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$41$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$42$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$43$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$44$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$46$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$47$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$48$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$49$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$4A$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$4B$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$4C$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$01$00$0A$0A$0A$00$00$01$01$00$4D$00$00$00$00$00$00$00$00$00$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));

        List<CTRAbstractValue[]> events = new ArrayList<CTRAbstractValue[]>();
        List<AbstractCTRObject> objects = new ArrayList<AbstractCTRObject>();

        for (byte[] byteArray : bytes) {
            objects.add(factory.parse(byteArray, 0, attributeType));
        }

        for (AbstractCTRObject object : objects) {
            events.add(object.getValue());
        }

        /**
         * Check if the descriptions match
         */
        assertEquals(meterEvent.convertToMeterEvents(events).get(0).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_30);
        assertEquals(meterEvent.convertToMeterEvents(events).get(1).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_31);
        assertEquals(meterEvent.convertToMeterEvents(events).get(2).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_32);
        assertEquals(meterEvent.convertToMeterEvents(events).get(3).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_33);
        assertEquals(meterEvent.convertToMeterEvents(events).get(4).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_34);
        assertEquals(meterEvent.convertToMeterEvents(events).get(5).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_35);
        assertEquals(meterEvent.convertToMeterEvents(events).get(6).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_36);
        assertEquals(meterEvent.convertToMeterEvents(events).get(7).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_37);
        assertEquals(meterEvent.convertToMeterEvents(events).get(8).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_38);
        assertEquals(meterEvent.convertToMeterEvents(events).get(9).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_3A);
        assertEquals(meterEvent.convertToMeterEvents(events).get(10).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_3B);
        assertEquals(meterEvent.convertToMeterEvents(events).get(11).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_3C);
        assertEquals(meterEvent.convertToMeterEvents(events).get(12).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_3D);
        assertEquals(meterEvent.convertToMeterEvents(events).get(13).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_3E);
        assertEquals(meterEvent.convertToMeterEvents(events).get(14).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_3F);
        assertEquals(meterEvent.convertToMeterEvents(events).get(15).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_40);
        assertEquals(meterEvent.convertToMeterEvents(events).get(16).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_41);
        assertEquals(meterEvent.convertToMeterEvents(events).get(17).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_42);
        assertEquals(meterEvent.convertToMeterEvents(events).get(18).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_43);
        assertEquals(meterEvent.convertToMeterEvents(events).get(19).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_44);
        assertEquals(meterEvent.convertToMeterEvents(events).get(20).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_46);
        assertEquals(meterEvent.convertToMeterEvents(events).get(21).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_47);
        assertEquals(meterEvent.convertToMeterEvents(events).get(22).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_48);
        assertEquals(meterEvent.convertToMeterEvents(events).get(23).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_49);
        assertEquals(meterEvent.convertToMeterEvents(events).get(24).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_4A);
        assertEquals(meterEvent.convertToMeterEvents(events).get(25).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_4B);
        assertEquals(meterEvent.convertToMeterEvents(events).get(26).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_4C);
        assertEquals(meterEvent.convertToMeterEvents(events).get(27).getMessage(), CTRMeterEvent.EVENT_DESCRIPTION_4D);

        /**
         * Check if the input bytes match the generated bytes
         */
        int index = 0;
        for (AbstractCTRObject object : objects) {
            assertArrayEquals(object.getBytes(), bytes.get(index));
            index++;
        }
    }
}
