package com.energyict.protocolimplv2.nta.dsmr40.eventhandling;

import com.energyict.protocol.MeterEvent;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DSMR40MbusEventLogTest {

    @Test
    public void testPossibleValueForChannel1() {
        Date date = new Date();
        DSMR40MbusEventLog eventLog = new DSMR40MbusEventLog(null, 1);
        List<MeterEvent> meterEvents = new ArrayList<>();
        for (int protocolCode=0; protocolCode<=255; protocolCode++) {
            eventLog.buildMeterEvent(meterEvents, date, protocolCode);
        }

        assertEquals(8, meterEvents.size());

        assertMeterEvent(meterEvents.get(0), MeterEvent.COMMUNICATION_ERROR_MBUS, "Communication problem when reading the meter connected to channel 1 of the M-Bus");
        assertMeterEvent(meterEvents.get(1), MeterEvent.COMMUNICATION_OK_MBUS, "Communication with the M-Bus meter connected to channel 1 of the M-Bus is ok again");
        assertMeterEvent(meterEvents.get(2), MeterEvent.REPLACE_BATTERY_MBUS,  "Battery must be exchanged due to the expected end of life time on channel 1");
        assertMeterEvent(meterEvents.get(3), MeterEvent.FRAUD_ATTEMPT_MBUS, "Fraud attempt has been registered on channel 1");
        assertMeterEvent(meterEvents.get(4), MeterEvent.CLOCK_ADJUSTED_MBUS, "Clock has been adjusted on channel 1");
        assertMeterEvent(meterEvents.get(5), MeterEvent.CONFIGURATIONCHANGE, "A new M-Bus Device has been detected on channel 1");
        assertMeterEvent(meterEvents.get(6), MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Permanent error on Mbus channel 1");
        assertMeterEvent(meterEvents.get(7), MeterEvent.EVENT_LOG_CLEARED, "Mbus event log profile cleared");

    }

    @Test
    public void testPossibleValueForChannel2() {
        Date date = new Date();
        DSMR40MbusEventLog eventLog = new DSMR40MbusEventLog(null, 2);
        List<MeterEvent> meterEvents = new ArrayList<>();
        for (int protocolCode=0; protocolCode<=255; protocolCode++) {
            eventLog.buildMeterEvent(meterEvents, date, protocolCode);
        }

        assertEquals(8, meterEvents.size());

        assertMeterEvent(meterEvents.get(0), MeterEvent.COMMUNICATION_ERROR_MBUS, "Communication problem when reading the meter connected to channel 2 of the M-Bus");
        assertMeterEvent(meterEvents.get(1), MeterEvent.COMMUNICATION_OK_MBUS, "Communication with the M-Bus meter connected to channel 2 of the M-Bus is ok again");
        assertMeterEvent(meterEvents.get(2), MeterEvent.REPLACE_BATTERY_MBUS,  "Battery must be exchanged due to the expected end of life time on channel 2");
        assertMeterEvent(meterEvents.get(3), MeterEvent.FRAUD_ATTEMPT_MBUS, "Fraud attempt has been registered on channel 2");
        assertMeterEvent(meterEvents.get(4), MeterEvent.CLOCK_ADJUSTED_MBUS, "Clock has been adjusted on channel 2");
        assertMeterEvent(meterEvents.get(5), MeterEvent.CONFIGURATIONCHANGE, "A new M-Bus Device has been detected on channel 2");
        assertMeterEvent(meterEvents.get(6), MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Permanent error on Mbus channel 2");
        assertMeterEvent(meterEvents.get(7), MeterEvent.EVENT_LOG_CLEARED, "Mbus event log profile cleared");

    }

    @Test
    public void testPossibleValueForChannel3() {
        Date date = new Date();
        DSMR40MbusEventLog eventLog = new DSMR40MbusEventLog(null, 3);
        List<MeterEvent> meterEvents = new ArrayList<>();
        for (int protocolCode=0; protocolCode<=255; protocolCode++) {
            eventLog.buildMeterEvent(meterEvents, date, protocolCode);
        }

        assertEquals(8, meterEvents.size());

        assertMeterEvent(meterEvents.get(0), MeterEvent.COMMUNICATION_ERROR_MBUS, "Communication problem when reading the meter connected to channel 3 of the M-Bus");
        assertMeterEvent(meterEvents.get(1), MeterEvent.COMMUNICATION_OK_MBUS, "Communication with the M-Bus meter connected to channel 3 of the M-Bus is ok again");
        assertMeterEvent(meterEvents.get(2), MeterEvent.REPLACE_BATTERY_MBUS,  "Battery must be exchanged due to the expected end of life time on channel 3");
        assertMeterEvent(meterEvents.get(3), MeterEvent.FRAUD_ATTEMPT_MBUS, "Fraud attempt has been registered on channel 3");
        assertMeterEvent(meterEvents.get(4), MeterEvent.CLOCK_ADJUSTED_MBUS, "Clock has been adjusted on channel 3");
        assertMeterEvent(meterEvents.get(5), MeterEvent.CONFIGURATIONCHANGE, "A new M-Bus Device has been detected on channel 3");
        assertMeterEvent(meterEvents.get(6), MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Permanent error on Mbus channel 3");
        assertMeterEvent(meterEvents.get(7), MeterEvent.EVENT_LOG_CLEARED, "Mbus event log profile cleared");

    }

    @Test
    public void testPossibleValueForChannel4() {
        Date date = new Date();
        DSMR40MbusEventLog eventLog = new DSMR40MbusEventLog(null, 4);
        List<MeterEvent> meterEvents = new ArrayList<>();
        for (int protocolCode=0; protocolCode<=255; protocolCode++) {
            eventLog.buildMeterEvent(meterEvents, date, protocolCode);
        }

        assertEquals(8, meterEvents.size());

        assertMeterEvent(meterEvents.get(0), MeterEvent.COMMUNICATION_ERROR_MBUS, "Communication problem when reading the meter connected to channel 4 of the M-Bus");
        assertMeterEvent(meterEvents.get(1), MeterEvent.COMMUNICATION_OK_MBUS, "Communication with the M-Bus meter connected to channel 4 of the M-Bus is ok again");
        assertMeterEvent(meterEvents.get(2), MeterEvent.REPLACE_BATTERY_MBUS,  "Battery must be exchanged due to the expected end of life time on channel 4");
        assertMeterEvent(meterEvents.get(3), MeterEvent.FRAUD_ATTEMPT_MBUS, "Fraud attempt has been registered on channel 4");
        assertMeterEvent(meterEvents.get(4), MeterEvent.CLOCK_ADJUSTED_MBUS, "Clock has been adjusted on channel 4");
        assertMeterEvent(meterEvents.get(5), MeterEvent.CONFIGURATIONCHANGE, "A new M-Bus Device has been detected on channel 4");
        assertMeterEvent(meterEvents.get(6), MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Permanent error on Mbus channel 4");
        assertMeterEvent(meterEvents.get(7), MeterEvent.EVENT_LOG_CLEARED, "Mbus event log profile cleared");

    }

    private void assertMeterEvent(MeterEvent meterEvent, int expectedEiCode, String expectedMessage) {
        assertEquals(expectedEiCode, meterEvent.getEiCode());
        assertEquals(expectedMessage, meterEvent.getMessage());
    }

}