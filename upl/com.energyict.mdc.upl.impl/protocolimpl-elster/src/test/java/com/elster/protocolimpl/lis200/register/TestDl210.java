package com.elster.protocolimpl.lis200.register;

import com.elster.protocolimpl.lis200.DL210;
import com.elster.protocolimpl.lis200.objects.ClockObject;
import com.elster.protocolimpl.lis200.registers.HistoricalArchive;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;

/**
 * test case for historical register reading for class DL210
 * User: heuckeg
 * Date: 20.04.11
 * Time: 16:34
 */
public class TestDl210 extends DL210 {

    @Test
    public void RegisterReaderTestWithDLData() throws IOException {

        StringBuilder sb = new StringBuilder();

        RegisterValue rv;

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
        String compareData = getResourceAsString("/com/elster/protocolimpl/lis200/register/dl210registertest.txt");
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
            return new HistoricalArchive(new MyDl210MonthlyArchive(this, 1));
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
        Calendar c = ClockObject.parseCalendar("2011-04-15,13:10:10", false, getTimeZone());
        return c.getTime();
    }

    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone("GMT+1");
    }

    @Override
    public int getMeterIndex() {
        return 1;
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

}
