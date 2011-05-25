package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.emh.lzqj.DummyFlagConnection;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 25-mei-2011
 * Time: 14:06:05
 */
public class ABBA1700MeterEventsTest {

    @Test
    public void getReverseRunCounterEventsTest() throws ConnectionException {
        byte[] responseByte = new byte[]{(byte) 0x30, (byte) 0x36, (byte) 0x30, (byte) 0x30, (byte) 0x43, (byte) 0x37, (byte) 0x36, (byte) 0x30, (byte) 0x41, (byte) 0x33, (byte) 0x34, (byte) 0x43, (byte) 0x42, (byte) 0x38, (byte) 0x36, (byte) 0x41, (byte) 0x42, (byte) 0x41, (byte) 0x34, (byte) 0x39, (byte) 0x33, (byte) 0x35, (byte) 0x36, (byte) 0x32, (byte) 0x42, (byte) 0x41, (byte) 0x34, (byte) 0x39};
        ABBA1700 protocol = new ABBA1700();
        DummyFlagConnection connection = new DummyFlagConnection(null, null, 1, 1, 1, 1, 1, true);
        connection.setResponseByte(responseByte);
        protocol.init(null, null, TimeZone.getTimeZone("Europe/Brussel"), Logger.getAnonymousLogger());
        protocol.setConnection(connection);
        protocol.setRegisterFactory(new ABBA1700RegisterFactory((ProtocolLink) protocol, (MeterExceptionInfo) protocol, new ABBA1700MeterType(1)));
        ABBA1700MeterEvents events = new ABBA1700MeterEvents(protocol);

        List<MeterEvent> meterEvents = events.getReverseRunCounterEvents(new Date(1));
        assertEquals(3, meterEvents.size());
        assertEquals(new Date(1236951605000L), meterEvents.get(0).getTime());
        assertEquals(new Date(1236953784000L), meterEvents.get(1).getTime());
        assertEquals(new Date(1285775559000L), meterEvents.get(2).getTime());

        connection.setResponseByte(responseByte);
        meterEvents = events.getReverseRunCounterEvents(new Date(1236952824000L));
        assertEquals(2, meterEvents.size());
        assertEquals(new Date(1236953784000L), meterEvents.get(0).getTime());
        assertEquals(new Date(1285775559000L), meterEvents.get(1).getTime());

        connection.setResponseByte(responseByte);
        meterEvents = events.getReverseRunCounterEvents(new Date(1262268024000L));
        assertEquals(1, meterEvents.size());
        assertEquals(new Date(1285775559000L), meterEvents.get(0).getTime());

        connection.setResponseByte(responseByte);
        meterEvents = events.getReverseRunCounterEvents(new Date(1306332000000L));
        assertEquals(0, meterEvents.size());

    }

    @Test
    public void getProgrammingCounterTest() throws ConnectionException {

        byte[] responseByte = new byte[]{(byte) 0x30, (byte) 0x42, (byte) 0x30, (byte) 0x30, (byte) 0x36, (byte) 0x43, (byte) 0x37, (byte) 0x30, (byte) 0x43, (byte) 0x34, (byte) 0x34, (byte) 0x44, (byte) 0x30, (byte) 0x44, (byte) 0x37, (byte) 0x35, (byte) 0x41, (byte) 0x38, (byte) 0x34, (byte) 0x44, (byte) 0x46, (byte) 0x33, (byte) 0x41, (byte) 0x37, (byte) 0x34, (byte) 0x44, (byte) 0x34, (byte) 0x43};
        ABBA1700 protocol = new ABBA1700();
        DummyFlagConnection connection = new DummyFlagConnection(null, null, 1, 1, 1, 1, 1, true);
        connection.setResponseByte(responseByte);
        protocol.init(null, null, TimeZone.getTimeZone("Europe/Brussel"), Logger.getAnonymousLogger());
        protocol.setConnection(connection);
        protocol.setRegisterFactory(new ABBA1700RegisterFactory((ProtocolLink) protocol, (MeterExceptionInfo) protocol, new ABBA1700MeterType(1)));
        ABBA1700MeterEvents events = new ABBA1700MeterEvents(protocol);

        List<MeterEvent> meterEvents = events.getProgrammingCounterEvents(new Date(1));
        assertEquals(3, meterEvents.size());
        assertEquals(new Date(1280157683000L), meterEvents.get(0).getTime());
        assertEquals(new Date(1302885645000L), meterEvents.get(1).getTime());
        assertEquals(new Date(1304719468000L), meterEvents.get(2).getTime());

        connection.setResponseByte(responseByte);
        meterEvents = events.getProgrammingCounterEvents(new Date(1293840000000L));
        assertEquals(2, meterEvents.size());
        assertEquals(new Date(1302885645000L), meterEvents.get(0).getTime());
        assertEquals(new Date(1304719468000L), meterEvents.get(1).getTime());

        connection.setResponseByte(responseByte);
        meterEvents = events.getProgrammingCounterEvents(new Date(1304208000000L));
        assertEquals(1, meterEvents.size());
        assertEquals(new Date(1304719468000L), meterEvents.get(0).getTime());

        connection.setResponseByte(responseByte);
        meterEvents = events.getProgrammingCounterEvents(new Date(1306332000000L));
        assertEquals(0, meterEvents.size());
    }

