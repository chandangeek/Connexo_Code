package com.elster.protocolimpl.lis200.register;

import com.elster.protocolimpl.lis200.objects.ClockObject;
import com.elster.protocolimpl.lis200.objects.HistoricalValueObject;
import com.elster.protocolimpl.lis200.objects.SimpleObject;
import com.elster.protocolimpl.lis200.registers.HistoricRegisterDefinition;
import com.elster.protocolimpl.lis200.registers.HistoricalArchive;
import com.elster.protocolimpl.lis200.registers.IRegisterReadable;
import com.elster.protocolimpl.lis200.registers.Lis200ObisCode;
import com.elster.protocolimpl.lis200.registers.Lis200RegisterN;
import com.elster.protocolimpl.lis200.registers.MaxRegisterDefinition;
import com.elster.protocolimpl.lis200.registers.RegisterDefinition;
import com.elster.protocolimpl.lis200.registers.RegisterMapN;
import com.elster.protocolimpl.lis200.registers.RegisterReader;
import com.elster.protocolimpl.lis200.registers.SimpleRegisterDefinition;
import com.elster.protocolimpl.lis200.registers.StateRegisterDefinition;
import com.elster.protocolimpl.lis200.registers.ValueRegisterDefinition;
import com.elster.protocolimpl.lis200.utils.RawArchiveLineInfo;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import static junit.framework.Assert.assertEquals;

/**
 * Class to test register reader
 * <p/>
 * User: heuckeg
 * Date: 14.04.11
 * Time: 09:09
 */
public class TestRegisterReader implements ProtocolLink, IRegisterReadable {


    private RegisterDefinition[] registersE1N = {

            /* status info */
            new StateRegisterDefinition(Lis200ObisCode.MOMENTARY_STATUS_TOTAL, 1, "(2)(3)(4)"),
            new SimpleRegisterDefinition(Lis200ObisCode.REMAINING_BATTERY_LIFE, 2, "44"),
            new SimpleRegisterDefinition(Lis200ObisCode.GSM_RECEPTION_LEVEL, 2, "99"),
            /* master data */
            new SimpleRegisterDefinition(Lis200ObisCode.SOFTWARE_VERSION, 2, "1.11"),
            new SimpleRegisterDefinition(Lis200ObisCode.SERIAL_NUMBER_METER, 1, "1234567890"),
            /* current, tariff and billing values */
            new ValueRegisterDefinition(Lis200ObisCode.MAIN_COUNTER_CURR, 1, "1234567.8*m3"),
            new ValueRegisterDefinition(Lis200ObisCode.ADJUSTABLE_COUNTER_CURR, 1, "2234567.8*m3"),
            new ValueRegisterDefinition(Lis200ObisCode.FLOW_RATE, 1, "100*m3/h"),
            /** maximum demand values */
            new MaxRegisterDefinition(Lis200ObisCode.MAX_INTERVAL_VALUE, 3, "(222*m3)(2011-1-22,12:12:12)"),
            new MaxRegisterDefinition(Lis200ObisCode.MAX_DAY_VALUE, 4, "(1111*m3)(2011-1-11,11:11:11)"),

            new HistoricRegisterDefinition(Lis200ObisCode.MAIN_COUNTER_HIST, 1, "VAL1"),
            new HistoricRegisterDefinition(Lis200ObisCode.ADJUSTABLE_COUNTER_HIST, 1, "VAL2"),
            new HistoricRegisterDefinition(Lis200ObisCode.MAX_INTERVAL_VALUE_HIST, 1, "INT1"),
            new HistoricRegisterDefinition(Lis200ObisCode.MAX_DAY_VALUE_HIST, 1, "DAY1")
    };

