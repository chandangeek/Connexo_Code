package com.energyict.protocolimpl.coronis.waveflow.core;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.LeakageEvent;

/**
 * Copyrights EnergyICT
 * Date: 13-mei-2011
 * Time: 14:29:13
 */
public class EventStatusAndDescription {

    public static final int EVENTCODE_DEFAULT = 0x00;
    public static final int EVENTCODE_BATTERY_LOW = 0x01;
    public static final int EVENTCODE_NO_FLOW = 0x02;
    private static final int EVENTCODE_MAGNETIC_TAMPER = 0x03;
    private static final int EVENTCODE_TAMPER_REMOVAL = 0x04;
    public static final int EVENTCODE_VALVE_FAULT = 0x05;
    public static final int EVENTCODE_WIRECUT_TAMPER_A = 0x10;
    public static final int EVENTCODE_WIRECUT_TAMPER_B = 0x11;
    public static final int EVENTCODE_WIRECUT_TAMPER_C = 0x12;
    public static final int EVENTCODE_WIRECUT_TAMPER_D = 0x13;
    public static final int EVENTCODE_REEDFAULT_A = 0x14;
    public static final int EVENTCODE_REEDFAULT_B = 0x15;
    private static final int EVENTCODE_BACKFLOW_VOLUMEMEASURING_START_A = 0x16;
    private static final int EVENTCODE_BACKFLOW_VOLUMEMEASURING_START_B = 0x17;
    private static final int EVENTCODE_BACKFLOW_VOLUMEMEASURING_END_A = 0x18;
    private static final int EVENTCODE_BACKFLOW_VOLUMEMEASURING_END_B = 0x19;
    private static final int EVENTCODE_BACKFLOW_FLOWRATE_START_A = 0x30;
    private static final int EVENTCODE_BACKFLOW_FLOWRATE_START_B = 0x31;
    private static final int EVENTCODE_BACKFLOW_FLOWRATE_END_A = 0x32;
    private static final int EVENTCODE_BACKFLOW_FLOWRATE_END_B = 0x33;
    public static final int EVENTCODE_LEAKAGE_RESIDUAL_START_A = 0x1A;
    public static final int EVENTCODE_LEAKAGE_RESIDUAL_START_B = 0x1B;
    private static final int EVENTCODE_LEAKAGE_RESIDUAL_START_C = 0x1C;
    private static final int EVENTCODE_LEAKAGE_RESIDUAL_START_D = 0x1D;
    private static final int EVENTCODE_LEAKAGE_RESIDUAL_A_END = 0x1E;
    private static final int EVENTCODE_LEAKAGE_RESIDUAL_B_END = 0x1F;
    private static final int EVENTCODE_LEAKAGE_RESIDUAL_C_END = 0x20;
    private static final int EVENTCODE_LEAKAGE_RESIDUAL_D_END = 0x21;
    public static final int EVENTCODE_LEAKAGE_EXTREME_START_A = 0x22;
    public static final int EVENTCODE_LEAKAGE_EXTREME_START_B = 0x23;
    private static final int EVENTCODE_LEAKAGE_EXTREME_START_C = 0x24;
    private static final int EVENTCODE_LEAKAGE_EXTREME_START_D = 0x25;
    private static final int EVENTCODE_LEAKAGE_EXTREME_END_A = 0x26;
    private static final int EVENTCODE_LEAKAGE_EXTREME_END_B = 0x27;
    private static final int EVENTCODE_LEAKAGE_EXTREME_END_C = 0x28;
    public static final int EVENTCODE_LEAKAGE_EXTREME_END_D = 0x29;
    public static final int EVENTCODE_BURST = 0x34;
    public static final int EVENTCODE_OVERSPEED = 0x35;

    public static final int EVENTCODE_SIMPLE_BACKFLOW_A = 0x3E;
    public static final int EVENTCODE_SIMPLE_BACKFLOW_B = 0x3F;
    public static final int EVENTCODE_BATTERY_LOW_PROBE = 0x40;
    public static final int EVENTCODE_DAILY_HYDREKA_DATA = 0x41;
    public static final int EVENTCODE_BADTIME_DATALOGGING = 0x42;

