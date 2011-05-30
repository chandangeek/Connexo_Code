package com.energyict.protocolimpl.coronis.amco.rtm.core;

import com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand.LeakageEvent;

/**
 * Copyrights EnergyICT
 * Date: 13-mei-2011
 * Time: 14:29:13
 */
public class EventStatusAndDescription {

    public static final int EVENTCODE_DEFAULT = 0x00;
    public static final int EVENTCODE_BATTERY_LOW = 0x01;
    public static final int EVENTCODE_VALVE_FAULT = 0x05;
    public static final int EVENTCODE_WIRECUT_TAMPER_A = 0x10;
    public static final int EVENTCODE_WIRECUT_TAMPER_B = 0x11;
    public static final int EVENTCODE_WIRECUT_TAMPER_C = 0x12;
    public static final int EVENTCODE_WIRECUT_TAMPER_D = 0x13;
    public static final int EVENTCODE_BACKFLOW_END_A = 0x18;
    public static final int EVENTCODE_BACKFLOW_END_B = 0x19;
    public static final int EVENTCODE_LEAKAGE_RESIDUAL_START_A = 0x1A;
    public static final int EVENTCODE_LEAKAGE_RESIDUAL_START_B = 0x1B;
    public static final int EVENTCODE_LEAKAGE_RESIDUAL_START_C = 0x1C;
    public static final int EVENTCODE_LEAKAGE_RESIDUAL_START_D = 0x1D;
    public static final int EVENTCODE_LEAKAGE_RESIDUAL_A_END = 0x1E;
    public static final int EVENTCODE_LEAKAGE_RESIDUAL_B_END = 0x1F;
    public static final int EVENTCODE_LEAKAGE_RESIDUAL_C_END = 0x20;
    public static final int EVENTCODE_LEAKAGE_RESIDUAL_D_END = 0x21;
    public static final int EVENTCODE_LEAKAGE_EXTREME_START_A = 0x22;
    public static final int EVENTCODE_LEAKAGE_EXTREME_START_B = 0x23;
    public static final int EVENTCODE_LEAKAGE_EXTREME_START_C = 0x24;
    public static final int EVENTCODE_LEAKAGE_EXTREME_START_D = 0x25;
    public static final int EVENTCODE_LEAKAGE_EXTREME_END_A = 0x26;
    public static final int EVENTCODE_LEAKAGE_EXTREME_END_B = 0x27;
    public static final int EVENTCODE_LEAKAGE_EXTREME_END_C = 0x28;
    public static final int EVENTCODE_LEAKAGE_EXTREME_END_D = 0x29;
    public static final int EVENTCODE_ENCODER_MISREAD_A = 0x2A;
    public static final int EVENTCODE_ENCODER_MISREAD_B = 0x2B;
    public static final int EVENTCODE_ENCODER_COMMUNICATION_FAULT_A = 0x2C;
    public static final int EVENTCODE_ENCODER_COMMUNICATION_FAULT_B = 0x2D;

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