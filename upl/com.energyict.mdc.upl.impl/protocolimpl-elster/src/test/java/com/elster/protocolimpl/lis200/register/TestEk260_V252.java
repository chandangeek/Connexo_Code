package com.elster.protocolimpl.lis200.register;

import com.elster.protocolimpl.lis200.EK260;
import com.elster.protocolimpl.lis200.objects.ClockObject;
import com.elster.protocolimpl.lis200.objects.GenericArchiveObject;
import com.elster.protocolimpl.lis200.registers.HistoricalArchive;
import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
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
public class TestEk260_V252 extends EK260 {

    @Test
    public void RegisterReaderTestWithDLData() throws IOException {

        StringBuilder sb = new StringBuilder();

        RegisterValue rv;

        setSoftwareVersion(252);
        setMeterIndex(1);

        /* volume */
        for (int i = 1; i <= 15; i++) {
            rv = this.readRegister(new ObisCode(7, 1, 13, 0, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 11, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 13, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 11, 0, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 13, 54, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 13, 60, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 13, 56, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 11, 62, 0, i));
            sb.append(rv);
            sb.append("\n");
        }

        /* analogue values */
        for (int i = 1; i <= 15; i++) {
            rv = this.readRegister(new ObisCode(7, 0, 43, 57, 255, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 128, 43, 22, 255, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 43, 55, 255, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 128, 43, 20, 255, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 42, 42, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 42, 57, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 42, 54, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 41, 42, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 41, 57, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 41, 54, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 52, 42, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 53, 42, 0, i));
            sb.append(rv);
            sb.append("\n");
        }

        String compareData = getResourceAsString("/com/elster/protocolimpl/lis200/register/ek260_2V52_registertest.txt");

        Unit degCelsius = Unit.get(BaseUnit.DEGREE_CELSIUS);
        String c = compareData.replaceAll("--CC--", degCelsius.toString());

        assertEquals(c, sb.toString());
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
            return new HistoricalArchive(new MyMonthlyArchive(this, 1));
        } else if (instance == 2) {
            return new HistoricalArchive(new MyMonthlyArchive(this, 2));
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
        } catch (IOException ignore) {

        }