    //Amplex MBus Echodis meter specific
    public static final int EVENTCODE_AIR_IN_PIPE_A = 0x43;
    public static final int EVENTCODE_AIR_IN_PIPE_B = 0x44;
    public static final int EVENTCODE_OVERFLOW_A = 0x45;
    public static final int EVENTCODE_OVERFLOW_B = 0x46;
    public static final int EVENTCODE_US_ASIC_A = 0x47;
    public static final int EVENTCODE_US_ASIC_B = 0x48;
    public static final int EVENTCODE_DIRTY_TRANS_A = 0x49;
    public static final int EVENTCODE_DIRTY_TRANS_B = 0x4A;
    public static final int EVENTCODE_VERY_DIRTY_TRANS_A = 0x4B;
    public static final int EVENTCODE_VERY_DIRTY_TRANS_B = 0x4C;
    public static final int EVENTCODE_MICRO_COM_A = 0x4D;
    public static final int EVENTCODE_MICRO_COM_B = 0x4E;
    public static final int EVENTCODE_FLOWMETER_TAMPER_A = 0x4F;
    public static final int EVENTCODE_FLOWMETER_TAMPER_B = 0x50;
    public static final int EVENTCODE_MODEM_PSTN_A = 0x51;
    public static final int EVENTCODE_MODEM_PSTN_B = 0x52;

    //Amplex Encoder meter specific
    public static final int EVENTCODE_DRY_A = 0x53;
    public static final int EVENTCODE_DRY_B = 0x54;
    public static final int EVENTCODE_LEAK_A = 0x55;
    public static final int EVENTCODE_LEAK_B = 0x56;
    public static final int EVENTCODE_TAMPER_A = 0x57;
    public static final int EVENTCODE_TAMPER_B = 0x58;
    public static final int EVENTCODE_NOFLOW_A = 0x59;
    public static final int EVENTCODE_NOFLOW_B = 0x5A;
    public static final int EVENTCODE_CELL_LOW_A = 0x5B;
    public static final int EVENTCODE_CELL_LOW_B = 0x5C;
    public static final int EVENTCODE_EEPROM_FAULT_A = 0x5D;
    public static final int EVENTCODE_EEPROM_FAULT_B = 0x5E;

    //As(1)253 error registers
    public static final int FATAL_ERROR = 0x5F;
    public static final int LOSS_OF_TIME_AND_DATE = 0x60;
    public static final int EXTERNAL_BATTERY_EMPTY = 0x61;
    public static final int INTERNAL_BATTERY_EMPTY = 0x62;
    public static final int MAGNETIC_FIELD_DETECTION = 0x63;
    public static final int MAIN_COVER_REMOVAL = 0x64;
    public static final int TERMINAL_COVER_REMOVAL = 0x65;
    public static final int CHECKSUM_ERROR = 0x66;
    public static final int UI_LAODPROFILE_INIT_ERROR = 0x67;
    public static final int RIPPLE_COMM_ERROR = 0x68;

    public static final int THRESHOLD_ACTIVE = 0x69;
    public static final int THRESHOLD_EXCEEDED1 = 0x6A;
    public static final int THRESHOLD_EXCEEDED2 = 0x6B;
    public static final int LOGBOOK_STOPPED = 0x6C;
    public static final int COMMUNICATION_ERROR_TO_THE_RIPPLE_RECEIVER = 0x6D;
    public static final int TANGENT_PHI_Q4_OVERLOAD = 0x6E;
    public static final int TANGENT_PHI_Q1_OVERLOAD = 0x6F;
    public static final int REVERSE_POWER = 0x70;
    public static final int REVERSE_PULSE = 0x71;
    public static final int NO_LOAD_P3 = 0x72;
    public static final int NO_LOAD_P2 = 0x73;
    public static final int NO_LOAD_P1 = 0x74;
    public static final int COMMUNICATION_ERROR_METER_CHIP = 0x75;
    public static final int CONFIGURABLE_EVENT2 = 0x76;
    public static final int CONFIGURABLE_EVENT1 = 0x77;
    public static final int ROTATION_FIELD_WRONG = 0x78;
    public static final int PHASE_POTENTIAL_MISSING = 0x79;

