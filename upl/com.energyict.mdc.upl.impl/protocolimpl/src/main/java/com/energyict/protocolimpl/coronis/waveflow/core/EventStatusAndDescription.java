package com.energyict.protocolimpl.coronis.waveflow.core;

import com.energyict.protocol.MeterEvent;
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
    private static final int EVENTCODE_LEAKAGE_RESIDUAL_START_A = 0x1A;
    private static final int EVENTCODE_LEAKAGE_RESIDUAL_START_B = 0x1B;
    private static final int EVENTCODE_LEAKAGE_RESIDUAL_START_C = 0x1C;
    private static final int EVENTCODE_LEAKAGE_RESIDUAL_START_D = 0x1D;
    private static final int EVENTCODE_LEAKAGE_RESIDUAL_A_END = 0x1E;
    private static final int EVENTCODE_LEAKAGE_RESIDUAL_B_END = 0x1F;
    private static final int EVENTCODE_LEAKAGE_RESIDUAL_C_END = 0x20;
    private static final int EVENTCODE_LEAKAGE_RESIDUAL_D_END = 0x21;
    private static final int EVENTCODE_LEAKAGE_EXTREME_START_A = 0x22;
    private static final int EVENTCODE_LEAKAGE_EXTREME_START_B = 0x23;
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

    private WaveFlow waveFlow;

    public EventStatusAndDescription(WaveFlow waveFlow) {
        this.waveFlow = waveFlow;
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
        int deviceType = waveFlow.getDeviceType();
        switch (bit) {
            case 0x02:
                if (deviceType == 0 || deviceType == 3) {
                    return MeterEvent.TAMPER;
                }
            case 0x20:
                if (deviceType == 1 || deviceType == 3) {
                    return MeterEvent.STRONG_DC_FIELD_DETECTED;
                }
        }
        return MeterEvent.OTHER;
    }

    public int getProtocolCodeForStatus(int bit) {
        int deviceType = waveFlow.getDeviceType();
        switch (bit) {
            case 0x02:
                if (deviceType == 0) {
                    return EVENTCODE_WIRECUT_TAMPER_A;
                } else if (deviceType == 3) {
                    return EVENTCODE_TAMPER_REMOVAL;
                } else {
                    return EVENTCODE_NO_FLOW;
                }
            case 0x20:
                if (deviceType == 1 || deviceType == 3) {
                    return EVENTCODE_MAGNETIC_TAMPER;
                }
        }
        return EVENTCODE_DEFAULT;
    }

    public String getEventDescription(int bit) {
        int deviceType = waveFlow.getDeviceType();
        switch (bit) {
            case 0x02:
                if (deviceType == 0) {
                    return "Tamper (wire cut)";
                } else if (deviceType == 3) {
                    return "Tamper (removal)";
                } else {
                    return "No flow";
                }
            case 0x20:
                if (deviceType == 1 || deviceType == 3) {
                    return "Tamper (magnet)";
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