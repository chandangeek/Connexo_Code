package com.energyict.protocolimpl.dlms.siemenszmd;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DedicatedEventLogSimple;
import com.energyict.dlms.cosem.LogicalName;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 16/04/2015 - 17:05
 */
@RunWith(MockitoJUnitRunner.class)
public class ReverseActiveEnergyLogBookTest {

    @Test
    public void test() throws IOException {

        ProtocolLink protocolLink = mock(ProtocolLink.class);
        when(protocolLink.getReference()).thenReturn(ProtocolLink.LN_REFERENCE);

        ReverseActiveEnergyLogBook parser = spy(new ReverseActiveEnergyLogBook(new CosemObjectFactory(protocolLink)));
        Calendar fromDate = Calendar.getInstance();
        fromDate.setTime(new Date(0));
        Calendar toDate = Calendar.getInstance();

        doReturn(createBuffer()).when(parser).readBuffer(any(DedicatedEventLogSimple.class));
        doReturn(createDummyCapturedObjects()).when(parser).readCapturedObjects(any(DedicatedEventLogSimple.class));
        doReturn(Unit.get(BaseUnit.WATTHOUR)).when(parser).getUnit(any(CapturedObject.class));

        List<MeterEvent> meterEvents = parser.readEvents(fromDate, toDate);

        assertEquals(meterEvents.size(), 4);
        assertTrue(meterEvents.get(0).getMessage().length() < 255);
        assertEquals(meterEvents.get(0).getMessage(), "Reverse run start. E:11111111Wh,22222222Wh,33333333Wh;V:230,231,232Wh;I:5,6,7,8Wh;PF:90%;D:20s;P:1");
        assertEquals(meterEvents.get(0).getTime().getTime(), 1409583775000L);
        assertEquals(meterEvents.get(0).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(0).getProtocolCode(), 0);

        assertTrue(meterEvents.get(1).getMessage().length() < 255);
        assertEquals(meterEvents.get(1).getMessage(), "Reverse run end. E:11111111Wh,22222222Wh,33333333Wh;V:230,231,232Wh;I:5,6,7,8Wh;PF:90%;D:20s;P:1");
        assertEquals(meterEvents.get(1).getTime().getTime(), 1409583835000L);
        assertEquals(meterEvents.get(1).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(1).getProtocolCode(), 0);

        assertTrue(meterEvents.get(2).getMessage().length() < 255);
        assertEquals(meterEvents.get(2).getMessage(), "Reverse run start. E:11111111Wh,22222222Wh,33333333Wh;V:230,231,232Wh;I:5,6,7,8Wh;PF:90%;D:20s;P:1");
        assertEquals(meterEvents.get(2).getTime().getTime(), 1409583775000L);
        assertEquals(meterEvents.get(2).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(2).getProtocolCode(), 0);

        assertTrue(meterEvents.get(3).getMessage().length() < 255);
        assertEquals(meterEvents.get(3).getMessage(), "Reverse run end. E:11111111Wh,22222222Wh,33333333Wh;V:230,231,232Wh;I:5,6,7,8Wh;PF:90%;D:20s;P:1");
        assertEquals(meterEvents.get(3).getTime().getTime(), 1409583835000L);
        assertEquals(meterEvents.get(3).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(3).getProtocolCode(), 0);

    }

    private List<CapturedObject> createDummyCapturedObjects() {
        List<CapturedObject> capturedObjects = new ArrayList<CapturedObject>(17);
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[0])), 2, 0));
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[0])), 2, 0));
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[0])), 2, 0));
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[0])), 2, 0));
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[0])), 2, 0));
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[0])), 2, 0));
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[0])), 2, 0));
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[0])), 2, 0));
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[0])), 2, 0));
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[0])), 2, 0));
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[0])), 2, 0));
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[0])), 2, 0));
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[0])), 2, 0));
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[0])), 2, 0));
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[0])), 2, 0));
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[0])), 2, 0));
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[0])), 2, 0));
        return capturedObjects;
    }

    private DataContainer createBuffer() throws IOException {
        DataContainer result = new DataContainer();

        Structure bufferEntry1 = createBufferEntry();

        Array array = new Array();
        array.addDataType(bufferEntry1);
        array.addDataType(bufferEntry1);

        result.parseObjectList(array.getBEREncodedByteArray(), Logger.getAnonymousLogger());
        return result;
    }

    private Structure createBufferEntry() {
        Structure bufferEntry1 = new Structure();
        bufferEntry1.addDataType(new Integer8(1));
        bufferEntry1.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString("07DE0901010F02372E000000", "")));
        bufferEntry1.addDataType(new Integer64(11111111));
        bufferEntry1.addDataType(new Integer32(22222222));
        bufferEntry1.addDataType(new Integer64(33333333));
        bufferEntry1.addDataType(new Integer16(230));
        bufferEntry1.addDataType(new Integer16(231));
        bufferEntry1.addDataType(new Integer16(232));
        bufferEntry1.addDataType(new Integer8(5));
        bufferEntry1.addDataType(new Integer8(6));
        bufferEntry1.addDataType(new Integer8(7));
        bufferEntry1.addDataType(new Integer8(8));
        bufferEntry1.addDataType(new Integer8(90));
        bufferEntry1.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString("07DE0901010F03372E000000", "")));
        bufferEntry1.addDataType(new Integer64(11111111));
        bufferEntry1.addDataType(new Integer32(22222222));
        bufferEntry1.addDataType(new Integer64(33333333));
        bufferEntry1.addDataType(new Integer16(230));
        bufferEntry1.addDataType(new Integer16(231));
        bufferEntry1.addDataType(new Integer16(232));
        bufferEntry1.addDataType(new Integer8(5));
        bufferEntry1.addDataType(new Integer8(6));
        bufferEntry1.addDataType(new Integer8(7));
        bufferEntry1.addDataType(new Integer8(8));
        bufferEntry1.addDataType(new Integer8(90));
        bufferEntry1.addDataType(new Unsigned32(20));
        bufferEntry1.addDataType(new Integer8(1));
        return bufferEntry1;
    }
}