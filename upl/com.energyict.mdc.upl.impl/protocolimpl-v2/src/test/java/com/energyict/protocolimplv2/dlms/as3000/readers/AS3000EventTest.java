package com.energyict.protocolimplv2.dlms.as3000.readers;


import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.dlms.as3000.readers.logbook.AS3000Event;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class AS3000EventTest {

    private Date eventTimeStamp;

    @Before
    public void setUp() {
        eventTimeStamp = new Date();
    }

    @Test
    public void defaultEvent() {
        eventTimeStamp = new Date();
        int eventId = 0x0;
        List<MeterEvent> meterEvents = AS3000Event.buildMeterEvents(eventTimeStamp, eventId);
        Assert.assertEquals(1, meterEvents.size());
        Assert.assertEquals(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId), meterEvents.get(0));
    }

    @Test
    public void newIntervalPowerDown() {
        eventTimeStamp = new Date();
        int eventId = 0x00000001;
        List<MeterEvent> meterEvents = AS3000Event.buildMeterEvents(eventTimeStamp, eventId);
        Assert.assertEquals(1, meterEvents.size());
        Assert.assertEquals(new MeterEvent(eventTimeStamp, MeterEvent.POWERDOWN, eventId, AS3000Event.NEW_INTERVAL_BECAUSE_POWER_DOWN.getDescription()), meterEvents.get(0));
    }

    @Test
    public void newIntervalPowerUp() {
        eventTimeStamp = new Date();
        int eventId = 0x00000002;
        List<MeterEvent> meterEvents = AS3000Event.buildMeterEvents(eventTimeStamp, eventId);
        Assert.assertEquals(1, meterEvents.size());
        Assert.assertEquals(new MeterEvent(eventTimeStamp, MeterEvent.POWERUP, eventId, AS3000Event.NEW_INTERVAL_POWER_UP_AND_VARIABLE_CHANGED.getDescription()), meterEvents.get(0));
    }

    @Test
    public void newIntervalPowerUpAndPowerDown() {
        eventTimeStamp = new Date();
        int eventId = 0x00000003;
        List<MeterEvent> meterEvents = AS3000Event.buildMeterEvents(eventTimeStamp, eventId);
        Assert.assertEquals(2, meterEvents.size());
        Assert.assertEquals(new MeterEvent(eventTimeStamp, MeterEvent.POWERDOWN, 1, AS3000Event.NEW_INTERVAL_BECAUSE_POWER_DOWN.getDescription()), meterEvents.get(0));
        Assert.assertEquals(new MeterEvent(eventTimeStamp, MeterEvent.POWERUP, 2, AS3000Event.NEW_INTERVAL_POWER_UP_AND_VARIABLE_CHANGED.getDescription()), meterEvents.get(1));
    }

    @Test
    public void newIntervalPowerUpAndPowerDownAndContactorMissing() {
        eventTimeStamp = new Date();
        int eventId = 0x00080003;
        List<MeterEvent> meterEvents = AS3000Event.buildMeterEvents(eventTimeStamp, eventId);
        Assert.assertEquals(3, meterEvents.size());
        Assert.assertEquals(new MeterEvent(eventTimeStamp, MeterEvent.POWERDOWN, 0x00000001, AS3000Event.NEW_INTERVAL_BECAUSE_POWER_DOWN.getDescription()), meterEvents.get(0));
        Assert.assertEquals(new MeterEvent(eventTimeStamp, MeterEvent.POWERUP, 0x00000002, AS3000Event.NEW_INTERVAL_POWER_UP_AND_VARIABLE_CHANGED.getDescription()), meterEvents.get(1));
        Assert.assertEquals(new MeterEvent(eventTimeStamp, MeterEvent.MANUAL_DISCONNECTION, 0x00080000, AS3000Event.CONTACTOR_SWITCHED_OFF.getDescription()), meterEvents.get(2));

    }
}