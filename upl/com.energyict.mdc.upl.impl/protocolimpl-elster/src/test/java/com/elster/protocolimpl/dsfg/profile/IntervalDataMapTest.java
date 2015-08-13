package com.elster.protocolimpl.dsfg.profile;

import com.elster.protocolimpl.dsfg.telegram.DataElement;
import com.energyict.protocol.IntervalData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class IntervalDataMapTest {

    private Locale savedLocale;

    @Before
    public void setup() {
        savedLocale = Locale.getDefault();
        Locale.setDefault(Locale.GERMANY);
    }

    @After
    public void tearDown() {
        if (!Locale.getDefault().equals(savedLocale)) {
            Locale.setDefault(savedLocale);
        }
    }

	@Test
	public void testBuildMap1() throws ParseException
    {
		IntervalDataMap idm = new IntervalDataMap();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

        Date d = sdf.parse("26.10.2014 00:00:00");
        DataElement de = new DataElement("aaaaa", (double) 10, d.getTime() / 1000, 1L, 0);
		idm.addElement(d, 0, de);
		
		String c = "[Sun Oct 26 00:00:00 CEST 2014<aaaaa,10.0,544C1D60,1,0>]\n\r";
		
        String s = idm.toString();
        assertEquals(c, s);
	}
	
	@Test
	public void testBuildMap2() throws ParseException
    {
		IntervalDataMap idm = new IntervalDataMap();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

        Date d = sdf.parse("26.10.2014 00:00:00");
		idm.addElement(d, 0, new DataElement("aaaaa", (double) 10, d.getTime() / 1000, 1L, 0));
        d = sdf.parse("26.10.2014 01:00:00");
		idm.addElement(d, 0, new DataElement("aaaaa", (double) 20, d.getTime() / 1000, 2L, 0));
		
		String c = "[Sun Oct 26 00:00:00 CEST 2014<aaaaa,10.0,544C1D60,1,0>]\n\r" +
		           "[Sun Oct 26 01:00:00 CEST 2014<aaaaa,20.0,544C2B70,2,0>]\n\r";
		
        String s = idm.toString();
        assertEquals(c, s);
	}
	
	@Test
	public void testBuildMap3() throws ParseException
    {
        Date d;
		IntervalDataMap idm = new IntervalDataMap();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

        d = sdf.parse("26.10.2014 01:00:00");
        idm.addElement(d, 0, new DataElement("aaaaa", (double) 30, d.getTime() / 1000, 3L, 0));
        d = sdf.parse("26.10.2014 00:00:00");
        idm.addElement(d, 0, new DataElement("aaaaa", (double) 20, d.getTime() / 1000, 2L, 0));
        d = sdf.parse("25.10.2014 23:00:00");
        idm.addElement(d, 0, new DataElement("aaaaa", (double) 10, d.getTime() / 1000, 1L, 0));

		String c = "[Sat Oct 25 23:00:00 CEST 2014<aaaaa,10.0,544C0F50,1,0>]\n\r" +
                "[Sun Oct 26 00:00:00 CEST 2014<aaaaa,20.0,544C1D60,2,0>]\n\r" +
                "[Sun Oct 26 01:00:00 CEST 2014<aaaaa,30.0,544C2B70,3,0>]\n\r";

        String s = idm.toString();
        assertEquals(c, s);
	}
	
	@Test
	public void testBuildMap4() throws ParseException
    {
        Date d;
        IntervalDataMap idm = new IntervalDataMap();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

        d = sdf.parse("26.10.2014 00:00:00");
        idm.addElement(d, 0, new DataElement("aaaaa", (double) 30, d.getTime() / 1000, 3L, 0));
        d = sdf.parse("25.10.2014 23:00:00");
        idm.addElement(d, 0, new DataElement("aaaaa", (double) 20, d.getTime() / 1000, 2L, 0));
        d = sdf.parse("25.10.2014 22:00:00");
        idm.addElement(d, 0, new DataElement("aaaaa", (double) 10, d.getTime() / 1000, 1L, 0));

        d = new Date(sdf.parse("25.10.2014 23:00:00").getTime());
        idm.addElement(d, 1, new DataElement("aaaab", (double) 21, d.getTime() / 1000, 2L, 0));
        d = new Date(sdf.parse("26.10.2014 00:00:00").getTime());
        idm.addElement(d, 1, new DataElement("aaaab", (double) 31, d.getTime() / 1000, 3L, 0));
        d = new Date(sdf.parse("26.10.2014 01:00:00").getTime());
        idm.addElement(d, 1, new DataElement("aaaab", (double) 41, d.getTime() / 1000, 4L, 0));

		String c = "[Sat Oct 25 22:00:00 CEST 2014<aaaaa,10.0,544C0140,1,0>]\n\r" +
                "[Sat Oct 25 23:00:00 CEST 2014<aaaaa,20.0,544C0F50,2,0;aaaab,21.0,544C0F50,2,0>]\n\r" +
                "[Sun Oct 26 00:00:00 CEST 2014<aaaaa,30.0,544C1D60,3,0;aaaab,31.0,544C1D60,3,0>]\n\r" +
                "[Sun Oct 26 01:00:00 CEST 2014<-;aaaab,41.0,544C2B70,4,0>]\n\r";

        String s = idm.toString();
        assertEquals(c, s);
	}

    @Test
    public void testBuildMap5() throws ParseException
    {
        Date d;
        IntervalDataMap idm = new IntervalDataMap();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

        d = sdf.parse("26.10.2014 00:00:00");
        idm.addElement(d, 0, new DataElement("aaaaa", (double) 30, d.getTime() / 1000, 3L, 0));
        d = sdf.parse("25.10.2014 23:00:00");
        idm.addElement(d, 0, new DataElement("aaaaa", (double) 20, d.getTime() / 1000, 2L, 0));
        d = sdf.parse("25.10.2014 22:00:00");
        idm.addElement(d, 0, new DataElement("aaaaa", (double) 10, d.getTime() / 1000, 1L, 0));

        d = sdf.parse("25.10.2014 23:00:00");
        idm.addElement(d, 1, new DataElement("aaaab", (double) 21, d.getTime() / 1000, 2L, 0));
        d = sdf.parse("26.10.2014 00:00:00");
        idm.addElement(d, 1, new DataElement("aaaab", (double) 31, d.getTime() / 1000, 3L, 0));
        d = sdf.parse("26.10.2014 01:00:00");
        idm.addElement(d, 1, new DataElement("aaaab", (double) 41, d.getTime() / 1000, 4L, 0));
        d = new Date(d.getTime() + 3600000);
        idm.addElement(d, 1, new DataElement("aaaab", (double) 51, d.getTime() / 1000, 5L, 0));
        d = new Date(d.getTime() + 3600000);
        idm.addElement(d, 1, new DataElement("aaaab", (double) 61, d.getTime() / 1000, 6L, 0));

        List<IntervalData> ivds = idm.buildIntervalData(TimeZone.getTimeZone("GMT1"));

        String s = "";
        for (IntervalData ivd: ivds)
        {
            s = s + ivd.toString() + "\n\r";
        }

        String c = "Sat Oct 25 22:00:00 CEST 2014 0 0 Values: 10.0 0 0\n\r" +
        "Sat Oct 25 23:00:00 CEST 2014 0 0 Values: 20.0 0 021.0 0 0\n\r" +
        "Sun Oct 26 00:00:00 CEST 2014 0 0 Values: 30.0 0 031.0 0 0\n\r" +
        "Sun Oct 26 01:00:00 CEST 2014 0 0 Values: 0.0 0 25641.0 0 0\n\r" +
        "Sun Oct 26 02:00:00 CEST 2014 0 0 Values: 0.0 0 25651.0 0 0\n\r" +
        "Sun Oct 26 02:00:00 CET 2014 0 0 Values: 0.0 0 25661.0 0 0\n\r";

        assertEquals(c, s);
    }
}
