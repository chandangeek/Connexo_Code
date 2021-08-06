package com.energyict.protocolimplv2.umi.ei4.profile;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EI4UmiProfileTableTest {

    @Test
    public void testCreateUmiProfileTable() {
        List<EI4UmiMeterReading> table = new ArrayList<>();

        long reading = 1234;
        Date date = new Date();
        int statusFlags = 1;
        int rateFlags = 2;
        EI4UmiMeterReading event = new EI4UmiMeterReading(reading, date, statusFlags, rateFlags);

        long reading1 = 5678;
        Date date1 = new Date();
        int statusFlags1 = 3;
        int rateFlags1 = 4;
        EI4UmiMeterReading event1 = new EI4UmiMeterReading(reading1, date1, statusFlags1, rateFlags1);

        table.add(event);
        table.add(event1);

        EI4UmiProfileTable profileTable = new EI4UmiProfileTable(table);
        assertEquals(table, profileTable.getReadings());
        assertEquals(table.get(0), profileTable.getReadings().get(0));
        assertEquals(table.get(1), profileTable.getReadings().get(1));

        EI4UmiProfileTable profileTableCopy = new EI4UmiProfileTable(profileTable.getRaw());
        assertEquals(profileTable, profileTableCopy);
    }

} 
