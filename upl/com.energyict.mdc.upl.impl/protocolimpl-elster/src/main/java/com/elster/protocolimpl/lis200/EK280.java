package com.elster.protocolimpl.lis200;

import com.elster.protocolimpl.lis200.objects.GenericArchiveObject;
import com.elster.protocolimpl.lis200.objects.SimpleObject;
import com.elster.protocolimpl.lis200.registers.HistoricRegisterDefinition;
import com.elster.protocolimpl.lis200.registers.HistoricalArchive;
import com.elster.protocolimpl.lis200.registers.IRegisterReadable;
import com.elster.protocolimpl.lis200.registers.Lis200ObisCode;
import com.elster.protocolimpl.lis200.registers.RegisterDefinition;
import com.elster.protocolimpl.lis200.registers.SimpleRegisterDefinition;
import com.elster.protocolimpl.lis200.registers.StateRegisterDefinition;
import com.elster.protocolimpl.lis200.registers.ValueRegisterDefinition;
import com.elster.protocolimpl.lis200.utils.RawArchiveLineInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

//import com.elster.utils.lis200.events.Ek280EventInterpreter;

/**
 * driver class for EK260
 * <p/>
 * it defines only the specialize behavior of a EK260
 *
 * @author heuckeg
 * @since 6/14/2010
 */
@SuppressWarnings({"unused"})
public class EK280 extends LIS200 implements IRegisterReadable {

    private RegisterDefinition[] registersE1N = {

            /* status info */
            new StateRegisterDefinition(Lis200ObisCode.MOMENTARY_STATUS_TOTAL, 1, "100.0"),
            new SimpleRegisterDefinition(Lis200ObisCode.REMAINING_BATTERY_LIFE, 2, "404.0"),
            new SimpleRegisterDefinition(Lis200ObisCode.GSM_RECEPTION_LEVEL, 2, "777.0"),

            /* master data */
            new SimpleRegisterDefinition(Lis200ObisCode.SOFTWARE_VERSION, 2, "190.0"),
            new SimpleRegisterDefinition(Lis200ObisCode.SERIAL_NUMBER_METER, 1, "222.0"),
            new SimpleRegisterDefinition(Lis200ObisCode.SERIAL_NUMBER_PRESSURE_SENS, 6, "222.0"),
            new SimpleRegisterDefinition(Lis200ObisCode.SERIAL_NUMBER_TEMP_SENS, 5, "222.0"),

            /* current, tariff and billing values */
            new ValueRegisterDefinition(Lis200ObisCode.VOLUME_BASE_COND_CORR_CURR, 2, "300.0"),
            new ValueRegisterDefinition(Lis200ObisCode.VOLUME_BASE_COND_TOTAL_CURR, 2, "302.0"),
            new ValueRegisterDefinition(Lis200ObisCode.VOLUME_MEAS_COND_ORG_CURR, 1, "202.0"),
            new ValueRegisterDefinition(Lis200ObisCode.VOLUME_MEAS_COND_CORR_CURR, 4, "300.0"),
            new ValueRegisterDefinition(Lis200ObisCode.VOLUME_MEAS_COND_TOTAL_CURR, 4, "302.0"),
            new ValueRegisterDefinition(Lis200ObisCode.ENERGY_TOTAL_CURR, 1, "302.0"),

            new ValueRegisterDefinition(Lis200ObisCode.PRESSURE_CURR, 7, "310.0"),
            new ValueRegisterDefinition(Lis200ObisCode.TEMPERATURE_CURR, 6, "310_1.0"),
            new ValueRegisterDefinition(Lis200ObisCode.CONVERSION_FACTOR_CURR, 5, "310.0"),
            new ValueRegisterDefinition(Lis200ObisCode.COMPRESSIBILITY_CURR, 8, "310.0"),

            new ValueRegisterDefinition(Lis200ObisCode.FLOW_RATE_MEAS_COND_CURR, 4, "310.0"),
            new ValueRegisterDefinition(Lis200ObisCode.FLOW_RATE_BASE_COND_CURR, 2, "310.0"),

            /** maximum demand values */
            /** volume */
            new HistoricRegisterDefinition(Lis200ObisCode.VOLUME_BASE_COND_TOTAL_HIST, 1, "VAL1"),
            new HistoricRegisterDefinition(Lis200ObisCode.VOLUME_MEAS_COND_ORG_HIST, 1, "VAL2"),
            new HistoricRegisterDefinition(Lis200ObisCode.VOLUME_BASE_COND_CORR_HIST, 1, "VAL3"),
            new HistoricRegisterDefinition(Lis200ObisCode.VOLUME_MEAS_COND_CORR_HIST, 1, "VAL4"),
            new HistoricRegisterDefinition(Lis200ObisCode.MAX_INT_VAL_BASE_COND_HIST, 1, "INT1"),
            new HistoricRegisterDefinition(Lis200ObisCode.MAX_DAY_VAL_BASE_COND_HIST, 1, "DAY1"),
            new HistoricRegisterDefinition(Lis200ObisCode.MAX_INT_VAL_MEAS_COND_HIST, 1, "INT2"),
            new HistoricRegisterDefinition(Lis200ObisCode.MAX_DAY_VAL_MEAS_COND_HIST, 1, "DAY2"),
            /** analogue values */
            new HistoricRegisterDefinition(Lis200ObisCode.MAX_INT_FR_BASE_COND_HIST, 2, "MAX1"),
            new HistoricRegisterDefinition(Lis200ObisCode.MIN_INT_FR_BASE_COND_HIST, 2, "MIN1"),
            new HistoricRegisterDefinition(Lis200ObisCode.MAX_INT_FR_MEAS_COND_HIST, 2, "MAX2"),
            new HistoricRegisterDefinition(Lis200ObisCode.MIN_INT_FR_MEAS_COND_HIST, 2, "MIN2"),
            new HistoricRegisterDefinition(Lis200ObisCode.MEAN_PREASURE_HIST, 2, "VAL1"),
            new HistoricRegisterDefinition(Lis200ObisCode.MAX_INT_PREASURE_HIST, 2, "MAX3"),
            new HistoricRegisterDefinition(Lis200ObisCode.MIN_INT_PREASURE_HIST, 2, "MIN3"),
            new HistoricRegisterDefinition(Lis200ObisCode.MEAN_TEMPERATURE_HIST, 2, "VAL2"),
            new HistoricRegisterDefinition(Lis200ObisCode.MAX_INT_TEMPERATURE_HIST, 2, "MAX4"),
            new HistoricRegisterDefinition(Lis200ObisCode.MIN_INT_TEMPERATURE_HIST, 2, "MIN4"),
            new HistoricRegisterDefinition(Lis200ObisCode.MEAN_COMPRESS_FACTOR_HIST, 2, "VAL3"),
            new HistoricRegisterDefinition(Lis200ObisCode.MEAN_CONVERSION_FACTOR_HIST, 2, "VAL4"),

    };
    private Integer beginOfDay = null;

