package com.elster.protocolimpl.lis200.register;

import com.elster.protocolimpl.lis200.objects.GenericArchiveObject;
import com.elster.protocolimpl.lis200.registers.HistoricalArchive;
import org.junit.Test;

import java.io.IOException;
import java.text.*;
import java.util.*;

import static junit.framework.Assert.assertEquals;

/**
 * Test unit to test class HistoricalArchive
 * User: heuckeg
 * Date: 07.04.11
 * Time: 09:46
 * To change this template use File | Settings | File Templates.
 */
public class TestHistoricalArchive {

    @Test
    public void myGenericArchiveObjectTest() throws ParseException, IOException {

        GenericArchiveObject gao = new MyGenericArchiveObject(null, 1);

        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dfm.setTimeZone(TimeZone.getTimeZone("GMT0"));

        Date from = dfm.parse("2011-01-01 06:00:00");
        Date to = dfm.parse("2011-04-01 06:00:00");

        String s = gao.getIntervals(from, to, 1);

        String c = "(29904)(28)(2011-01-01,06:00:00)(1485112.756)(1485112.756)(32.6143)(2010-12-26,08:00:00)(0)(779.6518)(2010-12-27,06:00:00)(0)(3014821.65)(3014821.65)(36)(2011-01-01,06:00:00)(0)(864)(2011-01-01,06:00:00)(0)(0)(14)(CRC Ok)\n\r" +
                "(30888)(29)(2011-02-01,06:00:00)(1509023.2078)(1509023.2078)(32.6657)(2011-01-21,09:00:00)(0)(781.2835)(2011-01-22,06:00:00)(0)(3041605.65)(3041605.65)(36)(2011-02-01,06:00:00)(0)(864)(2011-02-01,06:00:00)(0)(0)(14)(CRC Ok)\n\r" +
                "(31757)(30)(2011-03-01,06:00:00)(1530590.9722)(1530590.9722)(32.646)(2011-02-02,08:00:00)(0)(782.4728)(2011-02-03,06:00:00)(0)(3065797.65)(3065797.65)(36.01)(2011-02-28,18:00:00)(0)(864.01)(2011-02-22,06:00:00)(0)(0)(14)(CRC Ok)\n\r";
        assertEquals(c, s);
    }

    @Test
    public void baseHistoricalArchiveTest() {

        HistoricalArchive arc = new HistoricalArchive(new MyGenericArchiveObject(null, 1));

        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT0"));
        date.set(Calendar.YEAR, 2010);
        date.set(Calendar.MONTH, 11);
        date.set(Calendar.DAY_OF_MONTH, 1);
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        String s1 = arc.getArchiveLine(date);
        assertEquals("(28902)(27)(2010-12-01,06:00:00)(1461328.6922)(1461328.6922)(32.1564)(2010-11-28,08:00:00)(0)(770.1637)(2010-11-20,06:00:00)(0)(2988037.65)(2988037.65)(36)(2010-12-01,06:00:00)(0)(864)(2010-12-01,06:00:00)(0)(0)(14)(CRC Ok)\n\r", s1);

        date.add(Calendar.MONTH, 1);
        s1 = arc.getArchiveLine(date);
        assertEquals("(29904)(28)(2011-01-01,06:00:00)(1485112.756)(1485112.756)(32.6143)(2010-12-26,08:00:00)(0)(779.6518)(2010-12-27,06:00:00)(0)(3014821.65)(3014821.65)(36)(2011-01-01,06:00:00)(0)(864)(2011-01-01,06:00:00)(0)(0)(14)(CRC Ok)\n\r", s1);

        date.add(Calendar.MONTH, 1);
        s1 = arc.getArchiveLine(date);
        assertEquals("(30888)(29)(2011-02-01,06:00:00)(1509023.2078)(1509023.2078)(32.6657)(2011-01-21,09:00:00)(0)(781.2835)(2011-01-22,06:00:00)(0)(3041605.65)(3041605.65)(36)(2011-02-01,06:00:00)(0)(864)(2011-02-01,06:00:00)(0)(0)(14)(CRC Ok)\n\r", s1);

        date.add(Calendar.MONTH, -1);
        s1 = arc.getArchiveLine(date);
        assertEquals("(29904)(28)(2011-01-01,06:00:00)(1485112.756)(1485112.756)(32.6143)(2010-12-26,08:00:00)(0)(779.6518)(2010-12-27,06:00:00)(0)(3014821.65)(3014821.65)(36)(2011-01-01,06:00:00)(0)(864)(2011-01-01,06:00:00)(0)(0)(14)(CRC Ok)\n\r", s1);
    }


}
