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
import com.elster.utils.lis200.events.Dl220EventInterpreter;

import java.io.IOException;

/**
 * driver class for DL220
 * <p/>
 * it defines only the specialize behavior of a DL220
 *
 * @author heuckeg
 * @since 6/10/2010
 */
public class DL220 extends LIS200 implements IRegisterReadable {

    private static final RegisterDefinition[] registersE0N = {};

    private static final RegisterDefinition[] registersE1N = {

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

    private static final RegisterDefinition[] registersE2N = {

            /* status info */
            new StateRegisterDefinition(Lis200ObisCode.MOMENTARY_STATUS_TOTAL, 1, "100.0"),
            new SimpleRegisterDefinition(Lis200ObisCode.REMAINING_BATTERY_LIFE, 2, "404.0"),
            new SimpleRegisterDefinition(Lis200ObisCode.GSM_RECEPTION_LEVEL, 2, "777.0"),
            /* master data */
            new SimpleRegisterDefinition(Lis200ObisCode.SOFTWARE_VERSION, 2, "190.0"),
            new SimpleRegisterDefinition(Lis200ObisCode.SERIAL_NUMBER_METER, 2, "222.0"),
            /* current, tariff and billing values */
            new ValueRegisterDefinition(Lis200ObisCode.MAIN_COUNTER_CURR, 2, "200.0"),
            new ValueRegisterDefinition(Lis200ObisCode.ADJUSTABLE_COUNTER_CURR, 2, "203.0"),
            new ValueRegisterDefinition(Lis200ObisCode.FLOW_RATE, 2, "210.0"),
            /** maximum demand values */
            new HistoricRegisterDefinition(Lis200ObisCode.MAIN_COUNTER_HIST, 2, "VAL1"),
            new HistoricRegisterDefinition(Lis200ObisCode.ADJUSTABLE_COUNTER_HIST, 2, "VAL2"),
            new HistoricRegisterDefinition(Lis200ObisCode.MAX_INTERVAL_VALUE_HIST, 2, "INT1"),
            new HistoricRegisterDefinition(Lis200ObisCode.MAX_DAY_VALUE_HIST, 2, "DAY1")
    };

    private Integer beginOfDay = null;

    public DL220(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
        setMaxMeterIndex(2);
        setEventInterpreter(new Dl220EventInterpreter());
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2011-09-01 11:00:00 +0200 (do, 1 Sep 2011) $";
    }

    @Override
    public RegisterDefinition[] getRegisterDefinition() {
        switch (getMeterIndex()) {
            case 1:
                return registersE1N;
            case 2:
                return registersE2N;
            default:
                return registersE0N;
        }
    }

    @Override
    public int getBeginOfDay() throws IOException {
        if (beginOfDay == null) {
            String bodAddress;
            String value;

            switch (getMeterIndex()) {
                case 1:
                    bodAddress = "5:141.0";
                    break;
                case 2:
                    bodAddress = "6:141.0";
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

    @Override
    public HistoricalArchive getHistoricalArchive(int instance) {
        if (instance == 1) {
            return new HistoricalArchive(new GenericArchiveObject(this, 1));
        } else if (instance == 2) {
            return new HistoricalArchive(new GenericArchiveObject(this, 3));
        } else {
            return null;
        }
    }

    @Override
    public RawArchiveLineInfo getArchiveLineInfo(int archive, String value) {
        String archiveLineInfo = "";
        if ((archive == 1) || (archive == 2)) {
            if ("VAL1".equals(value)) {
                archiveLineInfo = ",,TST,CHN00[C],,,,,,,,,,CHKSUM";
            } else if ("VAL2".equals(value)) {
                archiveLineInfo = ",,TST,,CHN00[C],,,,,,,,,CHKSUM";
            } else if ("INT1".equals(value)) {
                archiveLineInfo = ",,,,,CHN00[C],TST,,,,,,,CHKSUM";
            } else if ("DAY1".equals(value)) {
                archiveLineInfo = ",,,,,,,,CHN00[C],TST,,,,CHKSUM";
            }
        }

        if (!archiveLineInfo.isEmpty()) {
            return new RawArchiveLineInfo(archiveLineInfo);
        } else {
            return null;
        }
    }

}