    public EK280() {
        super();
        setMaxMeterIndex(1);
//        setEventInterpreter(new Ek280EventInterpreter()); //ToDo - this class does not yet exists!
    }

    /**
     * {@inheritDoc}
     */
    public String getProtocolVersion() {
        return "$Date: 2012-12-12 09:00:00 +0100$";
    }

    // *******************************************************************************************
    // * I R e g i s t e r R e a d a b l e
    // *******************************************************************************************/
    public RegisterDefinition[] getRegisterDefinition() {

        ArrayList<RegisterDefinition> result = new ArrayList<RegisterDefinition>();
        if (getMeterIndex() == 1) {
            Collections.addAll(result, registersE1N);
        }
        return result.toArray(new RegisterDefinition[result.size()]);
    }

    public int getBeginOfDay() throws IOException {
        if (beginOfDay == null) {
            String bodAddress;
            String value;

            switch (getMeterIndex()) {
                case 1:
                    bodAddress = "2:141.0";
                    break;
                default:
                    bodAddress = "2:141.0";
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
        } else if (instance == 2) {
            return new HistoricalArchive(new GenericArchiveObject(this, 2));
        } else {
            return null;
        }
    }

    public RawArchiveLineInfo getArchiveLineInfo(int archive, String value) {

        String archiveLineInfo = "";

        if (archive == 1) {
            if (value.equals("VAL3")) {
                archiveLineInfo = ",,TST,CHN00[C],,,,,,,,,,,,,,,,,,CHKSUM";
            } else if (value.equals("VAL1")) {
                archiveLineInfo = ",,TST,,CHN00[C],,,,,,,,,,,,,,,,,CHKSUM";
            } else if (value.equals("INT1")) {
                archiveLineInfo = ",,,,,CHN00[C],TST,,,,,,,,,,,,,,,CHKSUM";
            } else if (value.equals("DAY1")) {
                archiveLineInfo = ",,,,,,,,CHN00[C],TST,,,,,,,,,,,,CHKSUM";
            } else if (value.equals("VAL2")) {
                archiveLineInfo = ",,TST,,,,,,,,,CHN00[C],,,,,,,,,,CHKSUM";
            } else if (value.equals("VAL4")) {
                archiveLineInfo = ",,TST,,,,,,,,,,CHN00[C],,,,,,,,,CHKSUM";
            } else if (value.equals("INT2")) {
                archiveLineInfo = ",,,,,,,,,,,,,CHN00[C],TST,,,,,,,CHKSUM";
            } else if (value.equals("DAY2")) {
                archiveLineInfo = ",,,,,,,,,,,,,,,,CHN00[C],TST,,,,CHKSUM";
            }
        }

        if (archive == 2) {
            if (value.equals("MAX1")) /* Qmes max */ {
                archiveLineInfo = ",,,CHN00,TST,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,CHKSUM";
            } else if (value.equals("MIN1")) /* Qmes min */ {
                archiveLineInfo = ",,,,,,CHN00,TST,,,,,,,,,,,,,,,,,,,,,,,,,,,,CHKSUM";
            } else if (value.equals("MAX2")) /* Qbase max */ {
                archiveLineInfo = ",,,,,,,,,CHN00,TST,,,,,,,,,,,,,,,,,,,,,,,,,CHKSUM";
            } else if (value.equals("MIN2")) /* Qbase min */ {
                archiveLineInfo = ",,,,,,,,,,,,CHN00,TST,,,,,,,,,,,,,,,,,,,,,,CHKSUM";
            } else if (value.equals("VAL1")) /* p mean */ {
                archiveLineInfo = ",,TST,,,,,,,,,,,,,CHN00,,,,,,,,,,,,,,,,,,,,CHKSUM";
            } else if (value.equals("MAX3")) /* p max */ {
                archiveLineInfo = ",,,,,,,,,,,,,,,,CHN00,TST,,,,,,,,,,,,,,,,,,CHKSUM";
            } else if (value.equals("MIN3")) /* p max */ {
                archiveLineInfo = ",,,,,,,,,,,,,,,,,,,CHN00,TST,,,,,,,,,,,,,,,CHKSUM";
            } else if (value.equals("VAL2")) /* t mean */ {
                archiveLineInfo = ",,TST,,,,,,,,,,,,,,,,,,,,CHN00,,,,,,,,,,,,,CHKSUM";
            } else if (value.equals("MAX4")) /* p max */ {
                archiveLineInfo = ",,,,,,,,,,,,,,,,,,,,,,,CHN00,TST,,,,,,,,,,,CHKSUM";
            } else if (value.equals("MIN4")) /* p max */ {
                archiveLineInfo = ",,,,,,,,,,,,,,,,,,,,,,,,,,CHN00,TST,,,,,,,,CHKSUM";
            } else if (value.equals("VAL3")) /* p max */ {
                archiveLineInfo = ",,TST,,,,,,,,,,,,,,,,,,,,,,,,,,,CHN00,,,,,,CHKSUM";
            } else if (value.equals("VAL4")) /* p max */ {
                archiveLineInfo = ",,TST,,,,,,,,,,,,,,,,,,,,,,,,,,,,CHN00,,,,,CHKSUM";
            }
        }

        if (archiveLineInfo.length() > 0) {
            return new RawArchiveLineInfo(archiveLineInfo);
        } else {
            return null;
        }
    }

    /**
     * getter for logbook instance to be able to read out meter events
     *
     * @return Instane of logbook
     */
    protected int getLogBookInstance() {
        if (getMeterType().equalsIgnoreCase("EK280")) {
            return 4;
        } else {
            return super.getLogBookInstance();
        }
    }
}
