package com.energyict.mdc.io;

import java.util.Arrays;

/**
 * Provide predefined values for the FlowControl.
 */
public enum FlowControl {
    NONE("flowcontrol_none"),
    RTSCTS("flowcontrol_rts_cts"),
    DTRDSR("flowcontrol_dtr_dsr"),
    XONXOFF("flowcontrol_xon_xoff")
    ;

    private final String flowControl;

    FlowControl(String flowControl) {
        this.flowControl = flowControl;
    }

    public static String[] getTypedValues() {
        return Arrays.stream(values()).map(FlowControl::value).toArray(String[]::new);
    }

    public String value() {
        return flowControl;
    }

    public static FlowControl valueFor (String strValue) {
        return Arrays.stream(values()).filter(x -> x.value().equals(strValue)).findFirst().orElse(null);
    }

}