        return stringBuilder.toString();
    }

    public class MyMonthlyArchive extends GenericArchiveObject {

        private TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");

        private HashMap<Date, String> archiveData;

        private String units;

        public MyMonthlyArchive(ProtocolLink link, int archiveInstance) {

            super(link, archiveInstance);

            archiveData = new HashMap<Date, String>();

            if (archiveInstance == 1) {
                //(GONr)(AONr)(Zeit)(Vn [m3])(VnG [m3])(VnMP [m3])(Zeit)(Stat)(VnTg [m3])(Zeit)(Stat)(Vb [m3])(VbG [m3])(VbMP [m3])(Zeit)(Stat)(VbTg [m3])(Zeit)(Stat)(St.2)(St.4)(Check)

                units = "()()()(m3)(m3)(m3)()()(m3)()()(m3)(m3)(m3)())(m3)()()()()()";

                archiveData.put(makeDate("2010-03-01,06:00:00"), "(17650)(20)(2010-03-01,06:00:00)(348503.3106)(348503.3106)(114.4272)(2010-02-10,14:00:00)(0)(2094.0193)(2010-02-02,06:00:00)(0)(2569877.2)(2569877.2)(119)(2010-02-10,14:00:00)(0)(2182)(2010-02-02,06:00:00)(0)(0)(14)(CRC Ok)");
                archiveData.put(makeDate("2010-04-01,06:00:00"), "(18627)(21)(2010-04-01,06:00:00)(378557.2891)(378557.2891)(115.2066)(2010-03-11,08:00:00)(0)(1714.2547)(2010-03-07,06:00:00)(0)(2601390.2)(2601390.2)(119)(2010-03-15,12:00:00)(0)(1779)(2010-03-07,06:00:00)(0)(0)(14)(CRC Ok)");
                archiveData.put(makeDate("2010-05-01,06:00:00"), "(19477)(22)(2010-05-01,06:00:00)(392973.1657)(392973.1657)(70.8365)(2010-04-06,07:00:00)(0)(1030.9541)(2010-04-05,06:00:00)(0)(2616741.2)(2616741.2)(74)(2010-04-06,07:00:00)(0)(1098)(2010-04-04,06:00:00)(0)(0)(14)(CRC Ok)");
                archiveData.put(makeDate("2010-06-01,06:00:00"), "(20345)(23)(2010-06-01,06:00:00)(393769.9246)(393769.9246)(43.5625)(2010-05-04,05:00:00)(0)(662.6105)(2010-05-04,06:00:00)(0)(2617586.2)(2617586.2)(46)(2010-05-04,05:00:00)(0)(703)(2010-05-04,06:00:00)(0)(0)(14)(CRC Ok)");
                archiveData.put(makeDate("2010-07-01,06:00:00"), "(21185)(24)(2010-07-01,06:00:00)(393769.9246)(393769.9246)(0)(2010-07-01,06:00:00)(0)(0)(2010-07-01,06:00:00)(0)(2617586.6)(2617586.6)(0)(2010-07-01,06:00:00)(0)(0)(2010-07-01,06:00:00)(0)(0)(14)(CRC Ok)");
                archiveData.put(makeDate("2010-08-01,06:00:00"), "(22132)(25)(2010-08-01,06:00:00)(393769.9246)(393769.9246)(0)(2010-08-01,06:00:00)(0)(0)(2010-08-01,06:00:00)(0)(2617586.6)(2617586.6)(0)(2010-08-01,06:00:00)(0)(0)(2010-08-01,06:00:00)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2010-09-01,06:00:00"), "(23096)(26)(2010-09-01,06:00:00)(393800.6907)(393800.6907)(18.2103)(2010-08-31,09:00:00)(0)(29.7929)(2010-09-01,06:00:00)(0)(2617618.6)(2617618.6)(19)(2010-08-31,09:00:00)(0)(31)(2010-09-01,06:00:00)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2010-10-01,06:00:00"), "(24006)(27)(2010-10-01,06:00:00)(393803.5163)(393803.5163)(2.8256)(2010-09-07,15:00:00)(0)(2.8256)(2010-09-08,06:00:00)(0)(2617621.6)(2617621.6)(3)(2010-09-07,15:00:00)(0)(3)(2010-09-08,06:00:00)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2010-11-01,06:00:00"), "(25044)(28)(2010-11-01,06:00:00)(393987.0396)(393987.0396)(37.9214)(2010-10-22,09:00:00)(0)(98.0748)(2010-10-23,06:00:00)(0)(2617810.6)(2617810.6)(39)(2010-10-22,09:00:00)(0)(101)(2010-10-23,06:00:00)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2010-12-01,06:00:00"), "(26030)(29)(2010-12-01,06:00:00)(395854.5976)(395854.5976)(31.7824)(2010-11-15,08:00:00)(0)(551.641)(2010-11-13,06:00:00)(0)(2619811.6)(2619811.6)(34)(2010-11-15,10:00:00)(0)(590)(2010-11-13,06:00:00)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2011-01-01,06:00:00"), "(27106)(30)(2011-01-01,06:00:00)(417105.5906)(417105.5906)(61.9874)(2010-12-20,11:00:00)(0)(1374.6425)(2010-12-22,06:00:00)(0)(2641869.6)(2641869.6)(65)(2010-12-20,11:00:00)(0)(1458)(2010-12-24,06:00:00)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2011-02-01,06:00:00"), "(28138)(31)(2011-02-01,06:00:00)(440549.2567)(440549.2567)(62.1606)(2011-01-21,11:00:00)(0)(1149.4766)(2011-01-04,06:00:00)(0)(2666266.6)(2666266.6)(64)(2011-01-31,16:00:00)(0)(1203)(2011-01-08,06:00:00)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2011-03-01,06:00:00"), "(29050)(32)(2011-03-01,06:00:00)(458007.4825)(458007.4825)(62.5205)(2011-02-02,09:00:00)(0)(1049.1748)(2011-02-03,06:00:00)(0)(2684501.6)(2684501.6)(64)(2011-02-22,10:00:00)(0)(1075)(2011-02-03,06:00:00)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2011-04-01,06:00:00"), "(30213)(33)(2011-04-01,06:00:00)(467538.0035)(467538.0035)(101.5322)(2011-03-11,08:00:00)(0)(865.2093)(2011-03-10,06:00:00)(0)(2694428.6)(2694428.6)(106)(2011-03-11,08:00:00)(0)(908)(2011-03-10,06:00:00)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2011-05-01,06:00:00"), "(31342)(34)(2011-05-01,06:00:00)(468934.4434)(468934.4434)(52.5631)(2011-04-14,09:00:00)(0)(398.1081)(2011-04-18,06:00:00)(0)(2695909.6)(2695909.6)(55)(2011-04-14,09:00:00)(0)(423)(2011-04-18,06:00:00)(0)(0)(0)(CRC Ok)");
            }

            if (archiveInstance == 2) {
                // (GONr)(AONr)(Zeit)(Qn [m3|h])(Zeit)(Stat)(Qn [m3|h])(Zeit)(Stat)(Qb [m3|h])(Zeit)(Stat)(Qb [m3|h])(Zeit)(Stat)(p.Mon [ bar])(p.Mon [ bar])(Zeit)(Stat)(p.Mon [ bar])(Zeit)(Stat)(T.Mon [{C])(T.Mon [{C])(Zeit)(Stat)(T.Mon [{C])(Zeit)(Stat)(K.Mon)(Z.Mon)(St.7)(St.6)(St.8)(St.5)(Check)
                units = "()()()(m3/h)()()(m3/h)()()(m3/h)()()(m3/h)()()(bar)(bar)()()(bar)()()\u00B0C)(\u00B0C)()()(\u00B0C)()()()()()()()()()";

                archiveData.put(makeDate("2010-03-01,06:00:00"), "(17650)(20)(2010-03-01,06:00:00)(120.99)(2010-02-01,20:43:34)(0)(0)(2010-02-27,17:16:10)(0)(126)(2010-02-01,20:43:46)(0)(0)(2010-02-27,17:16:10)(0)(1.06295)(1.07169)(2010-02-02,04:19:54)(0)(1.02374)(2010-02-28,11:04:56)(0)(27.38)(31.29)(2010-02-26,02:42:10)(0)(17.78)(2010-02-02,13:02:38)(0)(0.98774)(0.94759)(0)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2010-04-01,06:00:00"), "(18627)(21)(2010-04-01,06:00:00)(123.55)(2010-03-06,20:34:32)(0)(0)(2010-03-31,14:11:52)(0)(126)(2010-03-07,23:39:38)(0)(0)(2010-03-31,14:11:52)(0)(1.06206)(1.08724)(2010-03-07,10:31:12)(0)(1.03998)(2010-03-30,14:50:04)(0)(27.97)(36.73)(2010-03-29,20:21:06)(0)(15.91)(2010-03-13,17:14:38)(0)(0.98728)(0.94775)(0)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2010-05-01,06:00:00"), "(19477)(22)(2010-05-01,06:00:00)(122.97)(2010-04-21,13:21:48)(0)(0)(2010-05-01,06:00:00)(0)(128)(2010-04-21,13:22:06)(0)(0)(2010-05-01,06:00:00)(0)(1.06429)(1.08518)(2010-04-09,12:43:34)(0)(1.04577)(2010-04-15,06:39:44)(0)(31.17)(40.6)(2010-04-25,18:24:50)(0)(20.82)(2010-04-21,10:11:02)(0)(0.98742)(0.94029)(0)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2010-06-01,06:00:00"), "(20345)(23)(2010-06-01,06:00:00)(117.6)(2010-05-04,06:03:48)(0)(0)(2010-06-01,06:00:00)(0)(124)(2010-05-04,06:11:42)(0)(0)(2010-06-01,06:00:00)(0)(1.06077)(1.08114)(2010-05-21,08:19:04)(0)(1.04707)(2010-05-06,12:49:48)(0)(27.02)(34.2)(2010-05-02,13:04:34)(0)(20.03)(2010-05-31,06:09:00)(0)(0.98728)(0.96277)(0)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2010-07-01,06:00:00"), "(21185)(24)(2010-07-01,06:00:00)(0)(2010-07-01,06:00:00)(0)(0)(2010-07-01,06:00:00)(0)(0)(2010-07-01,06:00:00)(0)(0)(2010-07-01,06:00:00)(0)(1.06519)(1.07664)(2010-06-23,04:34:32)(0)(1.05218)(2010-06-10,19:52:36)(0)(33.52)(37)(2010-06-27,19:27:32)(0)(22.48)(2010-06-21,05:51:44)(0)(0.98743)(0.94755)(0)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2010-08-01,06:00:00"), "(22132)(25)(2010-08-01,06:00:00)(0)(2010-08-01,06:00:00)(0)(0)(2010-08-01,06:00:00)(0)(0)(2010-08-01,06:00:00)(0)(0)(2010-08-01,06:00:00)(0)(1.06395)(1.07756)(2010-07-06,08:05:18)(0)(1.01597)(2010-07-13,14:10:04)(0)(30.51)(37.7)(2010-07-11,20:22:26)(0)(26.16)(2010-07-30,06:03:58)(0)(0.98728)(0.95286)(0)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2010-09-01,06:00:00"), "(23096)(26)(2010-09-01,06:00:00)(121.04)(2010-08-31,08:51:10)(0)(0)(2010-09-01,06:00:00)(0)(126)(2010-08-31,08:51:10)(0)(0)(2010-09-01,06:00:00)(0)(1.06661)(1.07756)(2010-08-31,08:28:08)(0)(1.01063)(2010-08-10,05:34:34)(0)(29.65)(34.04)(2010-08-01,19:50:44)(0)(19.1)(2010-08-30,07:04:26)(0)(0.98728)(0.95468)(0)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2010-10-01,06:00:00"), "(24006)(27)(2010-10-01,06:00:00)(53.77)(2010-09-07,14:21:06)(0)(0)(2010-10-01,06:00:00)(0)(57)(2010-09-07,14:21:40)(0)(0)(2010-10-01,06:00:00)(0)(1.06451)(1.07748)(2010-09-13,06:48:56)(0)(1.04676)(2010-09-24,13:32:14)(0)(28.34)(32.63)(2010-09-06,16:35:00)(0)(20.12)(2010-09-27,05:47:20)(0)(0.98856)(0.96448)(0)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2010-11-01,06:00:00"), "(25044)(28)(2010-11-01,06:00:00)(58.09)(2010-10-26,09:42:56)(0)(0)(2010-11-01,06:00:00)(0)(59)(2010-10-26,09:44:30)(0)(0)(2010-11-01,06:00:00)(0)(1.06107)(1.08366)(2010-10-26,09:10:30)(0)(1.04775)(2010-10-31,05:16:58)(0)(22.11)(32.83)(2010-10-08,16:35:16)(0)(16.55)(2010-10-18,05:51:28)(0)(0.98728)(0.96825)(0)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2010-12-01,06:00:00"), "(26030)(29)(2010-12-01,06:00:00)(54.96)(2010-11-14,13:41:26)(0)(0)(2010-12-01,06:00:00)(0)(59)(2010-11-14,13:42:20)(0)(0)(2010-12-01,06:00:00)(0)(1.06023)(1.07664)(2010-11-05,10:51:24)(0)(1.0239)(2010-11-09,04:39:04)(0)(24.98)(33.93)(2010-11-14,19:34:16)(0)(15.69)(2010-12-01,06:00:00)(0)(0.98742)(0.96495)(0)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2011-01-01,06:00:00"), "(27106)(30)(2011-01-01,06:00:00)(75.69)(2010-12-02,15:57:18)(0)(0)(2010-12-20,12:59:22)(0)(78)(2010-12-02,15:57:22)(0)(0)(2010-12-20,12:59:22)(0)(1.06327)(1.08053)(2010-12-10,11:51:36)(0)(0.99439)(2010-12-01,14:57:28)(0)(19.62)(26.83)(2010-12-24,09:35:14)(0)(12.92)(2010-12-02,11:10:28)(0)(0.98728)(0.97724)(0)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2011-02-01,06:00:00"), "(28138)(31)(2011-02-01,06:00:00)(62.24)(2011-01-17,07:51:30)(0)(0)(2011-01-17,07:38:50)(0)(64)(2011-01-17,15:13:52)(0)(0)(2011-01-17,07:38:50)(0)(1.06402)(1.08366)(2011-01-21,14:48:10)(0)(1.04394)(2011-01-06,14:57:16)(0)(26.21)(31.64)(2011-01-08,16:14:16)(0)(17.85)(2011-01-17,01:01:46)(0)(0.98728)(0.96996)(0)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2011-03-01,06:00:00"), "(29050)(32)(2011-03-01,06:00:00)(62.61)(2011-02-22,07:13:14)(0)(0)(2011-02-22,07:11:16)(0)(66)(2011-02-17,12:20:54)(0)(0)(2011-02-22,07:11:16)(0)(1.0653)(1.08351)(2011-02-02,12:53:14)(0)(1.04196)(2011-02-15,17:11:22)(0)(26.53)(33.66)(2011-02-14,16:09:10)(0)(14.13)(2011-02-22,05:58:56)(0)(0.98774)(0.96675)(0)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2011-04-01,06:00:00"), "(30213)(33)(2011-04-01,06:00:00)(129.01)(2011-03-08,14:05:26)(0)(0)(2011-04-01,06:00:00)(0)(133)(2011-03-08,17:09:14)(0)(0)(2011-04-01,06:00:00)(0)(1.06621)(1.09197)(2011-03-23,09:35:42)(0)(1.04829)(2011-03-17,05:56:08)(0)(25.54)(33.19)(2011-03-26,09:20:16)(0)(16.63)(2011-03-20,06:06:54)(0)(0.98728)(0.96587)(0)(0)(0)(0)(CRC Ok)");
                archiveData.put(makeDate("2011-05-01,06:00:00"), "(31342)(34)(2011-05-01,06:00:00)(87)(2011-04-14,07:54:32)(0)(0)(2011-05-01,06:00:00)(0)(91)(2011-04-14,08:07:32)(0)(0)(2011-05-01,06:00:00)(0)(1.06499)(1.07908)(2011-04-06,11:17:08)(0)(1.05073)(2011-04-30,17:53:40)(0)(30.21)(36.42)(2011-04-23,18:20:58)(0)(21.53)(2011-04-13,01:03:00)(0)(0.98742)(0.94462)(0)(0)(0)(0)(CRC Ok)");
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