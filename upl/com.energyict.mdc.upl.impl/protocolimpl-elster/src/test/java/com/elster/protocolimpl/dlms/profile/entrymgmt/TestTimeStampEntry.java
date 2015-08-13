package com.elster.protocolimpl.dlms.profile.entrymgmt;

import com.elster.dlms.types.basic.DlmsDate;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.basic.DlmsTime;
import com.elster.dlms.types.basic.ObisCode;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;

/**
 * User: heuckeg
 * Date: 15.04.13
 * Time: 13:47
 */
public class TestTimeStampEntry
{
    @Test
    public void testWithFullDateTimeInformation()
    {
        DlmsDate d = new DlmsDate(2013, 2, 28);
        DlmsTime t = new DlmsTime(12, 13, 14, 0);
        DlmsDateTime dt = new DlmsDateTime(d, t, 120, 0);

        TimeStampEntry tst = new TimeStampEntry(new ObisCode(1,2,3,4,5,6), 2);
        tst.setIndex(0);

        Integer stat = 0;
        Date date = tst.toDate(new Object[] {dt}, TimeZone.getTimeZone("GMT+2"), stat);

        SimpleDateFormat dtf = new SimpleDateFormat("yyyyMMddHHmmss");
        dtf.setTimeZone(TimeZone.getTimeZone("GMT0"));
        final String s = dtf.format(date);
        assertEquals("20130228101314", s);
    }

    @Test
    public void testWithDateTimeInformationWithoutDeviation()
    {
        DlmsDate d = new DlmsDate(2013, 2, 28);
        DlmsTime t = new DlmsTime(12, 13, 14, 0);
        DlmsDateTime dt = new DlmsDateTime(d, t);

        TimeStampEntry tst = new TimeStampEntry(new ObisCode(1,2,3,4,5,6), 2);
        tst.setIndex(0);

        Integer stat = 0;
        Date date = tst.toDate(new Object[] {dt}, TimeZone.getTimeZone("GMT+2"), stat);

        SimpleDateFormat dtf = new SimpleDateFormat("yyyyMMddHHmmss");
        dtf.setTimeZone(TimeZone.getTimeZone("GMT"));
        final String s = dtf.format(date);
        assertEquals("20130228101314", s);
    }
}
