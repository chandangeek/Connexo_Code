package com.elster.protocolimpl.lis200.register;

import com.elster.protocolimpl.lis200.DL220;
import com.elster.protocolimpl.lis200.objects.ClockObject;
import com.elster.protocolimpl.lis200.objects.GenericArchiveObject;
import com.elster.protocolimpl.lis200.registers.HistoricalArchive;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;

/**
 * test case for historical register reading for class DL210
 * User: heuckeg
 * Date: 20.04.11
 * Time: 16:34
 */
public class TestDl220 extends DL220 {

    @Test
    public void RegisterReaderTestWithDLData() throws IOException {

        StringBuilder sb = new StringBuilder();

        RegisterValue rv;

        setMeterIndex(1);
        for (int i = 1; i <= 14; i++) {
            rv = this.readRegister(new ObisCode(7, 0, 23, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 128, 23, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 23, 56, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 23, 62, 0, i));
            sb.append(rv);
            sb.append("\n");
        }

        sb.append("\n");

        obisCodeMapper = null;
        setMeterIndex(2);
        for (int i = 1; i <= 14; i++) {
            rv = this.readRegister(new ObisCode(7, 0, 23, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 128, 23, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 23, 56, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 23, 62, 0, i));
            sb.append(rv);
            sb.append("\n");
        }


        String compareData = getResourceAsString("/com/elster/protocolimpl/lis200/register/dl220registertest.txt");
        assertEquals(compareData, sb.toString());
    }


    // *******************************************************************************************
    // * I R e g i s t e r R e a d a b l e
    // *******************************************************************************************/
    @Override
    public int getBeginOfDay() {
        return 6;
    }

    @Override
    public HistoricalArchive getHistoricalArchive(int instance) {
        if (instance == 1) {
            return new HistoricalArchive(new MyDl220MonthlyArchive(this, 1));
        } else if (instance == 2) {
            return new HistoricalArchive(new MyDl220MonthlyArchive(this, 3));
        } else {
            return null;
        }
    }