    public static final int POWER_QUALITY_MONITORING_VALUE_8_OVERFLOW = 0x7A;
    public static final int POWER_QUALITY_MONITORING_VALUE_8_UNDERFLOW = 0x7B;
    public static final int POWER_QUALITY_MONITORING_VALUE_7_OVERFLOW = 0x7C;
    public static final int POWER_QUALITY_MONITORING_VALUE_7_UNDERFLOW = 0x7D;
    public static final int POWER_QUALITY_MONITORING_VALUE_6_OVERFLOW = 0x7E;
    public static final int POWER_QUALITY_MONITORING_VALUE_6_UNDERFLOW = 0x7F;
    public static final int POWER_QUALITY_MONITORING_VALUE_5_OVERFLOW = 0x80;
    public static final int POWER_QUALITY_MONITORING_VALUE_5_UNDERFLOW = 0x81;
    public static final int POWER_QUALITY_MONITORING_VALUE_4_OVERFLOW = 0x82;
    public static final int POWER_QUALITY_MONITORING_VALUE_4_UNDERFLOW = 0x83;
    public static final int POWER_QUALITY_MONITORING_VALUE_3_OVERFLOW = 0x84;
    public static final int POWER_QUALITY_MONITORING_VALUE_3_UNDERFLOW = 0x85;
    public static final int POWER_QUALITY_MONITORING_VALUE_2_OVERFLOW = 0x86;
    public static final int POWER_QUALITY_MONITORING_VALUE_2_UNDERFLOW = 0x87;
    public static final int POWER_QUALITY_MONITORING_VALUE_1_OVERFLOW = 0x88;
    public static final int POWER_QUALITY_MONITORING_VALUE_1_UNDERFLOW = 0x89;

    //A1800 warning registers
    public static final int LOW_BATTERY = 0x90;
    public static final int IMPROPER_METER_ENGINE_OPERATION = 0x91;
    public static final int REVERSE_ENERGY = 0x92;
    public static final int POTENTIAL_INDICATOR = 0x93;
    public static final int DEMAND_OVERLOAD = 0x94;
    public static final int SERVICE_CURRENT_TEST_FAILURE = 0x95;
    public static final int DEMAND_THRESHOLD_EXCEEDED = 0x96;
    public static final int LINE_FREQUENCY_WARNING = 0x97;
    public static final int TRUEQ_TEST_FAILURE = 0x98;
    public static final int END_OF_CALENDAR = 0x99;
    public static final int READ_WITHOUT_POWER_BATTERY_DISCHARGED = 0x9A;
    public static final int READ_WITHOUT_POWER_MODE_ACTIVE = 0x9B;

    //A1800 error registers
    public static final int ERROR_REGISTER1 = 0x9C;
    public static final int ERROR_REGISTER2 = 0x9D;
    public static final int ERROR_REGISTER3 = 0x9E;


    private final int deviceType;

