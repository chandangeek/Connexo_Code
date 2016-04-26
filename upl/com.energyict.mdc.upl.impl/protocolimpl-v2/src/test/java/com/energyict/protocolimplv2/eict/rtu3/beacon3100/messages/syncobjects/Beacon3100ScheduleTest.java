package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;


@RunWith(MockitoJUnitRunner.class)
public class Beacon3100ScheduleTest {

    @Test
    public void testEquals() throws Exception {
        Beacon3100Schedule sh1 = new Beacon3100Schedule(1, "schedule1", "1234567890");
        Beacon3100Schedule sh2 = new Beacon3100Schedule(1, "schedule2", "1234567891");
        Beacon3100Schedule sh3 = new Beacon3100Schedule(1, "schedule1", "1234567890");

        assertTrue(sh1.equals(sh3));
        assertFalse(sh1.equals(sh2));
    }
}