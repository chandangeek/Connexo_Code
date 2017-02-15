package com.elster.protocolimpl.lis200;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

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
import com.elster.utils.lis200.events.Ek260EventInterpreter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * driver class for EK260
 * <p/>
 * it defines only the specialize behavior of a EK260
 *
 * @author heuckeg
 * @since 6/14/2010
 */
@SuppressWarnings({"unused"})
public class EK260 extends LIS200 implements IRegisterReadable {

    private static final RegisterDefinition[] registersE1N = {

            /* status info */
            new StateRegisterDefinition(Lis200ObisCode.MOMENTARY_STATUS_TOTAL, 1, "100.0"),
            new SimpleRegisterDefinition(Lis200ObisCode.REMAINING_BATTERY_LIFE, 2, "404.0"),
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

            new ValueRegisterDefinition(Lis200ObisCode.PRESSURE_CURR, 7, "310.0"),
            new ValueRegisterDefinition(Lis200ObisCode.TEMPERATURE_CURR, 6, "310_1.0"),
            new ValueRegisterDefinition(Lis200ObisCode.CONVERSION_FACTOR_CURR, 5, "310.0"),
            new ValueRegisterDefinition(Lis200ObisCode.COMPRESSIBILITY_CURR, 8, "310.0"),

            new ValueRegisterDefinition(Lis200ObisCode.FLOW_RATE_MEAS_COND_CURR, 4, "310.0"),
            new ValueRegisterDefinition(Lis200ObisCode.FLOW_RATE_BASE_COND_CURR, 2, "310.0"),

            /** maximum demand values */
            /** volume */
            new HistoricRegisterDefinition(Lis200ObisCode.VOLUME_BASE_COND_TOTAL_HIST, 1, "VAL1"),
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

    public EK260(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
        setMaxMeterIndex(1);
        setEventInterpreter(new Ek260EventInterpreter());
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2011-09-01 11:00:00 +0200 (do, 1 Sep 2011) $";
    }

    @Override
    public RegisterDefinition[] getRegisterDefinition() {

        ArrayList<RegisterDefinition> result = new ArrayList<RegisterDefinition>();

        if (getMeterIndex() == 1) {
            Collections.addAll(result, registersE1N);

            int swVersion = getSoftwareVersion();
            if (swVersion < 200) {
                result.add(new HistoricRegisterDefinition(Lis200ObisCode.VOLUME_MEAS_COND_TOTAL_HIST, 1, "VAL2"));
            } else {
                result.add(new HistoricRegisterDefinition(Lis200ObisCode.VOLUME_MEAS_COND_ORG_HIST, 1, "VAL2"));
                result.add(new HistoricRegisterDefinition(Lis200ObisCode.VOLUME_BASE_COND_CORR_HIST, 1, "VAL3"));
                result.add(new HistoricRegisterDefinition(Lis200ObisCode.VOLUME_MEAS_COND_CORR_HIST, 1, "VAL4"));
            }

            if (swVersion >= 232) {
                result.add(new SimpleRegisterDefinition(Lis200ObisCode.GSM_RECEPTION_LEVEL, 2, "777.0"));
            }
            if (swVersion >= 240) {
                result.add(new ValueRegisterDefinition(Lis200ObisCode.ENERGY_TOTAL_CURR, 1, "302.0"));
            }
        }
        return result.toArray(new RegisterDefinition[result.size()]);
    }

    @Override
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

    @Override
    public HistoricalArchive getHistoricalArchive(int instance) {
        if (instance == 1) {
            return new HistoricalArchive(new GenericArchiveObject(this, 1));
        } else if (instance == 2) {
            return new HistoricalArchive(new GenericArchiveObject(this, 2));
        } else {
            return null;
        }
    }

    @Override
    public RawArchiveLineInfo getArchiveLineInfo(int archive, String value) {
        String archiveLineInfo = "";
        if (archive == 1) {
            if (getSoftwareVersion() < 200) {
                if ("VAL1".equals(value)) {
                    archiveLineInfo = ",,TST,CHN00[C],,,,,,,,,,,,,,,,CHKSUM";
                } else if ("INT1".equals(value)) {
                    archiveLineInfo = ",,,,CHN00[C],TST,,,,,,,,,,,,,,CHKSUM";
                } else if ("DAY1".equals(value)) {
                    archiveLineInfo = ",,,,,,,CHN00[C],TST,,,,,,,,,,,CHKSUM";
                } else if ("VAL2".equals(value)) {
                    archiveLineInfo = ",,TST,,,,,,,,CHN00[C],,,,,,,,,CHKSUM";
                } else if ("INT2".equals(value)) {
                    archiveLineInfo = ",,,,,,,,,,,CHN00[C],TST,,,,,,,CHKSUM";
                } else if ("DAY2".equals(value)) {
                    archiveLineInfo = ",,,,,,,,,,,,,,CHN00[C],TST,,,,CHKSUM";
                }
            } else {
                if ("VAL3".equals(value)) {
                    archiveLineInfo = ",,TST,CHN00[C],,,,,,,,,,,,,,,,,,CHKSUM";
                } else if ("VAL1".equals(value)) {
                    archiveLineInfo = ",,TST,,CHN00[C],,,,,,,,,,,,,,,,,CHKSUM";
                } else if ("INT1".equals(value)) {
                    archiveLineInfo = ",,,,,CHN00[C],TST,,,,,,,,,,,,,,,CHKSUM";
                } else if ("DAY1".equals(value)) {
                    archiveLineInfo = ",,,,,,,,CHN00[C],TST,,,,,,,,,,,,CHKSUM";
                } else if ("VAL2".equals(value)) {
                    archiveLineInfo = ",,TST,,,,,,,,,CHN00[C],,,,,,,,,,CHKSUM";
                } else if ("VAL4".equals(value)) {
                    archiveLineInfo = ",,TST,,,,,,,,,,CHN00[C],,,,,,,,,CHKSUM";
                } else if ("INT2".equals(value)) {
                    archiveLineInfo = ",,,,,,,,,,,,,CHN00[C],TST,,,,,,,CHKSUM";
                } else if ("DAY2".equals(value)) {
                    archiveLineInfo = ",,,,,,,,,,,,,,,,CHN00[C],TST,,,,CHKSUM";
                }

            }
        }

        if (archive == 2) {
            if ("MAX1".equals(value)) /* Qmes max */ {
                archiveLineInfo = ",,,CHN00,TST,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,CHKSUM";
            } else if ("MIN1".equals(value)) /* Qmes min */ {
                archiveLineInfo = ",,,,,,CHN00,TST,,,,,,,,,,,,,,,,,,,,,,,,,,,,CHKSUM";
            } else if ("MAX2".equals(value)) /* Qbase max */ {
                archiveLineInfo = ",,,,,,,,,CHN00,TST,,,,,,,,,,,,,,,,,,,,,,,,,CHKSUM";
            } else if ("MIN2".equals(value)) /* Qbase min */ {
                archiveLineInfo = ",,,,,,,,,,,,CHN00,TST,,,,,,,,,,,,,,,,,,,,,,CHKSUM";
            } else if ("VAL1".equals(value)) /* p mean */ {
                archiveLineInfo = ",,TST,,,,,,,,,,,,,CHN00,,,,,,,,,,,,,,,,,,,,CHKSUM";
            } else if ("MAX3".equals(value)) /* p max */ {
                archiveLineInfo = ",,,,,,,,,,,,,,,,CHN00,TST,,,,,,,,,,,,,,,,,,CHKSUM";
            } else if ("MIN3".equals(value)) /* p max */ {
                archiveLineInfo = ",,,,,,,,,,,,,,,,,,,CHN00,TST,,,,,,,,,,,,,,,CHKSUM";
            } else if ("VAL2".equals(value)) /* t mean */ {
                archiveLineInfo = ",,TST,,,,,,,,,,,,,,,,,,,,CHN00,,,,,,,,,,,,,CHKSUM";
            } else if ("MAX4".equals(value)) /* p max */ {
                archiveLineInfo = ",,,,,,,,,,,,,,,,,,,,,,,CHN00,TST,,,,,,,,,,,CHKSUM";
            } else if ("MIN4".equals(value)) /* p max */ {
                archiveLineInfo = ",,,,,,,,,,,,,,,,,,,,,,,,,,CHN00,TST,,,,,,,,CHKSUM";
            } else if ("VAL3".equals(value)) /* p max */ {
                archiveLineInfo = ",,TST,,,,,,,,,,,,,,,,,,,,,,,,,,,CHN00,,,,,,CHKSUM";
            } else if ("VAL4".equals(value)) /* p max */ {
                archiveLineInfo = ",,TST,,,,,,,,,,,,,,,,,,,,,,,,,,,,CHN00,,,,,CHKSUM";
            }
        }

        if (!archiveLineInfo.isEmpty()) {
            return new RawArchiveLineInfo(archiveLineInfo);
        } else {
            return null;
        }
    }

}