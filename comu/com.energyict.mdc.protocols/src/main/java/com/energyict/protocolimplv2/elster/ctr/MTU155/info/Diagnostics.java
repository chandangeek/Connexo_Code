package com.energyict.protocolimplv2.elster.ctr.MTU155.info;

/**
 * Class with all common meter diagnostics
 * Copyrights EnergyICT
 * Date: 19-okt-2010
 * Time: 16:29:27
 */
public class Diagnostics {

    private static final int POWER_NOT_AVAILABLE_BIT = 0x0001;
    private static final int CONVERTER_LOW_BATTERY_BIT = 0x0002;
    private static final int EVENT_LOG_90P_BIT = 0x0004;
    private static final int GENERAL_ALARM_BIT = 0x0008;
    private static final int CONNECTION_BROKEN_BIT = 0x0010;
    private static final int EVENT_LOG_FULL_BIT = 0x0020;
    private static final int CLOCK_MISALIGNMENT_BIT = 0x0040;
    private static final int CONVERTER_ALARM_BIT = 0x0080;
    private static final int TEMPERATURE_OUT_OF_RANGE_BIT = 0x0100;
    private static final int PRESSURE_OUT_OF_RANGE_BIT = 0x0200;
    private static final int FLOW_OVER_LIMIT_BIT = 0x0400;
    private static final int VALVE_CLOSING_ERROR_BIT = 0x0800;
    private static final int VALVE_OPENING_ERROR_BIT = 0x1000;
    private static final int MTU155_LOW_BATTERY_BIT = 0x2000;

    private static final String POWER_NOT_AVAILABLE = "Mains power not available";
    private static final String CONVERTER_LOW_BATTERY = "Converter low Battery";
    private static final String EVENT_LOG_AT_90_PERCENT = "Event log at 90%";
    private static final String GENERAL_ALARM = "General alarm";
    private static final String CONNECTION_BROKEN = "Connection with emitter or converter broken";
    private static final String EVENT_LOG_FULL = "Event log full";
    private static final String CLOCK_MISALIGNMENT = "Clock misalignment";
    private static final String CONVERTER_ALARM = "Converter alarm";
    private static final String TEMPERATURE_OUT_OF_RANGE = "Temperature out of range";
    private static final String PRESSURE_OUT_OF_RANGE = "Pressure out of range";
    private static final String FLOW_OVER_LIMIT = "Flow over limit";
    private static final String VALVE_CLOSING_ERROR = "Valve closing error";
    private static final String VALVE_OPENING_ERROR = "Valve opening error";
    private static final String MTU155_LOW_BATTERY = "MTU155 low battery";

    /**
     * @param code: the diagnostics code
     * @return the matching diagnostics description(s)
     */
    public static String getDescriptionFromCode(int code) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            String bitMessage = getDescriptionFromSingleCode(code, i);
            if (bitMessage != null) {
                sb.append((sb.length() > 0) ? ", " : "").append(bitMessage);
            }
        }
        return sb.toString();
    }

    /**
     * @param code: a single bit indicating one diagnostic description
     * @return one matching description
     */
    private static String getDescriptionFromSingleCode(int code, int bitNumber) {
        int singleBitCode = code & (1 << bitNumber);
        switch (singleBitCode) {
            case POWER_NOT_AVAILABLE_BIT:
                return POWER_NOT_AVAILABLE;
            case CONVERTER_LOW_BATTERY_BIT:
                return CONVERTER_LOW_BATTERY;
            case EVENT_LOG_90P_BIT:
                return EVENT_LOG_AT_90_PERCENT;
            case GENERAL_ALARM_BIT:
                return GENERAL_ALARM;
            case CONNECTION_BROKEN_BIT:
                return CONNECTION_BROKEN;
            case EVENT_LOG_FULL_BIT:
                return EVENT_LOG_FULL;
            case CLOCK_MISALIGNMENT_BIT:
                return CLOCK_MISALIGNMENT;
            case CONVERTER_ALARM_BIT:
                return CONVERTER_ALARM;
            case TEMPERATURE_OUT_OF_RANGE_BIT:
                return TEMPERATURE_OUT_OF_RANGE;
            case PRESSURE_OUT_OF_RANGE_BIT:
                return PRESSURE_OUT_OF_RANGE;
            case FLOW_OVER_LIMIT_BIT:
                return FLOW_OVER_LIMIT;
            case VALVE_CLOSING_ERROR_BIT:
                return VALVE_CLOSING_ERROR;
            case VALVE_OPENING_ERROR_BIT:
                return VALVE_OPENING_ERROR;
            case MTU155_LOW_BATTERY_BIT:
                return MTU155_LOW_BATTERY;
            default:
                return null;
        }
    }

}
