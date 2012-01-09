package com.elster.protocolimpl.dlms;

import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.protocolimpl.dlms.registers.DlmsRegisterMapping;
import com.elster.protocolimpl.dlms.registers.RegisterMap;

/**
 * User: heuckeg
 * Date: 22.09.11
 * Time: 14:42
 */
@SuppressWarnings({"unused"})
public class EK280 extends Dlms {

    private static DlmsRegisterMapping[] mappings = {
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 0, 2, 1, 255), Ek280Defs.SOFTWARE_VERSION),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 0, 2, 13, 255), Ek280Defs.DLMS_DEVICE_SERIAL),

            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(0, 0, 96, 10, 2, 255), Ek280Defs.BILLING_STATUS_REGISTER),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(0, 2, 96, 10, 1, 255), Ek280Defs.STATUS_REGISTER_1_2),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(0, 3, 96, 12, 5, 255), Ek280Defs.GSM_SIGNAL_STRENGTH),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(0, 3, 96, 12, 6, 255), Ek280Defs.GSM_PHONE_NUMBER),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(0, 0, 96, 6, 6, 255), Ek280Defs.BATTERY_REMAINING),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 0, 9, 4, 255), Ek280Defs.REMAINING_SHIFT_TIME),

            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(0, 0, 96, 52, 0, 255), Ek280Defs.INSTALLATION_DATE),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 0, 2, 0, 255), Ek280Defs.EQUIPMENT_CONFIG),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(0, 0, 96, 1, 10, 255), Ek280Defs.METERING_POINT_ID),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 96, 99, 3, 255), Ek280Defs.INST_METER_TYPE),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 96, 99, 2, 255), Ek280Defs.INST_METER_CALIBER),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 0, 2, 14, 255), Ek280Defs.INST_METER_SERIAL),

            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 13, 0, 0, 255), Ek280Defs.VM_TOTAL_CURR),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 13, 2, 0, 255), Ek280Defs.VB_TOTAL_CURR),

            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 11, 83, 0, 255), Ek280Defs.VM_UNDISTURBED_TOTAL_CURR_PERIOD),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 11, 83, 1, 255), Ek280Defs.VM_UNDISTURBED_F1_CURR_PERIOD),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 11, 83, 2, 255), Ek280Defs.VM_UNDISTURBED_F2_CURR_PERIOD),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 11, 83, 3, 255), Ek280Defs.VM_UNDISTURBED_F3_CURR_PERIOD),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 11, 83, 0, 101), Ek280Defs.VM_UNDISTURBED_TOTAL_PREV_PERIOD),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 11, 83, 1, 101), Ek280Defs.VM_UNDISTURBED_F1_PREV_PERIOD),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 11, 83, 2, 101), Ek280Defs.VM_UNDISTURBED_F2_PREV_PERIOD),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 11, 83, 3, 101), Ek280Defs.VM_UNDISTURBED_F3_PREV_PERIOD),

            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 12, 81, 0, 255), Ek280Defs.VM_DISTURBED_TOTAL_CURR_PERIOD),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 12, 81, 1, 255), Ek280Defs.VM_DISTURBED_F1_CURR_PERIOD),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 12, 81, 2, 255), Ek280Defs.VM_DISTURBED_F2_CURR_PERIOD),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 12, 81, 3, 255), Ek280Defs.VM_DISTURBED_F3_CURR_PERIOD),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 12, 81, 0, 101), Ek280Defs.VM_DISTURBED_TOTAL_PREV_PERIOD),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 12, 81, 1, 101), Ek280Defs.VM_DISTURBED_F1_PREV_PERIOD),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 12, 81, 2, 101), Ek280Defs.VM_DISTURBED_F2_PREV_PERIOD),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 12, 81, 3, 101), Ek280Defs.VM_DISTURBED_F3_PREV_PERIOD),

            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 42, 2, 0, 255), Ek280Defs.PRESSURE_REFERENCE),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 41, 2, 0, 255), Ek280Defs.TEMPERATURE_REFERENCE),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 42, 0, 0, 255), Ek280Defs.PRESSURE_ABSOLUTE_CURR),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 41, 0, 0, 255), Ek280Defs.TEMPERATURE_CURR),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 43, 0, 0, 255), Ek280Defs.FLOWRATE_VM_CURR),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 43, 2, 0, 255), Ek280Defs.FLOWRATE_VB_CURR),

            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 52, 0, 0, 255), Ek280Defs.COEFFICIENT_C_CURR),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 53, 0, 0, 255), Ek280Defs.COEFFICIENT_Z_CURR),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 53, 12, 0, 255), Ek280Defs.Z_CALC_METHOD),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 0, 4, 0, 255), Ek280Defs.VOLUME_CALC_METHOD),

            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 0, 12, 45, 255), Ek280Defs.DENSITY_GAS_BASE_COND),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 0, 12, 46, 255), Ek280Defs.DENSITY_RATIO),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 0, 12, 54, 255), Ek280Defs.CALORIFIC_VALUE_COMP_CURR),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 0, 12, 60, 255), Ek280Defs.GAV_NITROGEN_CONT_CURR),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 0, 12, 61, 255), Ek280Defs.GAV_HYDROGEN_CONT_CURR),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 0, 12, 65, 255), Ek280Defs.GAV_CARBONOXID_CONT_CURR),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 0, 12, 66, 255), Ek280Defs.GAV_CARBONDIOXID_CONT_CURR),
            new DlmsRegisterMapping(new com.energyict.obis.ObisCode(7, 0, 0, 12, 67, 255), Ek280Defs.GAV_METHAN_CONT_CURR)
    };


    public EK280() {
        super();
    }

    protected RegisterMap getRegisterMap() {

        return new RegisterMap(mappings);
    }

    public String getProtocolVersion() {
        return "$Date: 2011-06-10 16:05:38 +0200 (vr, 10 jun 2011) $";
    }


}
