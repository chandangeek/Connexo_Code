package com.energyict.protocolimplv2.umi.ei4.profile;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class EI4UmiMeterReadingTest {

    @Test
    public void testCreateUmiMeterReading() {
        long reading = 1234;
        Date date = new Date();
        int statusFlags = 1;
        int rateFlags = 2;
        EI4UmiMeterReading event = new EI4UmiMeterReading(reading, date, statusFlags, rateFlags);
        assertEquals(reading, event.getReading());
        assertEquals(date.toString(), event.getTimestamp().toString());
        assertEquals(statusFlags, event.getStatusFlags());
        assertEquals(rateFlags, event.getRateFlags());

        EI4UmiMeterReading eventCopy = new EI4UmiMeterReading(event.getRaw());
        assertEquals(reading, eventCopy.getReading());
        assertEquals(date.toString(), eventCopy.getTimestamp().toString());
        assertEquals(statusFlags, eventCopy.getStatusFlags());
        assertEquals(rateFlags, eventCopy.getRateFlags());

        assertEquals(event, eventCopy);
    }

    @Test
    public void testCreateUmiMeterReadingFromRaw() {
        byte[] rawData =  {(byte)0xA9, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x08, (byte)0xB1, (byte)0x5A, (byte)0x28, (byte) 0x40, (byte)0x00, (byte)0x64, (byte)0x00};
        EI4UmiMeterReading reading = new EI4UmiMeterReading(rawData);
        assertEquals(reading.getReading(), 425);

    }

} 
