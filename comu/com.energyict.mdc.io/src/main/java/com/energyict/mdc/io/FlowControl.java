package com.energyict.mdc.io;

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

    private FlowControl(String flowControl) {
        this.flowControl = flowControl;
    }

    public static String[] getTypedValues() {
        String[] typedValues = new String[values().length];
        int i = 0;
        for (FlowControl flowControl : values()) {
            typedValues[i++] = flowControl.value();
        }
        return typedValues;
    }

    public String value() {
        return flowControl;
    }

    public static FlowControl valueFor (String strValue) {
        for (FlowControl flowControl : values()) {
            if (flowControl.value().equals(strValue)) {
                return flowControl;
            }
        }
        return FlowControl.NONE;    // The default
    }

}