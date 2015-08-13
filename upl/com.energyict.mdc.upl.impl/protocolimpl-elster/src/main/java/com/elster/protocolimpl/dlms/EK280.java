package com.elster.protocolimpl.dlms;

import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.protocolimpl.dlms.messaging.DlmsMessageExecutor;
import com.elster.protocolimpl.dlms.messaging.Ek280MessageExecutor;
import com.elster.protocolimpl.dlms.registers.DlmsHistoricalRegisterDefinition;
import com.elster.protocolimpl.dlms.registers.DlmsSimpleRegisterDefinition;
import com.elster.protocolimpl.dlms.registers.HistoricalObisCode;
import com.elster.protocolimpl.dlms.registers.IReadableRegister;
import com.elster.protocolimpl.dlms.registers.RegisterMap;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * User: heuckeg
 * Date: 22.09.11
 * Time: 14:42
 */
@SuppressWarnings({"unused"})
public class EK280 extends Dlms {

    private static String ARCHIVESTRUCTUREVERSION = "ArchiveStructureVersion";

    protected static String V2ARCHIVESTRUCTURE = "TST=0.0.1.0.0.255" + "," +
            "CHN0[C9]=7.0.11.2.0.255" + "," +
            "CHN1[C9]=7.0.13.2.0.255" + "," +
            "CHN2[C9]=7.0.11.0.0.255" + "," +
            "CHN3[C9]=7.0.13.0.0.255" + "," +
            "CHN4=7.0.42.42.0.255" + "," +
            "CHN5=7.0.41.42.0.255" + "," +
            "CHN6=7.0.53.0.16.255" + "," +
            "CHN7=7.0.52.0.16.255" + "," +
            "SYS=7.129.96.5.2.255" + "," +
            "EVT=7.128.96.5.67.255";

    // Standard event log 7.0.99.98.0.255
    protected static String ITALYLOGSTRUCTURE = "TST=0.0.1.0.0.255" + "," +
            "EVT_DLMS=7.128.96.5.74.255";

    protected static String V2LOGSTRUCTURE = "TST=0.0.1.0.0.255" + "," +
            "EVT_L2=7.128.96.5.68.255";