    public EventStatusAndDescription(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getProtocolCodeForSimpleBackflow(int input) {
        if (input == 0) {
            return EVENTCODE_SIMPLE_BACKFLOW_A;
        }
        if (input == 1) {
            return EVENTCODE_SIMPLE_BACKFLOW_B;
        }
        return EVENTCODE_DEFAULT;
    }

    //Waveflow alarm frame has weird index mapping
    public int getProtocolCodeForWireCut(int input) {
        switch (input) {
            case 0:
                return EVENTCODE_WIRECUT_TAMPER_C;
            case 1:
                return EVENTCODE_WIRECUT_TAMPER_A;
            case 2:
                return EVENTCODE_WIRECUT_TAMPER_B;
            case 3:
                return EVENTCODE_WIRECUT_TAMPER_D;
        }
        return EVENTCODE_DEFAULT;
    }

    public int getEventCode(int bit) {
        switch (bit) {
            case 0x02:
                if (deviceType == 0 || deviceType == 3) {
                    return MeterEvent.TAMPER;
                }
                break;
            case 0x04:
                if (deviceType == 0) {
                    return MeterEvent.TAMPER;
                }
                break;
            case 0x20:
                if (deviceType == 1 || deviceType == 3) {
                    return MeterEvent.STRONG_DC_FIELD_DETECTED;
                }
                break;
        }
        return MeterEvent.METER_ALARM;
    }

    public int getProtocolCodeForStatus(int bit) {
        switch (bit) {
            case 0x02:
                if (deviceType == 0) {
                    return EVENTCODE_WIRECUT_TAMPER_A;
                } else if (deviceType == 3) {
                    return EVENTCODE_TAMPER_REMOVAL;
                } else {
                    return EVENTCODE_NO_FLOW;
                }
            case 0x04:
                if (deviceType == 0) {
                    return EVENTCODE_WIRECUT_TAMPER_B;
                } else {
                    return EVENTCODE_NO_FLOW;
                }
            case 0x20:
                if (deviceType == 1 || deviceType == 3) {
                    return EVENTCODE_MAGNETIC_TAMPER;
                }
                break;
        }
        return EVENTCODE_DEFAULT;
    }

    public String getEventDescription(int bit) {
        switch (bit) {
            case 0x02:
                if (deviceType == 0) {
                    return "Tamper (wirecut A)";
                } else if (deviceType == 3) {
                    return "Tamper (removal)";
                } else {
                    return "No flow";
                }
            case 0x04:
                if (deviceType == 0) {
                    return "Tamper (wirecut B)";
                } else {
                    return "No flow";
                }
            case 0x20:
                if (deviceType == 0) {
                    return "Tamper (wirecut C)";
                } else if (deviceType == 1 || deviceType == 3) {
                    return "Tamper (magnetic)";
                }
                break;
        }
        return "";
    }

    public int getProtocolCodeForAdvancedBackflowVolumeMeasuring(int input, boolean start) {
        if (input == 0) {
            return start ? EVENTCODE_BACKFLOW_VOLUMEMEASURING_START_A : EVENTCODE_BACKFLOW_VOLUMEMEASURING_END_A;
        }
        if (input == 1) {
            return start ? EVENTCODE_BACKFLOW_VOLUMEMEASURING_START_B : EVENTCODE_BACKFLOW_VOLUMEMEASURING_END_B;
        }
        return EVENTCODE_DEFAULT;
    }

    public int getProtocolCodeForAdvancedBackflowFlowRate(int input, boolean start) {
        if (input == 0) {
            return start ? EVENTCODE_BACKFLOW_FLOWRATE_START_A : EVENTCODE_BACKFLOW_FLOWRATE_END_A;
        }
        if (input == 1) {
            return start ? EVENTCODE_BACKFLOW_FLOWRATE_START_B : EVENTCODE_BACKFLOW_FLOWRATE_END_B;
        }
        return EVENTCODE_DEFAULT;
    }

    public int getProtocolCodeForLeakage(String startOrEnd, String type, String input) {
        if (LeakageEvent.START.equals(startOrEnd)) {
            if (LeakageEvent.LEAKAGETYPE_EXTREME.equals(type)) {
                if (LeakageEvent.A.equals(input)) {
                    return EVENTCODE_LEAKAGE_EXTREME_START_A;
                }
                if (LeakageEvent.B.equals(input)) {
                    return EVENTCODE_LEAKAGE_EXTREME_START_B;
                }
                if (LeakageEvent.C.equals(input)) {
                    return EVENTCODE_LEAKAGE_EXTREME_START_C;
                }
                if (LeakageEvent.D.equals(input)) {
                    return EVENTCODE_LEAKAGE_EXTREME_START_D;
                }
            } else if (LeakageEvent.LEAKAGETYPE_RESIDUAL.equals(type)) {
                if (LeakageEvent.A.equals(input)) {
                    return EVENTCODE_LEAKAGE_RESIDUAL_START_A;
                }
                if (LeakageEvent.B.equals(input)) {
                    return EVENTCODE_LEAKAGE_RESIDUAL_START_B;
                }
                if (LeakageEvent.C.equals(input)) {
                    return EVENTCODE_LEAKAGE_RESIDUAL_START_C;
                }
                if (LeakageEvent.D.equals(input)) {
                    return EVENTCODE_LEAKAGE_RESIDUAL_START_D;
                }
            }
        } else if (LeakageEvent.END.equals(startOrEnd)) {
            if (LeakageEvent.LEAKAGETYPE_EXTREME.equals(type)) {
                if (LeakageEvent.A.equals(input)) {
                    return EVENTCODE_LEAKAGE_EXTREME_END_A;
                }
                if (LeakageEvent.B.equals(input)) {
                    return EVENTCODE_LEAKAGE_EXTREME_END_B;
                }
                if (LeakageEvent.C.equals(input)) {
                    return EVENTCODE_LEAKAGE_EXTREME_END_C;
                }
                if (LeakageEvent.D.equals(input)) {
                    return EVENTCODE_LEAKAGE_EXTREME_END_D;
                }
            } else if (LeakageEvent.LEAKAGETYPE_RESIDUAL.equals(type)) {
                if (LeakageEvent.A.equals(input)) {
                    return EVENTCODE_LEAKAGE_RESIDUAL_A_END;
                }
                if (LeakageEvent.B.equals(input)) {
                    return EVENTCODE_LEAKAGE_RESIDUAL_B_END;
                }
                if (LeakageEvent.C.equals(input)) {
                    return EVENTCODE_LEAKAGE_RESIDUAL_C_END;
                }
                if (LeakageEvent.D.equals(input)) {
                    return EVENTCODE_LEAKAGE_RESIDUAL_D_END;
                }
            }
        }
        return EVENTCODE_DEFAULT;
    }
}