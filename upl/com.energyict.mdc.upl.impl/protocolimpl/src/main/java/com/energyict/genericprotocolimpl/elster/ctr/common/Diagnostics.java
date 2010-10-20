package com.energyict.genericprotocolimpl.elster.ctr.common;

/**
 * Copyrights EnergyICT
 * Date: 19-okt-2010
 * Time: 16:29:27
 */
public class Diagnostics {

    public static final String POWER_NOT_AVAILABLE = "Mains power not available";
    public static final String LOW_BATTERY = "Low Battery";
    public static final String EVENT_LOG_AT_90_PERCENT = "Event log at 90%";
    public static final String GENERAL_ALARM = "General alarm";
    public static final String CONNECTION_BROKEN = "Connection with emitter or converter broken";
    public static final String EVENT_LOG_FULL = "Event log full";
    public static final String CLOCK_MISALIGNMENT = "Clock misalignment";
    public static final String CONVERTER_ALARM = "Converter alarm";
    public static final String TEMPERATURE_OUT_OF_RANGE = "Temperature out of range";
    public static final String PRESSURE_OUT_OF_RANGE =  "Pressure out of range";
    public static final String FLOW_OVER_LIMIT = "Flow over limit";
    public static final String VALVE_CLOSING_ERROR = "Valve closing error";
    public static final String VALVE_OPENING_ERROR = "Valve opening error";

    public static String getDescriptionFromCode(int code) {
        String description = "";

        for (int i = 0; i < 13; i++) {
            switch (i) {
                case 0:
                    if (isBitSet(code, i)) {
                        description = POWER_NOT_AVAILABLE;
                    }
                case 1:
                    if (isBitSet(code, i)) {
                        description = LOW_BATTERY;
                    }
                case 2:
                    if (isBitSet(code, i)) {
                        description = EVENT_LOG_AT_90_PERCENT;
                    }
                case 3:
                    if (isBitSet(code, i)) {
                        description = GENERAL_ALARM;
                    }
                case 4:
                    if (isBitSet(code, i)) {
                        description = CONNECTION_BROKEN;
                    }
                case 5:
                    if (isBitSet(code, i)) {
                        description = EVENT_LOG_FULL;
                    }
                case 6:
                    if (isBitSet(code, i)) {
                        description = CLOCK_MISALIGNMENT;
                    }
                case 7:
                    if (isBitSet(code, i)) {
                        description = CONVERTER_ALARM;
                    }
                case 8:
                    if (isBitSet(code, i)) {
                        description = TEMPERATURE_OUT_OF_RANGE;
                    }
                case 9:
                    if (isBitSet(code, i)) {
                        description = PRESSURE_OUT_OF_RANGE;
                    }
                case 10:
                    if (isBitSet(code, i)) {
                        description = FLOW_OVER_LIMIT;
                    }
                case 11:
                    if (isBitSet(code, i)) {
                        description = VALVE_CLOSING_ERROR;
                    }
                case 12:
                    if (isBitSet(code, i)) {
                        description = VALVE_OPENING_ERROR;
                    }
            }
        }
        return description;
    }


    private static boolean isBitSet(int value, int bitNr) {
        return (0 != (value & (0x01 << bitNr)));
    }


}