    @Test
    public void RegisterReaderTestWithDLData() throws IOException {

        RegisterMapN registerMap = new RegisterMapN();

        RegisterDefinition[] regs = registersE1N;

        SimpleObject lisObj;
        for (RegisterDefinition reg : regs) {
            lisObj = null;
            if (reg instanceof SimpleRegisterDefinition) {
                lisObj = new ConstIntervalObject(this, reg.getInstance(), reg.getAddress());
            }
            if (reg instanceof StateRegisterDefinition) {
                lisObj = new ConstStatusObject(this, reg.getInstance(), reg.getAddress());
            }
            if (reg instanceof ValueRegisterDefinition) {
                lisObj = new ConstSimpleObject(this, reg.getInstance(), reg.getAddress());
            }
            if (reg instanceof MaxRegisterDefinition) {
                lisObj = new ConstMaxDemandObject(this, reg.getInstance(), reg.getAddress());
            }
            if (reg instanceof HistoricRegisterDefinition) {
                RawArchiveLineInfo ralInfo = getArchiveLineInfo(reg.getInstance(), reg.getAddress());
                if (ralInfo != null) {
                    lisObj = new HistoricalValueObject(this, reg.getInstance(), ralInfo);
                }
            }

            if (lisObj != null) {
                registerMap.add(new Lis200RegisterN(reg.getObiscode(), lisObj));
            }
        }

        RegisterReader reader = new RegisterReader(this, registerMap);

        /* for test use a fixed date */
        Calendar c = ClockObject.parseCalendar("2011-04-15,13:10:10", false, getTimeZone());
        Date date = c.getTime();

        StringBuilder sb = new StringBuilder();

        String s;
        String[] parts;
        RegisterValue rv;
        rv = reader.getRegisterValue(new ObisCode(7, 0, 96, 5, 0, 255), date);
        s = rv.toString();
        parts = s.split(",");
        sb.append(parts[0]).append(",").append(parts[1].trim());
        sb.append("\n");

        rv = reader.getRegisterValue(new ObisCode(0, 0, 96, 6, 6, 255), date);
        s = rv.toString();
        parts = s.split(",");
        sb.append(parts[0]).append(",").append(parts[1].trim());
        sb.append("\n");

        rv = reader.getRegisterValue(new ObisCode(0, 0, 96, 12, 5, 255), date);
        s = rv.toString();
        parts = s.split(",");
        sb.append(parts[0]).append(",").append(parts[1].trim());
        sb.append("\n");

        rv = reader.getRegisterValue(new ObisCode(7, 0, 0, 2, 2, 255), date);
        s = rv.toString();
        parts = s.split(",");
        sb.append(parts[0]).append(",").append(parts[1].trim());
        sb.append("\n");

        rv = reader.getRegisterValue(new ObisCode(7, 0, 0, 2, 14, 255), date);
        s = rv.toString();
        parts = s.split(",");
        sb.append(parts[0]).append(",").append(parts[1].trim());
        sb.append("\n");

        rv = reader.getRegisterValue(new ObisCode(7, 0, 23, 2, 0, 255), date);
        s = rv.toString();
        parts = s.split(",");
        sb.append(parts[0]).append(",").append(parts[1].trim());
        sb.append("\n");

        rv = reader.getRegisterValue(new ObisCode(7, 128, 23, 2, 0, 255), date);
        s = rv.toString();
        parts = s.split(",");
        sb.append(parts[0]).append(",").append(parts[1].trim());
        sb.append("\n");

        rv = reader.getRegisterValue(new ObisCode(7, 0, 43, 0, 255, 255), date);
        s = rv.toString();
        parts = s.split(",");
        sb.append(parts[0]).append(",").append(parts[1].trim());
        sb.append("\n");


        // Tages- und Intervalmaximum.
        rv = reader.getRegisterValue(new ObisCode(7, 0, 23, 56, 0, 101), date);
        sb.append(rv);
        sb.append("\n");
        rv = reader.getRegisterValue(new ObisCode(7, 0, 23, 62, 0, 101), date);
        sb.append(rv);
        sb.append("\n");


        for (int i = 1; i <= 15; i++) {
            rv = reader.getRegisterValue(new ObisCode(7, 0, 23, 2, 0, i), date);
            sb.append(rv);
            sb.append("\n");
            rv = reader.getRegisterValue(new ObisCode(7, 128, 23, 2, 0, i), date);
            sb.append(rv);
            sb.append("\n");
            rv = reader.getRegisterValue(new ObisCode(7, 0, 23, 56, 0, i), date);
            sb.append(rv);
            sb.append("\n");
            rv = reader.getRegisterValue(new ObisCode(7, 0, 23, 62, 0, i), date);
            sb.append(rv);
            sb.append("\n");
        }
        String compareData = getResourceAsString("/com/elster/protocolimpl/lis200/register/RegisterReaderTestWithDLData.txt");
        assertEquals(compareData, sb.toString());
    }


    // *******************************************************************************************
    // * I R e g i s t e r R e a d a b l e
    // *******************************************************************************************/
    public RegisterDefinition[] getRegisterDefinition() {
        return new RegisterDefinition[0];
    }

    public int getBeginOfDay() {
        return 6;
    }

    public HistoricalArchive getHistoricalArchive(int instance) {
        if (instance == 1) {
            return new HistoricalArchive(new MyDl210MonthlyArchive(this, 1));
        } else {
            return null;
        }
    }

    public RawArchiveLineInfo getArchiveLineInfo(int archive, String value) {

        String archiveLineInfo = "";

        if (archive == 1) {
            if (value.equals("VAL1")) {
                archiveLineInfo = ",,TST,CHN00[C],,,,,,,,,,CHKSUM";
            } else if (value.equals("VAL2")) {
                archiveLineInfo = ",,TST,,CHN00[C],,,,,,,,,CHKSUM";
            } else if (value.equals("INT1")) {
                archiveLineInfo = ",,,,,CHN00[C],TST,,,,,,,CHKSUM";
            } else if (value.equals("DAY1")) {
                archiveLineInfo = ",,,,,,,,CHN00[C],TST,,,,CHKSUM";
            }
        }

        if (archiveLineInfo.length() > 0) {
            return new RawArchiveLineInfo(archiveLineInfo);
        } else {
            return null;
        }
    }

    // *******************************************************************************************
    // * P r o t o c o l L i n k
    // *******************************************************************************************/
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return null;
    }

    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone("GMT+1");
    }

    public boolean isIEC1107Compatible() {
        return false;
    }

    public int getNumberOfChannels() throws IOException {
        return 0;
    }

    public String getPassword() {
        return null;
    }

    public byte[] getDataReadout() {
        return new byte[0];
    }

    public int getProfileInterval() throws IOException {
        return 0;
    }

    public ChannelMap getChannelMap() {
        return null;
    }

    public ProtocolChannelMap getProtocolChannelMap() {
        return null;
    }

    public Logger getLogger() {
        return null;
    }

    public int getNrOfRetries() {
        return 0;
    }

    public boolean isRequestHeader() {
        return false;
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