    // *******************************************************************************************
    // * P r o t o c o l L i n k
    // *******************************************************************************************/
    @Override
    public Date getCurrentDate() {

        /* for test use a fixed date */
        Calendar c = ClockObject.parseCalendar("2011-05-05,10:10:10", false, getTimeZone());
        return c.getTime();
    }

    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone("Europe/Berlin");
    }

    private String getResourceAsString(String resourceName) {

        StringBuilder stringBuilder = new StringBuilder();

        InputStream stream = TestRegisterReader.class.getResourceAsStream(resourceName);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));

        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            bufferedReader.close();
        } catch (IOException ignored) {

        }

        return stringBuilder.toString();
    }

    public class MyDl220MonthlyArchive extends GenericArchiveObject {

        private TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");

        private HashMap<Date, String> archiveData;

        private String units;

        public MyDl220MonthlyArchive(ProtocolLink link, int archiveInstance) {

            super(link, archiveInstance);

            archiveData = new HashMap<Date, String>();

            if (archiveInstance == 1) {
                // (GONr)(AONr)(ZEIT)(V1)(V1.P)(V1M.L)(ZEIT)(STAT)(V1T.L)(ZEIT)(STAT)(ST.1)(ST.SY)(Er.Ch)

                // archive type 50
                units = "()()()(m3)(m3)(m3)()()(m3)()()()()()";

                archiveData.put(makeDate("2010-04-01,06:00:00"), "(306)(1)(2010-04-01,06:00:00)(2)(2)(0)(2010-04-01,06:00:00)(0)(0)(2010-04-01,06:00:00)(0)(16)(13;15)(CRC Ok)");
                archiveData.put(makeDate("2010-04-15,13:34:37"), "(315)(2)(2010-04-15,13:34:37)(2)(2)(0)(2010-04-15,13:34:37)(0)(0)(2010-04-15,13:34:37)(0)(0)(13;15;16)(CRC Ok)");
                archiveData.put(makeDate("2010-05-01,06:00:00"), "(16811)(3)(2010-05-01,06:00:00)(1243417)(1154616)(518)(2010-04-16,11:18:00)(0)(86402)(2010-04-30,06:00:00)(0)(16)(13;15;16)(CRC Ok)");
                archiveData.put(makeDate("2010-06-01,06:00:00"), "(63198)(4)(2010-06-01,06:00:00)(3889512)(3800711)(324)(2010-05-20,14:08:24)(0)(86402)(2010-05-31,06:00:00)(0)(16)(15;16)(CRC Ok)");
                archiveData.put(makeDate("2010-07-01,06:00:00"), "(104798)(5)(2010-07-01,06:00:00)(6481563)(6392762)(256)(2010-06-21,08:46:16)(0)(86405)(2010-06-24,06:00:02)(0)(16)(9;15;16)(CRC Ok)");
                archiveData.put(makeDate("2010-08-01,06:00:00"), "(122161)(6)(2010-08-01,06:00:00)(9159942)(9071141)(3602)(2010-07-29,10:00:02)(0)(86434)(2010-07-13,06:00:00)(0)(16)(15;16)(CRC Ok)");
                archiveData.put(makeDate("2010-09-01,06:00:00"), "(136681)(7)(2010-09-01,06:00:00)(11838087)(11749286)(3601)(2010-08-31,21:00:00)(0)(86403)(2010-08-27,06:00:00)(0)(0)(15;16)(CRC Ok)");
                archiveData.put(makeDate("2010-10-01,06:00:00"), "(145454)(8)(2010-10-01,06:00:00)(14430136)(14341335)(3601)(2010-09-30,20:00:00)(0)(86402)(2010-09-30,06:00:00)(0)(0)(15;16)(CRC Ok)");
                archiveData.put(makeDate("2010-11-01,06:00:00"), "(154607)(9)(2010-11-01,06:00:00)(17112137)(17023336)(3601)(2010-10-31,23:00:00)(0)(90001)(2010-10-31,06:00:00)(0)(0)(15)(CRC Ok)");
                archiveData.put(makeDate("2010-12-01,06:00:00"), "(163367)(10)(2010-12-01,06:00:00)(19704186)(19615385)(3601)(2010-11-30,23:00:00)(0)(86402)(2010-12-01,06:00:00)(0)(0)(15)(CRC Ok)");
                archiveData.put(makeDate("2011-01-01,06:00:00"), "(172431)(11)(2011-01-01,06:00:00)(22382597)(22293796)(3601)(2010-12-31,12:00:00)(0)(86402)(2010-12-31,06:00:00)(0)(0)(15)(CRC Ok)");
                archiveData.put(makeDate("2011-02-01,06:00:00"), "(181508)(12)(2011-02-01,06:00:00)(24113153)(24024352)(3601)(2011-01-19,23:00:00)(0)(86402)(2011-01-20,06:00:00)(0)(0)(15)(CRC Ok)");
                archiveData.put(makeDate("2011-03-01,06:00:00"), "(189840)(13)(2011-03-01,06:00:00)(24301135)(24212334)(1674)(2011-02-28,17:00:00)(0)(11978)(2011-02-10,06:00:00)(0)(0)(15)(CRC Ok)");
                archiveData.put(makeDate("2011-04-01,06:00:00"), "(198894)(14)(2011-04-01,06:00:00)(24595115)(24506314)(1741)(2011-03-07,17:00:00)(0)(12685)(2011-03-29,06:00:00)(0)(0)(15;16)(CRC Ok)");
                archiveData.put(makeDate("2011-05-01,06:00:00"), "(207652)(15)(2011-05-01,06:00:00)(24929880)(24841079)(1770)(2011-04-06,17:00:00)(0)(17198)(2011-04-29,06:00:00)(0)(0)(15;16)(CRC Ok)");
            }

            if (archiveInstance == 3) {
                // (GONr)(AONr)(ZEIT)(V1)(V1.P)(V1M.L)(ZEIT)(STAT)(V1T.L)(ZEIT)(STAT)(ST.1)(ST.SY)(Er.Ch)

                // archive type 50
                units = "()()()(m3)(m3)(m3)()()(m3)()()()()()";

                archiveData.put(makeDate("2010-04-01,06:00:00"), "(306)(1)(2010-04-01,06:00:00)(5)(5)(0)(2010-04-01,06:00:00)(0)(0)(2010-04-01,06:00:00)(0)(16)(13;15)(CRC Ok)");
                archiveData.put(makeDate("2010-04-15,13:34:37"), "(315)(2)(2010-04-15,13:34:37)(5)(5)(0)(2010-04-15,13:34:37)(0)(0)(2010-04-15,13:34:37)(0)(16)(13;15;16)(CRC Ok)");
                archiveData.put(makeDate("2010-05-01,06:00:00"), "(16811)(3)(2010-05-01,06:00:00)(1243079)(1425768)(348)(2010-04-19,18:08:48)(0)(86402)(2010-04-30,06:00:00)(0)(0)(13;15;16)(CRC Ok)");
                archiveData.put(makeDate("2010-06-01,06:00:00"), "(63198)(4)(2010-06-01,06:00:00)(3889174)(4071863)(324)(2010-05-20,14:08:24)(0)(86402)(2010-05-31,06:00:00)(0)(0)(15;16)(CRC Ok)");
                archiveData.put(makeDate("2010-07-01,06:00:00"), "(104798)(5)(2010-07-01,06:00:00)(6481225)(6663914)(256)(2010-06-21,08:46:16)(0)(86405)(2010-06-24,06:00:02)(0)(0)(9;15;16)(CRC Ok)");
                archiveData.put(makeDate("2010-08-01,06:00:00"), "(122161)(6)(2010-08-01,06:00:00)(9159605)(9342294)(3602)(2010-07-29,10:00:02)(0)(86434)(2010-07-13,06:00:00)(0)(0)(15;16)(CRC Ok)");
                archiveData.put(makeDate("2010-09-01,06:00:00"), "(136681)(7)(2010-09-01,06:00:00)(11837796)(12020485)(3601)(2010-08-31,21:00:00)(0)(86403)(2010-08-27,06:00:00)(0)(0)(15;16)(CRC Ok)");
                archiveData.put(makeDate("2010-10-01,06:00:00"), "(145454)(8)(2010-10-01,06:00:00)(14429845)(14612534)(3601)(2010-09-30,20:00:00)(0)(86402)(2010-09-30,06:00:00)(0)(0)(15;16)(CRC Ok)");
                archiveData.put(makeDate("2010-11-01,06:00:00"), "(154607)(9)(2010-11-01,06:00:00)(17111846)(17294535)(3601)(2010-10-31,23:00:00)(0)(90001)(2010-10-31,06:00:00)(0)(0)(15)(CRC Ok)");
                archiveData.put(makeDate("2010-12-01,06:00:00"), "(163367)(10)(2010-12-01,06:00:00)(19703895)(19886584)(3601)(2010-11-30,23:00:00)(0)(86402)(2010-12-01,06:00:00)(0)(0)(15)(CRC Ok)");
                archiveData.put(makeDate("2011-01-01,06:00:00"), "(172431)(11)(2011-01-01,06:00:00)(22382306)(22564995)(3601)(2010-12-31,12:00:00)(0)(86402)(2010-12-31,06:00:00)(0)(0)(15)(CRC Ok)");
                archiveData.put(makeDate("2011-02-01,06:00:00"), "(181508)(12)(2011-02-01,06:00:00)(24113947)(24296636)(3601)(2011-01-19,23:00:00)(0)(86402)(2011-01-20,06:00:00)(0)(0)(15)(CRC Ok)");
                archiveData.put(makeDate("2011-03-01,06:00:00"), "(189840)(13)(2011-03-01,06:00:00)(24300399)(24483088)(1692)(2011-02-28,17:00:00)(0)(11980)(2011-02-10,06:00:00)(0)(0)(15)(CRC Ok)");
                archiveData.put(makeDate("2011-04-01,06:00:00"), "(198894)(14)(2011-04-01,06:00:00)(24594381)(24777070)(1764)(2011-03-07,17:00:00)(0)(12685)(2011-03-29,06:00:00)(0)(0)(15;16)(CRC Ok)");
                archiveData.put(makeDate("2011-05-01,06:00:00"), "(207652)(15)(2011-05-01,06:00:00)(24920437)(25103126)(1756)(2011-04-06,17:00:00)(0)(17198)(2011-04-29,06:00:00)(0)(0)(15;16)(CRC Ok)");
            }

        }

        private Date makeDate(String rawDate) {
            return ClockObject.parseCalendar(rawDate, false, timeZone).getTime();
        }

        @Override
        public String getIntervals(Date from, Date to, int blockCount) {

            StringBuilder s = new StringBuilder();
            Date[] dates = archiveData.keySet().toArray(new Date[archiveData.keySet().size()]);
            java.util.Arrays.sort(dates);
            for (Date d : dates) {
                if ((d.getTime() >= from.getTime()) &&
                        (d.getTime() < to.getTime())) {
                    s.append(archiveData.get(d));
                    s.append("\n\r");
                }
            }
            return s.toString();
        }

        @Override
        public String getUnits() {
            return units;
        }

    }
}