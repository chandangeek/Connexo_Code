package com.elster.protocolimpl.lis200.register;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.elster.protocolimpl.lis200.DL240;
import com.elster.protocolimpl.lis200.objects.ClockObject;
import com.elster.protocolimpl.lis200.objects.GenericArchiveObject;
import com.elster.protocolimpl.lis200.registers.HistoricalArchive;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * test case for historical register reading for class DL210
 * User: heuckeg
 * Date: 20.04.11
 * Time: 16:34
 */
public class TestDl240 extends DL240 {
    public TestDl240(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Test
    public void RegisterReaderTestWithDLData() throws IOException {

        StringBuilder sb = new StringBuilder();

        RegisterValue rv;

        setMeterIndex(1);
        for (int i = 1; i <= 15; i++) {
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
        for (int i = 1; i <= 15; i++) {
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
        setMeterIndex(3);
        for (int i = 1; i <= 15; i++) {
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

        setMeterIndex(4);
        for (int i = 1; i <= 15; i++) {
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

        String compareData = getResourceAsString("/com/elster/protocolimpl/lis200/register/dl240registertest.txt");
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
        } else if (instance == 3) {
            return new HistoricalArchive(new MyDl220MonthlyArchive(this, 5));
        } else if (instance == 4) {
            return new HistoricalArchive(new MyDl220MonthlyArchive(this, 7));
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

    private class MyDl220MonthlyArchive extends GenericArchiveObject {

        private TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");

        private Map<Date, String> archiveData;

        private String units;

        MyDl220MonthlyArchive(ProtocolLink link, int archiveInstance) {

            super(link, archiveInstance);

            archiveData = new HashMap<>();

            if (archiveInstance == 1) {
                // (GONr)(AONr)(ZEIT)(V1)(V1.P)(V1M.L)(ZEIT)(STAT)(V1T.L)(ZEIT)(STAT)(ST.1)(ST.SY)(Er.Ch)

                // archive type 50
                units = "()()()(m3)(m3)(m3)()()(m3)()()()()()";

                archiveData.put(makeDate("2010-03-01,06:00:00"), "(28473)(40)(2010-03-01,06:00:00)(000765934.0000)(000000000.0000)(000000059.0000)(2010-02-11,11:00:00)(0)(000001004.0000)(2010-02-11,06:00:00)(0)(0)(0)(823)");
                archiveData.put(makeDate("2010-04-01,06:00:00"), "(29217)(41)(2010-04-01,06:00:00)(000782459.0000)(000000000.0000)(000000067.0000)(2010-03-11,12:00:00)(0)(000001118.0000)(2010-03-12,06:00:00)(0)(0)(0)(27059)");
                archiveData.put(makeDate("2010-05-01,06:00:00"), "(29937)(42)(2010-05-01,06:00:00)(000799384.0000)(000000000.0000)(000000062.0000)(2010-04-29,03:00:00)(0)(000000749.0000)(2010-04-07,06:00:00)(0)(0)(0)(708)");
                archiveData.put(makeDate("2010-06-01,06:00:00"), "(30681)(43)(2010-06-01,06:00:00)(000816503.0000)(000000000.0000)(000000060.0000)(2010-05-06,19:00:00)(0)(000001131.0000)(2010-05-21,06:00:00)(0)(0)(0)(34638)");
                archiveData.put(makeDate("2010-07-01,06:00:00"), "(31401)(44)(2010-07-01,06:00:00)(000832516.0000)(000000000.0000)(000000060.0000)(2010-06-04,09:00:00)(0)(000000696.0000)(2010-06-18,06:00:00)(0)(0)(0)(40773)");
                archiveData.put(makeDate("2010-08-01,06:00:00"), "(32145)(45)(2010-08-01,06:00:00)(000846408.0000)(000000000.0000)(000000087.0000)(2010-07-02,18:00:00)(0)(000001082.0000)(2010-07-13,06:00:00)(0)(0)(0)(57422)");
                archiveData.put(makeDate("2010-09-01,06:00:00"), "(32889)(46)(2010-09-01,06:00:00)(000846408.0000)(000000000.0000)(000000000.0000)(2010-09-01,06:00:00)(0)(000000000.0000)(2010-09-01,06:00:00)(0)(0)(0)(25128)");
                archiveData.put(makeDate("2010-10-01,06:00:00"), "(33609)(47)(2010-10-01,06:00:00)(000846408.0000)(000000000.0000)(000000000.0000)(2010-10-01,06:00:00)(0)(000000000.0000)(2010-10-01,06:00:00)(0)(0)(0)(17534)");
                archiveData.put(makeDate("2010-11-01,06:00:00"), "(34353)(48)(2010-11-01,06:00:00)(000846408.0000)(000000000.0000)(000000000.0000)(2010-11-01,06:00:00)(0)(000000000.0000)(2010-11-01,06:00:00)(0)(0)(0)(51006)");
                archiveData.put(makeDate("2010-12-01,06:00:00"), "(35073)(49)(2010-12-01,06:00:00)(000846408.0000)(000000000.0000)(000000000.0000)(2010-12-01,06:00:00)(0)(000000000.0000)(2010-12-01,06:00:00)(0)(0)(0)(36928)");
                archiveData.put(makeDate("2011-01-01,06:00:00"), "(35817)(50)(2011-01-01,06:00:00)(000846408.0000)(000000000.0000)(000000000.0000)(2011-01-01,06:00:00)(0)(000000000.0000)(2011-01-01,06:00:00)(0)(0)(0)(55762)");
                archiveData.put(makeDate("2011-02-01,06:00:00"), "(36561)(51)(2011-02-01,06:00:00)(000846408.0000)(000000000.0000)(000000000.0000)(2011-02-01,06:00:00)(0)(000000000.0000)(2011-02-01,06:00:00)(0)(0)(0)(59156)");
                archiveData.put(makeDate("2011-03-01,06:00:00"), "(37233)(52)(2011-03-01,06:00:00)(000846408.0000)(000000000.0000)(000000000.0000)(2011-03-01,06:00:00)(0)(000000000.0000)(2011-03-01,06:00:00)(0)(0)(0)(51535)");
                archiveData.put(makeDate("2011-04-01,06:00:00"), "(37977)(53)(2011-04-01,06:00:00)(000846408.0000)(000000000.0000)(000000000.0000)(2011-04-01,06:00:00)(0)(000000000.0000)(2011-04-01,06:00:00)(0)(0)(0)(32427)");
                archiveData.put(makeDate("2011-05-01,06:00:00"), "(38697)(54)(2011-05-01,06:00:00)(000846408.0000)(000000000.0000)(000000000.0000)(2011-05-01,06:00:00)(0)(000000000.0000)(2011-05-01,06:00:00)(0)(0)(0)(3525)");
            }

            if (archiveInstance == 3) {
                // (GONr)(AONr)(ZEIT)(V1)(V1.P)(V1M.L)(ZEIT)(STAT)(V1T.L)(ZEIT)(STAT)(ST.1)(ST.SY)(Er.Ch)

                // archive type 50
                units = "()()()(m3)(m3)(m3)()()(m3)()()()()()";

                archiveData.put(makeDate("2010-03-01,06:00:00"), "(28473)(40)(2010-03-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-03-01,06:00:00)(0)(000000000.0000)(2010-03-01,06:00:00)(0)(16)(0)(64481)");
                archiveData.put(makeDate("2010-04-01,06:00:00"), "(29217)(41)(2010-04-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-04-01,06:00:00)(0)(000000000.0000)(2010-04-01,06:00:00)(0)(16)(0)(32020)");
                archiveData.put(makeDate("2010-05-01,06:00:00"), "(29937)(42)(2010-05-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-05-01,06:00:00)(0)(000000000.0000)(2010-05-01,06:00:00)(0)(16)(0)(45073)");
                archiveData.put(makeDate("2010-06-01,06:00:00"), "(30681)(43)(2010-06-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-06-01,06:00:00)(0)(000000000.0000)(2010-06-01,06:00:00)(0)(16)(0)(62193)");
                archiveData.put(makeDate("2010-07-01,06:00:00"), "(31401)(44)(2010-07-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-07-01,06:00:00)(0)(000000000.0000)(2010-07-01,06:00:00)(0)(16)(0)(13983)");
                archiveData.put(makeDate("2010-08-01,06:00:00"), "(32145)(45)(2010-08-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-08-01,06:00:00)(0)(000000000.0000)(2010-08-01,06:00:00)(0)(16)(0)(15903)");
                archiveData.put(makeDate("2010-09-01,06:00:00"), "(32889)(46)(2010-09-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-09-01,06:00:00)(0)(000000000.0000)(2010-09-01,06:00:00)(0)(16)(0)(30802)");
                archiveData.put(makeDate("2010-10-01,06:00:00"), "(33609)(47)(2010-10-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-10-01,06:00:00)(0)(000000000.0000)(2010-10-01,06:00:00)(0)(16)(0)(24068)");
                archiveData.put(makeDate("2010-11-01,06:00:00"), "(34353)(48)(2010-11-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-11-01,06:00:00)(0)(000000000.0000)(2010-11-01,06:00:00)(0)(16)(0)(56644)");
                archiveData.put(makeDate("2010-12-01,06:00:00"), "(35073)(49)(2010-12-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-12-01,06:00:00)(0)(000000000.0000)(2010-12-01,06:00:00)(0)(16)(0)(35386)");
                archiveData.put(makeDate("2011-01-01,06:00:00"), "(35817)(50)(2011-01-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2011-01-01,06:00:00)(0)(000000000.0000)(2011-01-01,06:00:00)(0)(16)(0)(50088)");
                archiveData.put(makeDate("2011-02-01,06:00:00"), "(36561)(51)(2011-02-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2011-02-01,06:00:00)(0)(000000000.0000)(2011-02-01,06:00:00)(0)(16)(0)(64878)");
                archiveData.put(makeDate("2011-03-01,06:00:00"), "(37233)(52)(2011-03-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2011-03-01,06:00:00)(0)(000000000.0000)(2011-03-01,06:00:00)(0)(16)(0)(54069)");
                archiveData.put(makeDate("2011-04-01,06:00:00"), "(37977)(53)(2011-04-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2011-04-01,06:00:00)(0)(000000000.0000)(2011-04-01,06:00:00)(0)(16)(0)(25809)");
                archiveData.put(makeDate("2011-05-01,06:00:00"), "(38697)(54)(2011-05-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2011-05-01,06:00:00)(0)(000000000.0000)(2011-05-01,06:00:00)(0)(16)(0)(6079)");
            }

            if (archiveInstance == 5) {
                // (GONr)(AONr)(ZEIT)(V1)(V1.P)(V1M.L)(ZEIT)(STAT)(V1T.L)(ZEIT)(STAT)(ST.1)(ST.SY)(Er.Ch)

                // archive type 50
                units = "()()()(m3)(m3)(m3)()()(m3)()()()()()";

                archiveData.put(makeDate("2010-03-01,06:00:00"), "(28473)(40)(2010-03-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-03-01,06:00:00)(0)(000000000.0000)(2010-03-01,06:00:00)(0)(0)(0)(9945)");
                archiveData.put(makeDate("2010-04-01,06:00:00"), "(29217)(41)(2010-04-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-04-01,06:00:00)(0)(000000000.0000)(2010-04-01,06:00:00)(0)(0)(0)(41004)");
                archiveData.put(makeDate("2010-05-01,06:00:00"), "(29937)(42)(2010-05-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-05-01,06:00:00)(0)(000000000.0000)(2010-05-01,06:00:00)(0)(0)(0)(27945)");
                archiveData.put(makeDate("2010-06-01,06:00:00"), "(30681)(43)(2010-06-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-06-01,06:00:00)(0)(000000000.0000)(2010-06-01,06:00:00)(0)(0)(0)(12233)");
                archiveData.put(makeDate("2010-07-01,06:00:00"), "(31401)(44)(2010-07-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-07-01,06:00:00)(0)(000000000.0000)(2010-07-01,06:00:00)(0)(0)(0)(60327)");
                archiveData.put(makeDate("2010-08-01,06:00:00"), "(32145)(45)(2010-08-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-08-01,06:00:00)(0)(000000000.0000)(2010-08-01,06:00:00)(0)(0)(0)(58151)");
                archiveData.put(makeDate("2010-09-01,06:00:00"), "(32889)(46)(2010-09-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-09-01,06:00:00)(0)(000000000.0000)(2010-09-01,06:00:00)(0)(0)(0)(42346)");
                archiveData.put(makeDate("2010-10-01,06:00:00"), "(33609)(47)(2010-10-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-10-01,06:00:00)(0)(000000000.0000)(2010-10-01,06:00:00)(0)(0)(0)(33596)");
                archiveData.put(makeDate("2010-11-01,06:00:00"), "(34353)(48)(2010-11-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-11-01,06:00:00)(0)(000000000.0000)(2010-11-01,06:00:00)(0)(0)(0)(124)");
                archiveData.put(makeDate("2010-12-01,06:00:00"), "(35073)(49)(2010-12-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-12-01,06:00:00)(0)(000000000.0000)(2010-12-01,06:00:00)(0)(0)(0)(22274)");
                archiveData.put(makeDate("2011-01-01,06:00:00"), "(35817)(50)(2011-01-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2011-01-01,06:00:00)(0)(000000000.0000)(2011-01-01,06:00:00)(0)(0)(0)(7824)");
                archiveData.put(makeDate("2011-02-01,06:00:00"), "(36561)(51)(2011-02-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2011-02-01,06:00:00)(0)(000000000.0000)(2011-02-01,06:00:00)(0)(0)(0)(8278)");
                archiveData.put(makeDate("2011-03-01,06:00:00"), "(37233)(52)(2011-03-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2011-03-01,06:00:00)(0)(000000000.0000)(2011-03-01,06:00:00)(0)(0)(0)(3597)");
                archiveData.put(makeDate("2011-04-01,06:00:00"), "(37977)(53)(2011-04-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2011-04-01,06:00:00)(0)(000000000.0000)(2011-04-01,06:00:00)(0)(0)(0)(47593)");
                archiveData.put(makeDate("2011-05-01,06:00:00"), "(38697)(54)(2011-05-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2011-05-01,06:00:00)(0)(000000000.0000)(2011-05-01,06:00:00)(0)(0)(0)(51847)");
            }

            if (archiveInstance == 7) {
                // (GONr)(AONr)(ZEIT)(V1)(V1.P)(V1M.L)(ZEIT)(STAT)(V1T.L)(ZEIT)(STAT)(ST.1)(ST.SY)(Er.Ch)

                // archive type 50
                units = "()()()(m3)(m3)(m3)()()(m3)()()()()()";

                archiveData.put(makeDate("2010-03-01,06:00:00"), "(28473)(40)(2010-03-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-03-01,06:00:00)(0)(000000000.0000)(2010-03-01,06:00:00)(0)(0)(0)(9945)");
                archiveData.put(makeDate("2010-04-01,06:00:00"), "(29217)(41)(2010-04-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-04-01,06:00:00)(0)(000000000.0000)(2010-04-01,06:00:00)(0)(0)(0)(41004)");
                archiveData.put(makeDate("2010-05-01,06:00:00"), "(29937)(42)(2010-05-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-05-01,06:00:00)(0)(000000000.0000)(2010-05-01,06:00:00)(0)(0)(0)(27945)");
                archiveData.put(makeDate("2010-06-01,06:00:00"), "(30681)(43)(2010-06-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-06-01,06:00:00)(0)(000000000.0000)(2010-06-01,06:00:00)(0)(0)(0)(12233)");
                archiveData.put(makeDate("2010-07-01,06:00:00"), "(31401)(44)(2010-07-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-07-01,06:00:00)(0)(000000000.0000)(2010-07-01,06:00:00)(0)(0)(0)(60327)");
                archiveData.put(makeDate("2010-08-01,06:00:00"), "(32145)(45)(2010-08-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-08-01,06:00:00)(0)(000000000.0000)(2010-08-01,06:00:00)(0)(0)(0)(58151)");
                archiveData.put(makeDate("2010-09-01,06:00:00"), "(32889)(46)(2010-09-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-09-01,06:00:00)(0)(000000000.0000)(2010-09-01,06:00:00)(0)(0)(0)(42346)");
                archiveData.put(makeDate("2010-10-01,06:00:00"), "(33609)(47)(2010-10-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-10-01,06:00:00)(0)(000000000.0000)(2010-10-01,06:00:00)(0)(0)(0)(33596)");
                archiveData.put(makeDate("2010-11-01,06:00:00"), "(34353)(48)(2010-11-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-11-01,06:00:00)(0)(000000000.0000)(2010-11-01,06:00:00)(0)(0)(0)(124)");
                archiveData.put(makeDate("2010-12-01,06:00:00"), "(35073)(49)(2010-12-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2010-12-01,06:00:00)(0)(000000000.0000)(2010-12-01,06:00:00)(0)(0)(0)(22274)");
                archiveData.put(makeDate("2011-01-01,06:00:00"), "(35817)(50)(2011-01-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2011-01-01,06:00:00)(0)(000000000.0000)(2011-01-01,06:00:00)(0)(0)(0)(7824)");
                archiveData.put(makeDate("2011-02-01,06:00:00"), "(36561)(51)(2011-02-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2011-02-01,06:00:00)(0)(000000000.0000)(2011-02-01,06:00:00)(0)(0)(0)(8278)");
                archiveData.put(makeDate("2011-03-01,06:00:00"), "(37233)(52)(2011-03-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2011-03-01,06:00:00)(0)(000000000.0000)(2011-03-01,06:00:00)(0)(0)(0)(3597)");
                archiveData.put(makeDate("2011-04-01,06:00:00"), "(37977)(53)(2011-04-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2011-04-01,06:00:00)(0)(000000000.0000)(2011-04-01,06:00:00)(0)(0)(0)(47593)");
                archiveData.put(makeDate("2011-05-01,06:00:00"), "(38697)(54)(2011-05-01,06:00:00)(000000000.0000)(000000000.0000)(000000000.0000)(2011-05-01,06:00:00)(0)(000000000.0000)(2011-05-01,06:00:00)(0)(0)(0)(51847)");
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