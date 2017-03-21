package com.elster.protocolimpl.lis200.register;

import com.elster.protocolimpl.lis200.EK260;
import com.elster.protocolimpl.lis200.objects.ClockObject;
import com.elster.protocolimpl.lis200.objects.GenericArchiveObject;
import com.elster.protocolimpl.lis200.registers.HistoricalArchive;
import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;

/**
 * test case for historical register reading for class DL210
 * User: heuckeg
 * Date: 20.04.11
 * Time: 16:34
 */
public class TestEk260_V111 extends EK260 {

    private Locale savedLocale;

    public TestEk260_V111(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Before
    public void setup() {
        savedLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @After
    public void tearDown() {
        if (!Locale.getDefault().equals(savedLocale)) {
            Locale.setDefault(savedLocale);
        }
    }

    @Test
    public void RegisterReaderTestWithDLData() throws IOException {
        StringBuilder sb = new StringBuilder();
        RegisterValue rv;

        setSoftwareVersion(111);
        setMeterIndex(1);

        /* volume */
        for (int i = 1; i <= 15; i++) {
            rv = this.readRegister(new ObisCode(7, 0, 13, 0, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = this.readRegister(new ObisCode(7, 0, 13, 2, 0, i));
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

        String compareData = getResourceAsString("/com/elster/protocolimpl/lis200/register/ek260_1V11_registertest.txt");

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

    private class MyMonthlyArchive extends GenericArchiveObject {

        private TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");

        private Map<Date, String> archiveData;

        private String units;

        MyMonthlyArchive(ProtocolLink link, int archiveInstance) {

            super(link, archiveInstance);

            archiveData = new HashMap<>();

            if (archiveInstance == 1) {
                //(GO.Nr)(AONr)(Zeit)(VnG [m3])(VnMP [m3])(Zeit)(Stat)(VnTg [m3])(Zeit)(Stat)(VbG [m3])(VbMP [m3])(Zeit)(Stat)(VbTg [m3])(Zeit)(Stat)(St.2)(St.4)(Er.Ch)
                units = "()()()(m3)(m3)()()(m3)()()(m3)(m3)()()(m3)()()()()()";

                archiveData.put(makeDate("2010-03-01,06:00:00"), "(99431)(108)(2010-03-01,06:00:00)(000144076.2083)(000000000.0000)(2010-03-01,06:00:00)(0)(000000000.0000)(2010-03-01,06:00:00)(0)(000154013.0000)(000000000.0000)(2010-03-01,06:00:00)(0)(000000000.0000)(2010-03-01,06:00:00)(0)(0)(0)(29928)");
                archiveData.put(makeDate("2010-04-01,06:00:00"), "(100175)(109)(2010-04-01,06:00:00)(000144076.2083)(000000000.0000)(2010-04-01,06:00:00)(0)(000000000.0000)(2010-04-01,06:00:00)(0)(000154013.0000)(000000000.0000)(2010-04-01,06:00:00)(0)(000000000.0000)(2010-04-01,06:00:00)(0)(0)(0)(57621)");
                archiveData.put(makeDate("2010-05-01,06:00:00"), "(100895)(110)(2010-05-01,06:00:00)(000144076.2083)(000000000.0000)(2010-05-01,06:00:00)(0)(000000000.0000)(2010-05-01,06:00:00)(0)(000154013.0000)(000000000.0000)(2010-05-01,06:00:00)(0)(000000000.0000)(2010-05-01,06:00:00)(0)(0)(0)(9772)");
                archiveData.put(makeDate("2010-06-01,06:00:00"), "(101639)(111)(2010-06-01,06:00:00)(000144076.2083)(000000000.0000)(2010-06-01,06:00:00)(0)(000000000.0000)(2010-06-01,06:00:00)(0)(000154013.0000)(000000000.0000)(2010-06-01,06:00:00)(0)(000000000.0000)(2010-06-01,06:00:00)(0)(0)(0)(11114)");
                archiveData.put(makeDate("2010-07-01,06:00:00"), "(102359)(112)(2010-07-01,06:00:00)(000144076.2083)(000000000.0000)(2010-07-01,06:00:00)(0)(000000000.0000)(2010-07-01,06:00:00)(0)(000154013.0000)(000000000.0000)(2010-07-01,06:00:00)(0)(000000000.0000)(2010-07-01,06:00:00)(0)(0)(0)(58034)");
                archiveData.put(makeDate("2010-08-01,06:00:00"), "(103103)(113)(2010-08-01,06:00:00)(000144076.2083)(000000000.0000)(2010-08-01,06:00:00)(0)(000000000.0000)(2010-08-01,06:00:00)(0)(000154013.0000)(000000000.0000)(2010-08-01,06:00:00)(0)(000000000.0000)(2010-08-01,06:00:00)(0)(0)(0)(8402)");
                archiveData.put(makeDate("2010-09-01,06:00:00"), "(103847)(114)(2010-09-01,06:00:00)(000144076.2083)(000000000.0000)(2010-09-01,06:00:00)(0)(000000000.0000)(2010-09-01,06:00:00)(0)(000154013.0000)(000000000.0000)(2010-09-01,06:00:00)(0)(000000000.0000)(2010-09-01,06:00:00)(0)(0)(0)(34626)");
                archiveData.put(makeDate("2010-10-01,06:00:00"), "(104567)(115)(2010-10-01,06:00:00)(000144076.2083)(000000000.0000)(2010-10-01,06:00:00)(0)(000000000.0000)(2010-10-01,06:00:00)(0)(000154013.0000)(000000000.0000)(2010-10-01,06:00:00)(0)(000000000.0000)(2010-10-01,06:00:00)(0)(0)(0)(7865)");
                archiveData.put(makeDate("2010-11-01,06:00:00"), "(105311)(116)(2010-11-01,06:00:00)(000144076.2083)(000000000.0000)(2010-11-01,06:00:00)(0)(000000000.0000)(2010-11-01,06:00:00)(0)(000154013.0000)(000000000.0000)(2010-11-01,06:00:00)(0)(000000000.0000)(2010-11-01,06:00:00)(0)(0)(0)(1884)");
                archiveData.put(makeDate("2010-12-01,06:00:00"), "(106031)(117)(2010-12-01,06:00:00)(000144076.2083)(000000000.0000)(2010-12-01,06:00:00)(0)(000000000.0000)(2010-12-01,06:00:00)(0)(000154013.0000)(000000000.0000)(2010-12-01,06:00:00)(0)(000000000.0000)(2010-12-01,06:00:00)(0)(0)(0)(64543)");
                archiveData.put(makeDate("2011-01-01,06:00:00"), "(106775)(118)(2011-01-01,06:00:00)(000144076.2083)(000000000.0000)(2011-01-01,06:00:00)(0)(000000000.0000)(2011-01-01,06:00:00)(0)(000154013.0000)(000000000.0000)(2011-01-01,06:00:00)(0)(000000000.0000)(2011-01-01,06:00:00)(0)(0)(0)(62077)");
                archiveData.put(makeDate("2011-02-01,06:00:00"), "(107519)(119)(2011-02-01,06:00:00)(000144076.2083)(000000000.0000)(2011-02-01,06:00:00)(0)(000000000.0000)(2011-02-01,06:00:00)(0)(000154013.0000)(000000000.0000)(2011-02-01,06:00:00)(0)(000000000.0000)(2011-02-01,06:00:00)(0)(0)(0)(2950)");
                archiveData.put(makeDate("2011-03-01,06:00:00"), "(108191)(120)(2011-03-01,06:00:00)(000144076.2083)(000000000.0000)(2011-03-01,06:00:00)(0)(000000000.0000)(2011-03-01,06:00:00)(0)(000154013.0000)(000000000.0000)(2011-03-01,06:00:00)(0)(000000000.0000)(2011-03-01,06:00:00)(0)(0)(0)(64319)");
                archiveData.put(makeDate("2011-04-01,06:00:00"), "(108935)(121)(2011-04-01,06:00:00)(000144076.2083)(000000000.0000)(2011-04-01,06:00:00)(0)(000000000.0000)(2011-04-01,06:00:00)(0)(000154013.0000)(000000000.0000)(2011-04-01,06:00:00)(0)(000000000.0000)(2011-04-01,06:00:00)(0)(0)(0)(54179)");
                archiveData.put(makeDate("2011-05-01,06:00:00"), "(109655)(122)(2011-05-01,06:00:00)(000144076.2083)(000000000.0000)(2011-05-01,06:00:00)(0)(000000000.0000)(2011-05-01,06:00:00)(0)(000154013.0000)(000000000.0000)(2011-05-01,06:00:00)(0)(000000000.0000)(2011-05-01,06:00:00)(0)(0)(0)(15531)");
            }

            if (archiveInstance == 2) {
                // (GO.Nr)(AONr)(Zeit)(Qn [m3|h])(Zeit)(Stat)(Qn [m3|h])(Zeit)(Stat)(Qb [m3|h])(Zeit)(Stat)(Qb [m3|h])(Zeit)(Stat)(p.Mon [ bar])(p.Mon [ bar])(Zeit)(Stat)(p.Mon [ bar])(Zeit)(Stat)(T.Mon [{C])(T.Mon [{C])(Zeit)(Stat)(T.Mon [{C])(Zeit)(Stat)(K.Mon)(Z.Mon)(St.7)(St.6)(St.8)(St.5)(Er.Ch)
                units = "()()()(m3/h)()()(m3/h)()()(m3/h)()()(m3/h)()()(bar)(bar)()()(bar)()()(\u00B0C)(\u00B0C)()()(\u00B0C)()()()()()()()()()";

                archiveData.put(makeDate("2010-03-01,06:00:00"), "(99431)(108)(2010-03-01,06:00:00)(111.1)(2010-03-11,11:00:00)(0)(22.2)(2010-03-22,22:00:00)(0)(110.0)(2010-03-11,11:00:00)(0)(22.0)(2010-03-22,22:00:00)(0)(0.99176)(1.00777)(2010-02-07,11:25:00)(0)(0.97162)(2010-02-28,11:35:00)(0)(17.50)(20.46)(2010-02-26,17:25:00)(0)(15.10)(2010-02-11,06:15:00)(0)(1.00045)(0.91938)(0)(0)(0)(0)(63867)");
                archiveData.put(makeDate("2010-04-01,06:00:00"), "(100175)(109)(2010-04-01,06:00:00)(0.0)(2010-04-01,06:00:00)(0)(0.0)(2010-04-01,06:00:00)(0)(0.0)(2010-04-01,06:00:00)(0)(0.0)(2010-04-01,06:00:00)(0)(1.00419)(1.01905)(2010-03-07,11:55:00)(0)(0.98042)(2010-03-30,16:40:00)(0)(18.81)(22.32)(2010-03-25,00:15:00)(0)(14.97)(2010-03-07,07:15:00)(0)(1.00046)(0.92674)(0)(0)(0)(0)(34582)");
                archiveData.put(makeDate("2010-05-01,06:00:00"), "(100895)(110)(2010-05-01,06:00:00)(0.0)(2010-05-01,06:00:00)(0)(0.0)(2010-05-01,06:00:00)(0)(0.0)(2010-05-01,06:00:00)(0)(0.0)(2010-05-01,06:00:00)(0)(1.00547)(1.01886)(2010-04-09,10:00:00)(0)(0.99495)(2010-04-01,06:00:00)(0)(20.87)(25.09)(2010-04-30,20:00:00)(0)(17.01)(2010-04-01,06:10:00)(0)(1.00060)(0.92137)(0)(0)(0)(0)(5465)");
                archiveData.put(makeDate("2010-06-01,06:00:00"), "(101639)(111)(2010-06-01,06:00:00)(0.0)(2010-06-01,06:00:00)(0)(0.0)(2010-06-01,06:00:00)(0)(0.0)(2010-06-01,06:00:00)(0)(0.0)(2010-06-01,06:00:00)(0)(1.00053)(1.01618)(2010-05-21,00:25:00)(0)(0.98845)(2010-05-11,16:45:00)(0)(22.40)(27.12)(2010-05-25,21:45:00)(0)(18.38)(2010-05-31,02:55:00)(0)(1.00067)(0.91206)(0)(0)(0)(0)(40373)");
                archiveData.put(makeDate("2010-07-01,06:00:00"), "(102359)(112)(2010-07-01,06:00:00)(0.0)(2010-07-01,06:00:00)(0)(0.0)(2010-07-01,06:00:00)(0)(0.0)(2010-07-01,06:00:00)(0)(0.0)(2010-07-01,06:00:00)(0)(1.00131)(1.00949)(2010-06-22,10:10:00)(0)(0.98979)(2010-06-10,20:10:00)(0)(24.95)(29.86)(2010-06-30,20:30:00)(0)(18.27)(2010-06-21,05:55:00)(0)(1.00069)(0.90496)(0)(0)(0)(0)(56145)");
                archiveData.put(makeDate("2010-08-01,06:00:00"), "(103103)(113)(2010-08-01,06:00:00)(0.0)(2010-08-01,06:00:00)(0)(0.0)(2010-08-01,06:00:00)(0)(0.0)(2010-08-01,06:00:00)(0)(0.0)(2010-08-01,06:00:00)(0)(1.00274)(1.01427)(2010-07-18,09:25:00)(0)(0.98845)(2010-07-14,18:20:00)(0)(27.07)(32.74)(2010-07-03,16:35:00)(0)(20.32)(2010-07-30,06:55:00)(0)(1.00073)(0.89983)(0)(0)(0)(0)(51086)");
                archiveData.put(makeDate("2010-09-01,06:00:00"), "(103847)(114)(2010-09-01,06:00:00)(0.0)(2010-09-01,06:00:00)(0)(0.0)(2010-09-01,06:00:00)(0)(0.0)(2010-09-01,06:00:00)(0)(0.0)(2010-09-01,06:00:00)(0)(0.99854)(1.00891)(2010-08-21,08:00:00)(0)(0.98673)(2010-08-23,17:15:00)(0)(23.67)(26.71)(2010-08-22,17:00:00)(0)(17.65)(2010-08-31,05:30:00)(0)(1.00069)(0.90631)(0)(0)(0)(0)(15230)");
                archiveData.put(makeDate("2010-10-01,06:00:00"), "(104567)(115)(2010-10-01,06:00:00)(0.0)(2010-10-01,06:00:00)(0)(0.0)(2010-10-01,06:00:00)(0)(0.0)(2010-10-01,06:00:00)(0)(0.0)(2010-10-01,06:00:00)(0)(0.99969)(1.01063)(2010-09-13,13:40:00)(0)(0.98405)(2010-09-24,14:30:00)(0)(20.66)(25.02)(2010-09-23,17:30:00)(0)(13.58)(2010-09-29,22:15:00)(0)(1.00056)(0.91670)(0)(0)(0)(0)(8805)");
                archiveData.put(makeDate("2010-11-01,06:00:00"), "(105311)(116)(2010-11-01,06:00:00)(0.0)(2010-11-01,06:00:00)(0)(0.0)(2010-11-01,06:00:00)(0)(0.0)(2010-11-01,06:00:00)(0)(0.0)(2010-11-01,06:00:00)(0)(0.99884)(1.01465)(2010-10-26,11:35:00)(0)(0.98482)(2010-10-04,18:30:00)(0)(19.57)(23.96)(2010-10-08,14:30:00)(0)(13.27)(2010-10-14,04:30:00)(0)(1.00049)(0.91943)(0)(0)(0)(0)(5378)");
                archiveData.put(makeDate("2010-12-01,06:00:00"), "(106031)(117)(2010-12-01,06:00:00)(0.0)(2010-12-01,06:00:00)(0)(0.0)(2010-12-01,06:00:00)(0)(0.0)(2010-12-01,06:00:00)(0)(0.0)(2010-12-01,06:00:00)(0)(0.99114)(1.00987)(2010-11-05,10:15:00)(0)(0.96168)(2010-11-09,04:15:00)(0)(19.50)(23.20)(2010-11-05,17:55:00)(0)(14.66)(2010-11-28,09:00:00)(0)(1.00051)(0.91247)(0)(0)(0)(0)(21852)");
                archiveData.put(makeDate("2011-01-01,06:00:00"), "(106775)(118)(2011-01-01,06:00:00)(0.0)(2011-01-01,06:00:00)(0)(0.0)(2011-01-01,06:00:00)(0)(0.0)(2011-01-01,06:00:00)(0)(0.0)(2011-01-01,06:00:00)(0)(0.99847)(1.01618)(2010-12-10,12:25:00)(0)(0.97583)(2010-12-20,00:05:00)(0)(19.20)(21.12)(2010-12-12,18:00:00)(0)(17.74)(2010-12-26,02:55:00)(0)(1.00050)(0.92016)(0)(0)(0)(0)(49491)");
                archiveData.put(makeDate("2011-02-01,06:00:00"), "(107519)(119)(2011-02-01,06:00:00)(0.0)(2011-02-01,06:00:00)(0)(0.0)(2011-02-01,06:00:00)(0)(0.0)(2011-02-01,06:00:00)(0)(0.0)(2011-02-01,06:00:00)(0)(1.00544)(1.02039)(2011-01-21,10:55:00)(0)(0.98635)(2011-01-06,18:30:00)(0)(19.19)(21.81)(2011-01-14,18:15:00)(0)(17.17)(2011-01-31,23:30:00)(0)(1.00049)(0.92664)(0)(0)(0)(0)(22590)");
                archiveData.put(makeDate("2011-03-01,06:00:00"), "(108191)(120)(2011-03-01,06:00:00)(0.0)(2011-03-01,06:00:00)(0)(0.0)(2011-03-01,06:00:00)(0)(0.0)(2011-03-01,06:00:00)(0)(0.0)(2011-03-01,06:00:00)(0)(1.00492)(1.01886)(2011-02-02,12:15:00)(0)(0.98539)(2011-02-15,21:40:00)(0)(18.46)(23.45)(2011-02-18,10:35:00)(0)(2.19)(2011-02-23,10:35:00)(0)(1.00048)(0.92852)(0)(0)(0)(0)(56521)");
                archiveData.put(makeDate("2011-04-01,06:00:00"), "(108935)(121)(2011-04-01,06:00:00)(0.0)(2011-04-01,06:00:00)(0)(0.0)(2011-04-01,06:00:00)(0)(0.0)(2011-04-01,06:00:00)(0)(0.0)(2011-04-01,06:00:00)(0)(1.00836)(1.02632)(2011-03-23,09:40:00)(0)(0.98998)(2011-03-17,04:20:00)(0)(20.03)(25.42)(2011-03-10,08:15:00)(0)(10.51)(2011-03-07,11:15:00)(0)(1.00048)(0.92669)(0)(0)(0)(0)(64197)");
                archiveData.put(makeDate("2011-05-01,06:00:00"), "(109655)(122)(2011-05-01,06:00:00)(0.0)(2011-05-01,06:00:00)(0)(0.0)(2011-05-01,06:00:00)(0)(0.0)(2011-05-01,06:00:00)(0)(0.0)(2011-05-01,06:00:00)(0)(1.00459)(1.01580)(2011-04-06,11:25:00)(0)(0.99189)(2011-04-30,17:35:00)(0)(21.39)(25.58)(2011-04-04,17:35:00)(0)(11.15)(2011-04-05,08:45:00)(0)(1.00065)(0.91892)(0)(0)(0)(0)(13993)");
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