    protected static IReadableRegister[] italyMappings = {
            new DlmsSimpleRegisterDefinition("7.0.0.2.1.255", Ek280Defs.SOFTWARE_VERSION),
            new DlmsSimpleRegisterDefinition("7.0.0.2.1.255", Ek280Defs.SOFTWARE_VERSION),
            new DlmsSimpleRegisterDefinition("7.0.0.2.13.255", Ek280Defs.DLMS_DEVICE_SERIAL),

            new DlmsSimpleRegisterDefinition("0.0.96.10.2.255", Ek280Defs.BILLING_STATUS_REGISTER),
            new DlmsSimpleRegisterDefinition("0.2.96.10.1.255", Ek280Defs.STATUS_REGISTER_1_2),
            new DlmsSimpleRegisterDefinition("0.3.96.12.5.255", Ek280Defs.GSM_SIGNAL_STRENGTH),
            new DlmsSimpleRegisterDefinition("0.3.96.12.6.255", Ek280Defs.GSM_PHONE_NUMBER),
            new DlmsSimpleRegisterDefinition("0.0.96.6.6.255", Ek280Defs.BATTERY_REMAINING),
            new DlmsSimpleRegisterDefinition("7.0.0.9.4.255", Ek280Defs.REMAINING_SHIFT_TIME),

            // new since 24/2/2012
            new DlmsSimpleRegisterDefinition("0.2.96.6.3.255", Ek280Defs.MODEM_BATTERY_VOLTAGE),
            // new since 27/01/2012
            new DlmsSimpleRegisterDefinition("0.0.96.53.0.255", new ObisCode("0.0.96.53.0.255")),
            // new since 27/01/2012

            new DlmsSimpleRegisterDefinition("7.0.0.0.9.255", Ek280Defs.EQUIPMENT_CLASS),
            // new since 27/01/2012
            new DlmsSimpleRegisterDefinition("7.0.96.99.5.255", Ek280Defs.NUMBER_OF_PRE_DECIMAL_PLACES),
            // new since 27/01/2012
            new DlmsSimpleRegisterDefinition("7.1.0.7.2.255", Ek280Defs.CP_VALUE),

            new DlmsSimpleRegisterDefinition("0.0.96.52.0.255", Ek280Defs.INSTALLATION_DATE),
            new DlmsSimpleRegisterDefinition("7.0.0.2.0.255", Ek280Defs.EQUIPMENT_CONFIG),
            new DlmsSimpleRegisterDefinition("0.0.96.1.10.255", Ek280Defs.METERING_POINT_ID),
            new DlmsSimpleRegisterDefinition("7.0.96.99.3.255", Ek280Defs.INST_METER_TYPE),
            new DlmsSimpleRegisterDefinition("7.0.96.99.2.255", Ek280Defs.INST_METER_CALIBER),
            new DlmsSimpleRegisterDefinition("7.0.0.2.14.255", Ek280Defs.INST_METER_SERIAL),

            new DlmsSimpleRegisterDefinition("7.0.13.0.0.255", Ek280Defs.VM_TOTAL_CURR),
            new DlmsSimpleRegisterDefinition("7.0.13.2.0.255", Ek280Defs.VB_TOTAL_CURR),

            new DlmsSimpleRegisterDefinition("7.0.11.83.0.255", Ek280Defs.VM_UNDISTURBED_TOTAL_CURR_PERIOD),
            new DlmsSimpleRegisterDefinition("7.0.11.83.1.255", Ek280Defs.VM_UNDISTURBED_F1_CURR_PERIOD),
            new DlmsSimpleRegisterDefinition("7.0.11.83.2.255", Ek280Defs.VM_UNDISTURBED_F2_CURR_PERIOD),
            new DlmsSimpleRegisterDefinition("7.0.11.83.3.255", Ek280Defs.VM_UNDISTURBED_F3_CURR_PERIOD),
            new DlmsSimpleRegisterDefinition("7.0.11.83.0.101", Ek280Defs.VM_UNDISTURBED_TOTAL_PREV_PERIOD),
            new DlmsSimpleRegisterDefinition("7.0.11.83.1.101", Ek280Defs.VM_UNDISTURBED_F1_PREV_PERIOD),
            new DlmsSimpleRegisterDefinition("7.0.11.83.2.101", Ek280Defs.VM_UNDISTURBED_F2_PREV_PERIOD),
            new DlmsSimpleRegisterDefinition("7.0.11.83.3.101", Ek280Defs.VM_UNDISTURBED_F3_PREV_PERIOD),

            new DlmsSimpleRegisterDefinition("7.0.12.81.0.255", Ek280Defs.VM_DISTURBED_TOTAL_CURR_PERIOD),
            new DlmsSimpleRegisterDefinition("7.0.12.81.1.255", Ek280Defs.VM_DISTURBED_F1_CURR_PERIOD),
            new DlmsSimpleRegisterDefinition("7.0.12.81.2.255", Ek280Defs.VM_DISTURBED_F2_CURR_PERIOD),
            new DlmsSimpleRegisterDefinition("7.0.12.81.3.255", Ek280Defs.VM_DISTURBED_F3_CURR_PERIOD),
            new DlmsSimpleRegisterDefinition("7.0.12.81.0.101", Ek280Defs.VM_DISTURBED_TOTAL_PREV_PERIOD),
            new DlmsSimpleRegisterDefinition("7.0.12.81.1.101", Ek280Defs.VM_DISTURBED_F1_PREV_PERIOD),
            new DlmsSimpleRegisterDefinition("7.0.12.81.2.101", Ek280Defs.VM_DISTURBED_F2_PREV_PERIOD),
            new DlmsSimpleRegisterDefinition("7.0.12.81.3.101", Ek280Defs.VM_DISTURBED_F3_PREV_PERIOD),

            new DlmsSimpleRegisterDefinition("7.0.42.2.0.255", Ek280Defs.PRESSURE_REFERENCE),
            new DlmsSimpleRegisterDefinition("7.0.41.2.0.255", Ek280Defs.TEMPERATURE_REFERENCE),
            new DlmsSimpleRegisterDefinition("7.0.42.0.0.255", Ek280Defs.PRESSURE_ABSOLUTE_CURR),
            new DlmsSimpleRegisterDefinition("7.0.41.0.0.255", Ek280Defs.TEMPERATURE_CURR),
            new DlmsSimpleRegisterDefinition("7.0.43.0.0.255", Ek280Defs.FLOWRATE_VM_CURR),
            new DlmsSimpleRegisterDefinition("7.0.43.2.0.255", Ek280Defs.FLOWRATE_VB_CURR),

            new DlmsSimpleRegisterDefinition("7.0.43.153.0.255", Ek280Defs.FLOWRATE_CONV_MAX_CURR_DAY),
            new DlmsSimpleRegisterDefinition("7.0.43.153.0.101", Ek280Defs.FLOWRATE_CONV_MAX_PREV_DAY),

            new DlmsSimpleRegisterDefinition("7.0.52.0.0.255", Ek280Defs.COEFFICIENT_C_CURR),
            new DlmsSimpleRegisterDefinition("7.0.53.0.0.255", Ek280Defs.COEFFICIENT_Z_CURR),
            new DlmsSimpleRegisterDefinition("7.0.53.12.0.255", Ek280Defs.Z_CALC_METHOD),
            // new since 24/2/2012
            new DlmsSimpleRegisterDefinition("7.0.0.4.2.255", Ek280Defs.Z_CALC_METHOD_CODE),
            new DlmsSimpleRegisterDefinition("7.0.0.4.0.255", Ek280Defs.VOLUME_CALC_METHOD),

            new DlmsSimpleRegisterDefinition("7.0.0.12.45.255", Ek280Defs.DENSITY_GAS_BASE_COND),
            new DlmsSimpleRegisterDefinition("7.0.0.12.46.255", Ek280Defs.DENSITY_RATIO),
            new DlmsSimpleRegisterDefinition("7.0.0.12.54.255", Ek280Defs.CALORIFIC_VALUE_COMP_CURR),
            new DlmsSimpleRegisterDefinition("7.0.0.12.60.255", Ek280Defs.GAV_NITROGEN_CONT_CURR),
            new DlmsSimpleRegisterDefinition("7.0.0.12.61.255", Ek280Defs.GAV_HYDROGEN_CONT_CURR),
            new DlmsSimpleRegisterDefinition("7.0.0.12.65.255", Ek280Defs.GAV_CARBONOXID_CONT_CURR),
            new DlmsSimpleRegisterDefinition("7.0.0.12.66.255", Ek280Defs.GAV_CARBONDIOXID_CONT_CURR),
            new DlmsSimpleRegisterDefinition("7.0.0.12.67.255", Ek280Defs.GAV_METHAN_CONT_CURR)
    };

