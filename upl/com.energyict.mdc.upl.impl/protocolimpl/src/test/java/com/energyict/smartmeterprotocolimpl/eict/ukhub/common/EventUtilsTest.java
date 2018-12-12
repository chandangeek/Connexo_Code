package com.energyict.smartmeterprotocolimpl.eict.ukhub.common;

import com.energyict.protocol.MeterEvent;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 9-sep-2011
 * Time: 15:09:32
 */
public class EventUtilsTest {

    @Test
    public void testRemoveDuplicateEvents() throws Exception {

        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

        Date date1 = new Date();
        Date date2 = new Date(date1.getTime() + 1000);
        Date date3 = new Date(date2.getTime() + 1000);

        meterEvents.add(new MeterEvent(date1, 1, 2, "TestMessage"));
        meterEvents.add(new MeterEvent(date1, 1, 2, "TestMessage"));
        meterEvents.add(new MeterEvent(date1, 2, 2, "TestMessage"));
        meterEvents.add(new MeterEvent(date2, 1, 2, "TestMessage"));
        meterEvents.add(new MeterEvent(date2, 1, 2, "TestMessage"));
        meterEvents.add(new MeterEvent(date2, 2, 2, "TestMessage"));
        meterEvents.add(new MeterEvent(date3, 1, 2, "TestMessage"));
        meterEvents.add(new MeterEvent(date3, 1, 2, "TestMessage"));
        meterEvents.add(new MeterEvent(date3, 2, 2, "TestMessage"));

        EventUtils.removeDuplicateEvents(meterEvents);
        assertEquals(6, meterEvents.size());

        assertEquals(date1, meterEvents.get(0).getTime());
        assertEquals(1, meterEvents.get(0).getEiCode());
        assertEquals(date1, meterEvents.get(1).getTime());
        assertEquals(2, meterEvents.get(1).getEiCode());

        assertEquals(date2, meterEvents.get(2).getTime());
        assertEquals(1, meterEvents.get(2).getEiCode());
        assertEquals(date2, meterEvents.get(3).getTime());
        assertEquals(2, meterEvents.get(3).getEiCode());

        assertEquals(date3, meterEvents.get(4).getTime());
        assertEquals(1, meterEvents.get(4).getEiCode());
        assertEquals(date3, meterEvents.get(5).getTime());
        assertEquals(2, meterEvents.get(5).getEiCode());
    }


    @Test
    public void testRemoveStoredEvents() throws Exception {

        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

        Date date1 = new Date();
        Date date1_1 = new Date(date1.getTime()+1);
        Date date2 = new Date(date1.getTime() + 1000);
        Date date2_1 = new Date(date2.getTime()+1);
        Date date3 = new Date(date2.getTime() + 1000);
        Date date3_1 = new Date(date3.getTime()+1);

        meterEvents.add(new MeterEvent(date1, 1, 2, "TestMessage"));
        meterEvents.add(new MeterEvent(date1, 1, 2, "TestMessage"));
        meterEvents.add(new MeterEvent(date1_1, 2, 2, "TestMessage"));
        meterEvents.add(new MeterEvent(date2, 1, 2, "TestMessage"));
        meterEvents.add(new MeterEvent(date2, 1, 2, "TestMessage"));
        meterEvents.add(new MeterEvent(date2_1, 2, 2, "TestMessage"));
        meterEvents.add(new MeterEvent(date3, 1, 2, "TestMessage"));
        meterEvents.add(new MeterEvent(date3, 1, 2, "TestMessage"));
        meterEvents.add(new MeterEvent(date3_1, 2, 2, "TestMessage"));

        EventUtils.removeStoredEvents(meterEvents, date1);
        assertEquals(7, meterEvents.size());

        EventUtils.removeStoredEvents(meterEvents, date2);
        assertEquals(4, meterEvents.size());

        EventUtils.removeStoredEvents(meterEvents, date3);
        assertEquals(1, meterEvents.size());

    }

}
