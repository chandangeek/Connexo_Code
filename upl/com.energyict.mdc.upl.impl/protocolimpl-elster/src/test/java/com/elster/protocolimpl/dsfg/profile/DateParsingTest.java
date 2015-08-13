package com.elster.protocolimpl.dsfg.profile;

import com.elster.protocolimpl.dsfg.objects.ClockObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

/**
 * Created by heuckeg on 29.10.2014.
 */
public class DateParsingTest {

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
    public void testSuWiDateParsing1() throws ParseException {
        Date d;
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

        Date dates[] = {
                sdf.parse("26.10.2014 00:00:00"),
                sdf.parse("26.10.2014 01:00:00"),
                sdf.parse("26.10.2014 02:00:00"),
                sdf.parse("26.10.2014 02:00:00"),
                sdf.parse("26.10.2014 03:00:00")
        };

        String s = "";
        d = null;
        for (Date c : dates) {
            Date cc = ClockObject.checkDate(c, d, TimeZone.getTimeZone("Europe/Berlin"));
            s += cc.toString() + "\n";
        }

        String c = "Sun Oct 26 00:00:00 CEST 2014\n" +
                "Sun Oct 26 01:00:00 CEST 2014\n" +
                "Sun Oct 26 02:00:00 CET 2014\n" +
                "Sun Oct 26 02:00:00 CET 2014\n" +
                "Sun Oct 26 03:00:00 CET 2014\n";

        assertEquals(c, s);

    }

    @Test
    public void testSuWiDateParsing2() throws ParseException {
        Date d;
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

        Date base = sdf.parse("26.10.2014 00:00:00");
        Date dates[] = {
                new Date(base.getTime()),
                new Date(base.getTime() + 3600000),
                new Date(base.getTime() + 3600000 * 2),
                new Date(base.getTime() + 3600000 * 3),
                new Date(base.getTime() + 3600000 * 4)
        };

        String s = "";
        d = null;
        for (Date c : dates) {
            Date cc = ClockObject.checkDate(c, d, TimeZone.getTimeZone("Europe/Berlin"));
            s += cc.toString() + "\n";
        }

        String c = "Sun Oct 26 00:00:00 CEST 2014\n" +
                "Sun Oct 26 01:00:00 CEST 2014\n" +
                "Sun Oct 26 02:00:00 CEST 2014\n" +
                "Sun Oct 26 02:00:00 CET 2014\n" +
                "Sun Oct 26 03:00:00 CET 2014\n";

        assertEquals(c, s);

    }

    @Test
    public void testSuWiDateParsing3() throws ParseException {
        Date d;
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

        Date dates[] = {
                sdf.parse("26.10.2014 00:00:00"),
                sdf.parse("26.10.2014 01:00:00"),
                sdf.parse("26.10.2014 02:00:00"),
                sdf.parse("26.10.2014 02:00:00"),
                sdf.parse("26.10.2014 03:00:00")
        };

        String s = "";
        d = null;
        for (Date c : dates) {
            Date cc = ClockObject.checkDate(c, d, TimeZone.getTimeZone("Europe/Berlin"));
            s += cc.toString() + "\n";
            d = (Date) cc.clone();
        }

        String c = "Sun Oct 26 00:00:00 CEST 2014\n" +
                "Sun Oct 26 01:00:00 CEST 2014\n" +
                "Sun Oct 26 02:00:00 CEST 2014\n" +
                "Sun Oct 26 02:00:00 CET 2014\n" +
                "Sun Oct 26 03:00:00 CET 2014\n";

        assertEquals(c, s);

    }

    @Test
    public void testSuWi15minDateParsing() throws ParseException {
        Date d;
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

        Date dates[] = {
                sdf.parse("26.10.2014 01:00:00"),
                sdf.parse("26.10.2014 01:15:00"),
                sdf.parse("26.10.2014 01:30:00"),
                sdf.parse("26.10.2014 01:45:00"),
                sdf.parse("26.10.2014 02:00:00"),
                sdf.parse("26.10.2014 02:15:00"),
                sdf.parse("26.10.2014 02:30:00"),
                sdf.parse("26.10.2014 02:45:00"),
                sdf.parse("26.10.2014 02:00:00"),
                sdf.parse("26.10.2014 02:15:00"),
                sdf.parse("26.10.2014 02:30:00"),
                sdf.parse("26.10.2014 02:45:00"),
                sdf.parse("26.10.2014 03:00:00"),
                sdf.parse("26.10.2014 03:15:00")
        };

        String s = "";
        d = null;
        for (Date c : dates) {
            Date cc = ClockObject.checkDate(c, d, TimeZone.getTimeZone("Europe/Berlin"));
            s += cc.toString() + "\n";
            d = (Date) cc.clone();
        }

        String c = "Sun Oct 26 01:00:00 CEST 2014\n" +
                "Sun Oct 26 01:15:00 CEST 2014\n" +
                "Sun Oct 26 01:30:00 CEST 2014\n" +
                "Sun Oct 26 01:45:00 CEST 2014\n" +
                "Sun Oct 26 02:00:00 CEST 2014\n" +
                "Sun Oct 26 02:15:00 CEST 2014\n" +
                "Sun Oct 26 02:30:00 CEST 2014\n" +
                "Sun Oct 26 02:45:00 CEST 2014\n" +
                "Sun Oct 26 02:00:00 CET 2014\n" +
                "Sun Oct 26 02:15:00 CET 2014\n" +
                "Sun Oct 26 02:30:00 CET 2014\n" +
                "Sun Oct 26 02:45:00 CET 2014\n" +
                "Sun Oct 26 03:00:00 CET 2014\n" +
                "Sun Oct 26 03:15:00 CET 2014\n";

        assertEquals(c, s);
    }

    @Test
    public void testWiSuDateParsing() throws ParseException {
        Date d;
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

        Date dates[] = {
                sdf.parse("30.03.2014 00:00:00"),
                sdf.parse("30.03.2014 01:00:00"),
                sdf.parse("30.03.2014 03:00:00"),
                sdf.parse("30.03.2014 04:00:00"),
        };

        String s = "";
        d = null;
        for (Date c : dates) {
            Date cc = ClockObject.checkDate(c, d, TimeZone.getTimeZone("Europe/Berlin"));
            s += cc.toString() + "\n";
            d = (Date) cc.clone();
        }

        String c = "Sun Mar 30 00:00:00 CET 2014\n" +
                "Sun Mar 30 01:00:00 CET 2014\n" +
                "Sun Mar 30 03:00:00 CEST 2014\n" +
                "Sun Mar 30 04:00:00 CEST 2014\n";

        assertEquals(c, s);
    }
}