    private final static String OC_TST = "0.0.1.0.0.255";
    private final static String LP_MONTH = "7.1.99.99.4.255";
    private final static String MP_MONTH = "7.2.99.99.4.255";

    protected static IReadableRegister[] v2Mappings = {
            new DlmsSimpleRegisterDefinition("7.129.96.5.1.255", new ObisCode("7.129.96.5.1.255")),
            new DlmsSimpleRegisterDefinition("0.2.96.12.5.255", new ObisCode("0.2.96.12.5.255")),
            new DlmsSimpleRegisterDefinition("0.3.96.12.5.255", new ObisCode("0.3.96.12.5.255")),
            new DlmsSimpleRegisterDefinition("0.0.96.6.6.255", Ek280Defs.BATTERY_REMAINING),
            new DlmsSimpleRegisterDefinition("0.2.96.6.3.255", Ek280Defs.MODEM_BATTERY_VOLTAGE),

            new DlmsSimpleRegisterDefinition("7.0.0.2.1.255", new ObisCode("7.0.0.2.1.255")),
            new DlmsSimpleRegisterDefinition("7.0.0.2.2.255", new ObisCode("7.0.0.2.2.255")),
            new DlmsSimpleRegisterDefinition("7.0.0.2.0.255", Ek280Defs.EQUIPMENT_CONFIG),
            new DlmsSimpleRegisterDefinition("0.0.96.1.10.255", Ek280Defs.METERING_POINT_ID),
            new DlmsSimpleRegisterDefinition("7.0.96.99.3.255", Ek280Defs.INST_METER_TYPE),
            new DlmsSimpleRegisterDefinition("7.0.96.99.2.255", Ek280Defs.INST_METER_CALIBER),
            new DlmsSimpleRegisterDefinition("7.0.0.2.14.255", Ek280Defs.INST_METER_SERIAL),
            // SN temperature sensor
            new DlmsSimpleRegisterDefinition("7.0.0.15.1.255", new ObisCode("7.0.0.15.1.255"), 67, 2),
            // SN pressure sensor
            new DlmsSimpleRegisterDefinition("7.0.0.15.2.255", new ObisCode("7.0.0.15.2.255"), 67, 2),

            new DlmsSimpleRegisterDefinition("7.0.11.2.0.255", new ObisCode("7.0.11.2.0.255")),
            new DlmsSimpleRegisterDefinition("7.0.13.2.0.255", Ek280Defs.VB_TOTAL_CURR),
            new DlmsSimpleRegisterDefinition("7.0.3.0.0.255", new ObisCode("7.0.3.0.0.255")),
            new DlmsSimpleRegisterDefinition("7.0.11.0.0.255", new ObisCode("7.0.11.0.0.255")),
            new DlmsSimpleRegisterDefinition("7.0.13.0.0.255", Ek280Defs.VM_TOTAL_CURR),
            new DlmsSimpleRegisterDefinition("7.0.33.2.0.255", new ObisCode("7.0.33.2.0.255")),
            new DlmsSimpleRegisterDefinition("7.0.42.0.0.255", Ek280Defs.PRESSURE_ABSOLUTE_CURR),
            new DlmsSimpleRegisterDefinition("7.0.41.0.0.255", Ek280Defs.TEMPERATURE_CURR),
            new DlmsSimpleRegisterDefinition("7.0.52.0.0.255", Ek280Defs.COEFFICIENT_C_CURR),
            new DlmsSimpleRegisterDefinition("7.0.53.0.0.255", Ek280Defs.COEFFICIENT_Z_CURR),
            new DlmsSimpleRegisterDefinition("7.0.43.0.0.255", Ek280Defs.FLOWRATE_VM_CURR),
            new DlmsSimpleRegisterDefinition("7.0.43.2.0.255", Ek280Defs.FLOWRATE_VB_CURR),
            new DlmsSimpleRegisterDefinition("7.0.0.12.45.255", Ek280Defs.DENSITY_GAS_BASE_COND),
            new DlmsSimpleRegisterDefinition("7.0.0.12.46.255", Ek280Defs.DENSITY_RATIO),
            new DlmsSimpleRegisterDefinition("7.0.0.12.54.255", Ek280Defs.CALORIFIC_VALUE_COMP_CURR),
            new DlmsSimpleRegisterDefinition("7.0.0.12.60.255", Ek280Defs.GAV_NITROGEN_CONT_CURR),
            new DlmsSimpleRegisterDefinition("7.0.0.12.61.255", Ek280Defs.GAV_HYDROGEN_CONT_CURR),
            new DlmsSimpleRegisterDefinition("7.0.0.12.65.255", Ek280Defs.GAV_CARBONOXID_CONT_CURR),
            new DlmsSimpleRegisterDefinition("7.0.0.12.66.255", Ek280Defs.GAV_CARBONDIOXID_CONT_CURR),
            new DlmsSimpleRegisterDefinition("7.0.0.12.67.255", Ek280Defs.GAV_METHAN_CONT_CURR),

            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.VOLUME_BASE_COND_CORR_HIST,
                    LP_MONTH, "7.0.11.2.0.255", 2, OC_TST, 2),
            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.VOLUME_BASE_COND_TOTAL_HIST,
                    LP_MONTH, "7.0.13.2.0.255", 2, OC_TST, 2),
            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.VOLUME_MEAS_COND_CORR_HIST,
                    LP_MONTH, "7.0.11.0.0.255", 2, OC_TST, 2),
            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.VOLUME_MEAS_COND_TOTAL_HIST,
                    LP_MONTH, "7.0.13.0.0.255", 2, OC_TST, 2),

            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.MAX_INT_VAL_MEAS_COND_HIST,
                    LP_MONTH, "7.0.13.54.0.101", 2, "7.0.13.54.0.101", 5),
            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.MAX_DAY_VAL_MEAS_COND_HIST,
                    LP_MONTH, "7.0.13.60.0.101", 2, "7.0.13.60.0.101", 5),
            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.MAX_INT_VAL_BASE_COND_HIST,
                    LP_MONTH, "7.0.13.56.0.101", 2, "7.0.13.56.0.101", 5),
            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.MAX_DAY_VAL_BASE_COND_HIST,
                    LP_MONTH, "7.0.11.62.0.101", 2, "7.0.11.62.0.101", 5),

            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.MAX_INT_FR_BASE_COND_HIST,
                    MP_MONTH, "7.0.43.206.20.255", 2, "7.0.43.206.20.255", 5),
            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.MIN_INT_FR_BASE_COND_HIST,
                    MP_MONTH, "7.0.43.216.20.255", 2, "7.0.43.216.20.255", 5),
            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.MAX_INT_FR_MEAS_COND_HIST,
                    MP_MONTH, "7.0.43.206.0.255", 2, "7.0.43.206.0.255", 5),
            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.MIN_INT_FR_MEAS_COND_HIST,
                    MP_MONTH, "7.0.43.216.0.255", 2, "7.0.43.216.0.255", 5),

            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.MEAN_PREASURE_HIST,
                    MP_MONTH, "7.0.42.78.0.255", 2, OC_TST, 2),
            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.MAX_INT_PREASURE_HIST,
                    MP_MONTH, "7.0.42.84.0.255", 2, "7.0.42.84.0.255", 5),
            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.MIN_INT_PREASURE_HIST,
                    MP_MONTH, "7.0.42.81.0.255", 2, "7.0.42.81.0.255", 5),

            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.MEAN_TEMPERATURE_HIST,
                    MP_MONTH, "7.0.41.78.0.255", 2, OC_TST, 2),
            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.MAX_INT_TEMPERATURE_HIST,
                    MP_MONTH, "7.0.41.84.0.255", 2, "7.0.41.84.0.255", 5),
            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.MIN_INT_TEMPERATURE_HIST,
                    MP_MONTH, "7.0.41.81.0.255", 2, "7.0.41.81.0.255", 5),

            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.MEAN_CONVERSION_FACTOR_HIST,
                    MP_MONTH, "7.0.52.0.20.255", 2, OC_TST, 2),
            new DlmsHistoricalRegisterDefinition(HistoricalObisCode.MEAN_COMPRESS_FACTOR_HIST,
                    MP_MONTH, "7.0.55.0.20.255", 2, OC_TST, 2),
    };

    protected static String archiveStructureVersion = "";
    protected IReadableRegister[] readableRegisters;

    public EK280() {
        super();
        ocIntervalProfile = Ek280Defs.LOAD_PROFILE_60;
        ocLogProfile = Ek280Defs.EVENT_LOG;

        logStructure = ITALYLOGSTRUCTURE;

        this.readableRegisters = italyMappings;
    }

    protected RegisterMap getRegisterMap() {

        return new RegisterMap(readableRegisters);
    }

    public String getProtocolVersion() {
        return "$Date: 2012-10-09 09:00:00 +0200 (vr, 10 sep 2012) $";
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected List doGetOptionalKeys() {

        List result = new ArrayList();
        result.add(EK280.ARCHIVESTRUCTUREVERSION);
        return result;
    }

    @Override
    protected void validateProperties(Properties properties)
            throws MissingPropertyException, InvalidPropertyException {
        archiveStructureVersion = properties.getProperty(EK280.ARCHIVESTRUCTUREVERSION, "");
        if (archiveStructureVersion.equalsIgnoreCase("V2")) {
            archiveStructure = V2ARCHIVESTRUCTURE;

            ocLogProfile = new ObisCode("7.0.99.98.1.255");
            logStructure = V2LOGSTRUCTURE;

            this.readableRegisters = v2Mappings;
        }
        super.validateProperties(properties);
    }

    @Override
    public DlmsMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            messageExecutor = new Ek280MessageExecutor(this);
        }
        return messageExecutor;
    }
}