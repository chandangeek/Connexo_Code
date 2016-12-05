package com.energyict.mdc.channels.serial;

import com.energyict.cpo.Environment;

/**
 * Provide predefined values for the FlowControl
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
            typedValues[i++] = flowControl.getFlowControl();
        }
        return typedValues;
    }

    public String getFlowControl() {
        return flowControl;
    }

    public static FlowControl valueFor (String strValue) {
        for (FlowControl flowControl : values()) {
            if (flowControl.getFlowControl().equals(strValue)) {
                return flowControl;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return Environment.getDefault().getTranslation(getFlowControl());
    }
}