    @Test
    public void getPowerDownCounterTest() throws IOException {

        byte[] responseByte = new byte[]{(byte) 0x36, (byte) 0x44, (byte) 0x30, (byte) 0x30, (byte) 0x46, (byte) 0x41, (byte) 0x42, (byte) 0x42, (byte) 0x43, (byte) 0x32, (byte) 0x34, (byte) 0x44, (byte) 0x44, (byte) 0x46, (byte) 0x37, (byte) 0x37, (byte) 0x43, (byte) 0x32, (byte) 0x34, (byte) 0x44, (byte) 0x35, (byte) 0x33, (byte) 0x41, (byte) 0x39, (byte) 0x42, (byte) 0x36, (byte) 0x34, (byte) 0x44};
        ABBA1700 protocol = new ABBA1700();
        DummyFlagConnection connection = new DummyFlagConnection(null, null, 1, 1, 1, 1, 1, true);
        connection.setResponseByte(responseByte);
        protocol.init(null, null, TimeZone.getTimeZone("Europe/Brussel"), Logger.getAnonymousLogger());
        protocol.setConnection(connection);
        protocol.setRegisterFactory(new ABBA1700RegisterFactory((ProtocolLink) protocol, (MeterExceptionInfo) protocol, new ABBA1700MeterType(1)));
        ABBA1700MeterEvents events = new ABBA1700MeterEvents(protocol);

        List<MeterEvent> meterEvents = events.getPowerDownEvents(new Date(1));
        assertEquals(3, meterEvents.size());
        assertEquals(new Date(1303816531000L), meterEvents.get(0).getTime());
        assertEquals(new Date(1304590303000L), meterEvents.get(1).getTime());
        assertEquals(new Date(1304607738000L), meterEvents.get(2).getTime());

        connection.setResponseByte(responseByte);
        meterEvents = events.getPowerDownEvents(new Date(1304258400000L));
        assertEquals(2, meterEvents.size());
        assertEquals(new Date(1304590303000L), meterEvents.get(0).getTime());
        assertEquals(new Date(1304607738000L), meterEvents.get(1).getTime());

        connection.setResponseByte(responseByte);
        meterEvents = events.getPowerDownEvents(new Date(1304604000000L));
        assertEquals(1, meterEvents.size());
        assertEquals(new Date(1304607738000L), meterEvents.get(0).getTime());

        connection.setResponseByte(responseByte);
        meterEvents = events.getPowerDownEvents(new Date(1306332000000L));
        assertEquals(0, meterEvents.size());
    }

    @Test
    public void getPhaseFailureEventsTest() throws ConnectionException {

        byte[] responseByte = new byte[]{(byte) 0x33, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x39, (byte) 0x33, (byte) 0x32, (byte) 0x32, (byte) 0x37, (byte) 0x46, (byte) 0x34, (byte) 0x44, (byte) 0x33, (byte) 0x30, (byte) 0x32, (byte) 0x32, (byte) 0x37, (byte) 0x46, (byte) 0x34, (byte) 0x44, (byte) 0x43, (byte) 0x36, (byte) 0x32, (byte) 0x31, (byte) 0x37, (byte) 0x46, (byte) 0x34, (byte) 0x44, (byte) 0x30, (byte) 0x33, (byte) 0x30, (byte) 0x32, (byte) 0x30, (byte) 0x31};
        ABBA1700 protocol = new ABBA1700();
        DummyFlagConnection connection = new DummyFlagConnection(null, null, 1, 1, 1, 1, 1, true);
        connection.setResponseByte(responseByte);
        protocol.init(null, null, TimeZone.getTimeZone("Europe/Brussel"), Logger.getAnonymousLogger());
        protocol.setConnection(connection);
        protocol.setRegisterFactory(new ABBA1700RegisterFactory((ProtocolLink) protocol, (MeterExceptionInfo) protocol, new ABBA1700MeterType(1)));
        ABBA1700MeterEvents events = new ABBA1700MeterEvents(protocol);

        List<MeterEvent> meterEvents = events.getPhaseFailureEvents(new Date(1));
        assertEquals(3, meterEvents.size());
        assertEquals(new Date(1300177350000L), meterEvents.get(0).getTime());
        assertEquals("Phase 1", meterEvents.get(0).getMessage());
        assertEquals(new Date(1300177456000L), meterEvents.get(1).getTime());
        assertEquals("Phase 2", meterEvents.get(1).getMessage());
        assertEquals(new Date(1300177555000L), meterEvents.get(2).getTime());
        assertEquals("Phase 3", meterEvents.get(2).getMessage());

        connection.setResponseByte(responseByte);
        meterEvents = events.getPhaseFailureEvents(new Date(1306332000000L));
        assertEquals(0, meterEvents.size());
    }
}
