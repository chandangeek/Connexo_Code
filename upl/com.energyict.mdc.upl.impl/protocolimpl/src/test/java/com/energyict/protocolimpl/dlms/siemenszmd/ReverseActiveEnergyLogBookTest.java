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
    public void testParserWith0EnergyRegisters() throws IOException {
        ReverseActiveEnergyLogBook parser = mockParser(0);

        Calendar fromDate = Calendar.getInstance();
        fromDate.setTime(new Date(0));
        Calendar toDate = Calendar.getInstance();
        List<MeterEvent> meterEvents = parser.readEvents(fromDate, toDate);

        assertEquals(meterEvents.size(), 4);
        assertTrue(meterEvents.get(0).getMessage().length() < 255);
        assertEquals(meterEvents.get(0).getMessage(), "Reverse run start. V:2299,2299,2300Wh;I:599,599,600,1201Wh;PF:-33%;D:135s;P:3");
        assertEquals(meterEvents.get(0).getTime().getTime(), 1424109658000L);       //Mon, 16 Feb 2015 18:00:58 GMT
        assertEquals(meterEvents.get(0).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(0).getProtocolCode(), 0);

        assertTrue(meterEvents.get(1).getMessage().length() < 255);
        assertEquals(meterEvents.get(1).getMessage(), "Reverse run end. V:2299,2300,2300Wh;I:599,599,600,0Wh;PF:100%;D:135s;P:3");
        assertEquals(meterEvents.get(1).getTime().getTime(), 1424109793000L);
        assertEquals(meterEvents.get(1).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(1).getProtocolCode(), 0);

        assertTrue(meterEvents.get(2).getMessage().length() < 255);
        assertEquals(meterEvents.get(2).getMessage(), "Reverse run start. V:2299,2299,2300Wh;I:599,599,600,1201Wh;PF:-33%;D:135s;P:3");
        assertEquals(meterEvents.get(2).getTime().getTime(), 1424109658000L);
        assertEquals(meterEvents.get(2).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(2).getProtocolCode(), 0);

        assertTrue(meterEvents.get(3).getMessage().length() < 255);
        assertEquals(meterEvents.get(3).getMessage(), "Reverse run end. V:2299,2300,2300Wh;I:599,599,600,0Wh;PF:100%;D:135s;P:3");
        assertEquals(meterEvents.get(3).getTime().getTime(), 1424109793000L);
        assertEquals(meterEvents.get(3).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(3).getProtocolCode(), 0);
    }

    @Test
    public void testParserWith1EnergyRegisters() throws IOException {
        ReverseActiveEnergyLogBook parser = mockParser(1);

        Calendar fromDate = Calendar.getInstance();
        fromDate.setTime(new Date(0));
        Calendar toDate = Calendar.getInstance();
        List<MeterEvent> meterEvents = parser.readEvents(fromDate, toDate);

        assertEquals(meterEvents.size(), 4);
        assertTrue(meterEvents.get(0).getMessage().length() < 255);
        assertEquals(meterEvents.get(0).getMessage(), "Reverse run start. E:174440Wh;V:2299,2299,2300Wh;I:599,599,600,1201Wh;PF:-33%;D:135s;P:3");
        assertEquals(meterEvents.get(0).getTime().getTime(), 1424109658000L);       //Mon, 16 Feb 2015 18:00:58 GMT
        assertEquals(meterEvents.get(0).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(0).getProtocolCode(), 0);

        assertTrue(meterEvents.get(1).getMessage().length() < 255);
        assertEquals(meterEvents.get(1).getMessage(), "Reverse run end. E:174543Wh;V:2299,2300,2300Wh;I:599,599,600,0Wh;PF:100%;D:135s;P:3");
        assertEquals(meterEvents.get(1).getTime().getTime(), 1424109793000L);
        assertEquals(meterEvents.get(1).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(1).getProtocolCode(), 0);

        assertTrue(meterEvents.get(2).getMessage().length() < 255);
        assertEquals(meterEvents.get(2).getMessage(), "Reverse run start. E:174440Wh;V:2299,2299,2300Wh;I:599,599,600,1201Wh;PF:-33%;D:135s;P:3");
        assertEquals(meterEvents.get(2).getTime().getTime(), 1424109658000L);
        assertEquals(meterEvents.get(2).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(2).getProtocolCode(), 0);

        assertTrue(meterEvents.get(3).getMessage().length() < 255);
        assertEquals(meterEvents.get(3).getMessage(), "Reverse run end. E:174543Wh;V:2299,2300,2300Wh;I:599,599,600,0Wh;PF:100%;D:135s;P:3");
        assertEquals(meterEvents.get(3).getTime().getTime(), 1424109793000L);
        assertEquals(meterEvents.get(3).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(3).getProtocolCode(), 0);
    }

    @Test
    public void testParserWith2EnergyRegisters() throws IOException {
        ReverseActiveEnergyLogBook parser = mockParser(2);

        Calendar fromDate = Calendar.getInstance();
        fromDate.setTime(new Date(0));
        Calendar toDate = Calendar.getInstance();
        List<MeterEvent> meterEvents = parser.readEvents(fromDate, toDate);

        assertEquals(meterEvents.size(), 4);
        assertTrue(meterEvents.get(0).getMessage().length() < 255);
        assertEquals(meterEvents.get(0).getMessage(), "Reverse run start. E:174440Wh,6253Wh;V:2299,2299,2300Wh;I:599,599,600,1201Wh;PF:-33%;D:135s;P:3");
        assertEquals(meterEvents.get(0).getTime().getTime(), 1424109658000L);       //Mon, 16 Feb 2015 18:00:58 GMT
        assertEquals(meterEvents.get(0).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(0).getProtocolCode(), 0);

        assertTrue(meterEvents.get(1).getMessage().length() < 255);
        assertEquals(meterEvents.get(1).getMessage(), "Reverse run end. E:174543Wh,6254Wh;V:2299,2300,2300Wh;I:599,599,600,0Wh;PF:100%;D:135s;P:3");
        assertEquals(meterEvents.get(1).getTime().getTime(), 1424109793000L);
        assertEquals(meterEvents.get(1).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(1).getProtocolCode(), 0);

        assertTrue(meterEvents.get(2).getMessage().length() < 255);
        assertEquals(meterEvents.get(2).getMessage(), "Reverse run start. E:174440Wh,6253Wh;V:2299,2299,2300Wh;I:599,599,600,1201Wh;PF:-33%;D:135s;P:3");
        assertEquals(meterEvents.get(2).getTime().getTime(), 1424109658000L);
        assertEquals(meterEvents.get(2).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(2).getProtocolCode(), 0);

        assertTrue(meterEvents.get(3).getMessage().length() < 255);
        assertEquals(meterEvents.get(3).getMessage(), "Reverse run end. E:174543Wh,6254Wh;V:2299,2300,2300Wh;I:599,599,600,0Wh;PF:100%;D:135s;P:3");
        assertEquals(meterEvents.get(3).getTime().getTime(), 1424109793000L);
        assertEquals(meterEvents.get(3).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(3).getProtocolCode(), 0);
    }

    @Test
    public void testParserWith3EnergyRegisters() throws IOException {
        ReverseActiveEnergyLogBook parser = mockParser(3);

        Calendar fromDate = Calendar.getInstance();
        fromDate.setTime(new Date(0));
        Calendar toDate = Calendar.getInstance();
        List<MeterEvent> meterEvents = parser.readEvents(fromDate, toDate);

        assertEquals(meterEvents.size(), 4);
        assertTrue(meterEvents.get(0).getMessage().length() < 255);
        assertEquals(meterEvents.get(0).getMessage(), "Reverse run start. E:174440Wh,6253Wh,1Wh;V:2299,2299,2300Wh;I:599,599,600,1201Wh;PF:-33%;D:135s;P:3");
        assertEquals(meterEvents.get(0).getTime().getTime(), 1424109658000L);       //Mon, 16 Feb 2015 18:00:58 GMT
        assertEquals(meterEvents.get(0).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(0).getProtocolCode(), 0);

        assertTrue(meterEvents.get(1).getMessage().length() < 255);
        assertEquals(meterEvents.get(1).getMessage(), "Reverse run end. E:174543Wh,6254Wh,1Wh;V:2299,2300,2300Wh;I:599,599,600,0Wh;PF:100%;D:135s;P:3");
        assertEquals(meterEvents.get(1).getTime().getTime(), 1424109793000L);
        assertEquals(meterEvents.get(1).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(1).getProtocolCode(), 0);

        assertTrue(meterEvents.get(2).getMessage().length() < 255);
        assertEquals(meterEvents.get(2).getMessage(), "Reverse run start. E:174440Wh,6253Wh,1Wh;V:2299,2299,2300Wh;I:599,599,600,1201Wh;PF:-33%;D:135s;P:3");
        assertEquals(meterEvents.get(2).getTime().getTime(), 1424109658000L);
        assertEquals(meterEvents.get(2).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(2).getProtocolCode(), 0);

        assertTrue(meterEvents.get(3).getMessage().length() < 255);
        assertEquals(meterEvents.get(3).getMessage(), "Reverse run end. E:174543Wh,6254Wh,1Wh;V:2299,2300,2300Wh;I:599,599,600,0Wh;PF:100%;D:135s;P:3");
        assertEquals(meterEvents.get(3).getTime().getTime(), 1424109793000L);
        assertEquals(meterEvents.get(3).getEiCode(), MeterEvent.REVERSE_RUN);
        assertEquals(meterEvents.get(3).getProtocolCode(), 0);
    }

    private ReverseActiveEnergyLogBook mockParser(int numberOfEnergyRegisters) throws IOException {
        ProtocolLink protocolLink = mock(ProtocolLink.class);
        when(protocolLink.getReference()).thenReturn(ProtocolLink.LN_REFERENCE);
        ReverseActiveEnergyLogBook parser = spy(new ReverseActiveEnergyLogBook(new CosemObjectFactory(protocolLink)));

        doReturn(createBuffer(numberOfEnergyRegisters)).when(parser).readBuffer(any(DedicatedEventLogSimple.class));
        doReturn(createDummyCapturedObjects(numberOfEnergyRegisters)).when(parser).readCapturedObjects(any(DedicatedEventLogSimple.class));
        doReturn(Unit.get(BaseUnit.WATTHOUR)).when(parser).getUnit(any(CapturedObject.class));
        return parser;
    }

    private List<CapturedObject> createDummyCapturedObjects(int numberOfEnergyRegisters) {
        List<CapturedObject> capturedObjects = new ArrayList<CapturedObject>(25);
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 0, (byte) 0, (byte) 240, (byte) 30, (byte) 13, (byte) 255})), 2, 0));
        capturedObjects.add(new CapturedObject(8, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 255})), 2, 0));

        if (numberOfEnergyRegisters > 0) {
            capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 1, (byte) 8, (byte) 0, (byte) 255})), 2, 0));
        }
        if (numberOfEnergyRegisters > 1) {
            capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 5, (byte) 8, (byte) 0, (byte) 255})), 2, 0));
        }
        if (numberOfEnergyRegisters > 2) {
            capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 2, (byte) 8, (byte) 0, (byte) 255})), 2, 0));
        }

        capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 32, (byte) 7, (byte) 0, (byte) 255})), 2, 0));
        capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 52, (byte) 7, (byte) 0, (byte) 255})), 2, 0));
        capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 72, (byte) 7, (byte) 0, (byte) 255})), 2, 0));
        capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 31, (byte) 7, (byte) 0, (byte) 255})), 2, 0));
        capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 51, (byte) 7, (byte) 0, (byte) 255})), 2, 0));
        capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 71, (byte) 7, (byte) 0, (byte) 255})), 2, 0));
        capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 91, (byte) 7, (byte) 0, (byte) 255})), 2, 0));
        capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 13, (byte) 7, (byte) 0, (byte) 255})), 2, 0));
        capturedObjects.add(new CapturedObject(8, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 255})), 2, 0));

        if (numberOfEnergyRegisters > 0) {
            capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 1, (byte) 8, (byte) 0, (byte) 255})), 2, 0));
        }
        if (numberOfEnergyRegisters > 1) {
            capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 5, (byte) 8, (byte) 0, (byte) 255})), 2, 0));
        }
        if (numberOfEnergyRegisters > 2) {
            capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 2, (byte) 8, (byte) 0, (byte) 255})), 2, 0));
        }

        capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 32, (byte) 7, (byte) 0, (byte) 255})), 2, 0));
        capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 52, (byte) 7, (byte) 0, (byte) 255})), 2, 0));
        capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 72, (byte) 7, (byte) 0, (byte) 255})), 2, 0));
        capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 31, (byte) 7, (byte) 0, (byte) 255})), 2, 0));
        capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 51, (byte) 7, (byte) 0, (byte) 255})), 2, 0));
        capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 71, (byte) 7, (byte) 0, (byte) 255})), 2, 0));
        capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 91, (byte) 7, (byte) 0, (byte) 255})), 2, 0));
        capturedObjects.add(new CapturedObject(3, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 1, (byte) 1, (byte) 13, (byte) 7, (byte) 0, (byte) 255})), 2, 0));
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 0, (byte) 0, (byte) 240, (byte) 30, (byte) 14, (byte) 255})), 2, 0));
        capturedObjects.add(new CapturedObject(1, new LogicalName(new com.energyict.dlms.OctetString(new byte[]{(byte) 0, (byte) 0, (byte) 240, (byte) 30, (byte) 15, (byte) 255})), 2, 0));

        assertEquals(capturedObjects.get(1), capturedObjects.get(10 + numberOfEnergyRegisters));
        return capturedObjects;
    }

    private DataContainer createBuffer(int numberOfEnergyRegisters) throws IOException {
        DataContainer result = new DataContainer();

        Structure bufferEntry1 = createBufferEntry(numberOfEnergyRegisters);

        Array array = new Array();
        array.addDataType(bufferEntry1);
        array.addDataType(bufferEntry1);

        result.parseObjectList(array.getBEREncodedByteArray(), Logger.getAnonymousLogger());
        return result;
    }

    private Structure createBufferEntry(int numberOfEnergyRegisters) {
        Structure bufferEntry1 = new Structure();
        bufferEntry1.addDataType(new Integer8(123));
        bufferEntry1.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString("07DF02100112003AFF800000", "")));

        if (numberOfEnergyRegisters > 0) {
            bufferEntry1.addDataType(new Integer64(174440));
        }
        if (numberOfEnergyRegisters > 1) {
            bufferEntry1.addDataType(new Integer32(6253));
        }
        if (numberOfEnergyRegisters > 2) {
            bufferEntry1.addDataType(new Integer32(1));
        }

        bufferEntry1.addDataType(new Integer16(2299));
        bufferEntry1.addDataType(new Integer16(2299));
        bufferEntry1.addDataType(new Integer16(2300));
        bufferEntry1.addDataType(new Integer16(599));
        bufferEntry1.addDataType(new Integer16(599));
        bufferEntry1.addDataType(new Integer16(600));
        bufferEntry1.addDataType(new Integer16(1201));
        bufferEntry1.addDataType(new Integer8(-33));
        bufferEntry1.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString("07DF02100112030DFF800000", "")));

        if (numberOfEnergyRegisters > 0) {
            bufferEntry1.addDataType(new Integer64(174543));
        }
        if (numberOfEnergyRegisters > 1) {
            bufferEntry1.addDataType(new Integer32(6254));
        }
        if (numberOfEnergyRegisters > 2) {
            bufferEntry1.addDataType(new Integer32(1));
        }

        bufferEntry1.addDataType(new Integer16(2299));
        bufferEntry1.addDataType(new Integer16(2300));
        bufferEntry1.addDataType(new Integer16(2300));
        bufferEntry1.addDataType(new Integer16(599));
        bufferEntry1.addDataType(new Integer16(599));
        bufferEntry1.addDataType(new Integer16(600));
        bufferEntry1.addDataType(new Integer8(0));
        bufferEntry1.addDataType(new Integer8(100));
        bufferEntry1.addDataType(new Unsigned32(135));
        bufferEntry1.addDataType(new Integer8(3));
        return bufferEntry1;
    }
}