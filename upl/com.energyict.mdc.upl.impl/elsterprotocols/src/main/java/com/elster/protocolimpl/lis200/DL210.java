package com.elster.protocolimpl.lis200;

import com.elster.protocolimpl.lis200.objects.*;
import com.elster.protocolimpl.lis200.registers.*;
import com.elster.protocolimpl.lis200.utils.RawArchiveLineInfo;
import com.elster.utils.lis200.events.Dl210EventInterpreter;

import java.io.IOException;

/**
 * driver class for DL210
 * <p/>
 * it defines only the specialize behavior of a DL210
 *
 * @author heuckeg
 * @since 6/8/2010
 */
@SuppressWarnings({"unused"})
public class DL210 extends LIS200 implements IRegisterReadable {

    private RegisterDefinition[] registersE0N = {};

    private RegisterDefinition[] registersE1N = {

            /* status info */
            new StateRegisterDefinition(Lis200ObisCode.MOMENTARY_STATUS_TOTAL, 1, "100.0"),
            new SimpleRegisterDefinition(Lis200ObisCode.REMAINING_BATTERY_LIFE, 2, "404.0"),
            new SimpleRegisterDefinition(Lis200ObisCode.GSM_RECEPTION_LEVEL, 2, "777.0"),
            /* master data */
            new SimpleRegisterDefinition(Lis200ObisCode.SOFTWARE_VERSION, 2, "190.0"),
            new SimpleRegisterDefinition(Lis200ObisCode.SERIAL_NUMBER_METER, 1, "222.0"),
            /* current, tariff and billing values */
            new ValueRegisterDefinition(Lis200ObisCode.MAIN_COUNTER_CURR, 1, "200.0"),
            new ValueRegisterDefinition(Lis200ObisCode.ADJUSTABLE_COUNTER_CURR, 1, "203.0"),
            new ValueRegisterDefinition(Lis200ObisCode.FLOW_RATE, 1, "210.0"),
            /** maximum demand values */
            new HistoricRegisterDefinition(Lis200ObisCode.MAIN_COUNTER_HIST, 1, "VAL1"),
            new HistoricRegisterDefinition(Lis200ObisCode.ADJUSTABLE_COUNTER_HIST, 1, "VAL2"),
            new HistoricRegisterDefinition(Lis200ObisCode.MAX_INTERVAL_VALUE_HIST, 1, "INT1"),
            new HistoricRegisterDefinition(Lis200ObisCode.MAX_DAY_VALUE_HIST, 1, "DAY1")
    };

    private Integer beginOfDay = null;

    @SuppressWarnings({"unused"})
    public DL210() {
        super();
        setMaxMeterIndex(1);
        setEventInterpreter(new Dl210EventInterpreter());
    }

    /**
     * {@inheritDoc}
     */
    public String getProtocolVersion() {
        return "$Date: 2011-09-01 11:00:00 +0200 (do, 1 Sep 2011) $";
    }

    // *******************************************************************************************
    // * I R e g i s t e r R e a d a b l e
    // *******************************************************************************************/
    public RegisterDefinition[] getRegisterDefinition() {
        switch (getMeterIndex()) {
            case 1:
                return registersE1N;
            default:
                return registersE0N;
        }
    }

    public int getBeginOfDay() throws IOException {
        if (beginOfDay == null) {
            String bodAddress;
            String value;

            switch (getMeterIndex()) {
                case 1:
                    bodAddress = "5:141.0";
                    break;
                default:
                    bodAddress = "5:141.0";
            }

            SimpleObject bod = new SimpleObject(this, bodAddress);
            value = bod.getValue();
            String[] p = value.split("[*]");
            beginOfDay = Integer.parseInt(p[0]);
        }
        return beginOfDay;
    }

    public HistoricalArchive getHistoricalArchive(int instance) {
        if (instance == 1) {
            return new HistoricalArchive(new GenericArchiveObject(this, 1));
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
}
