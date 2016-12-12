package com.energyict.mdc.channels.serial;

import java.math.BigDecimal;
import java.util.stream.Stream;

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

    FlowControl(String flowControl) {
        this.flowControl = flowControl;
    }

    public static String[] getTypedValues() {
        return Stream
                .of(values())
                .map(FlowControl::getFlowControl)
                .toArray(String[]::new);
    }

    public String getFlowControl() {
        return flowControl;
    }

    public static FlowControl valueFor (String strValue) {
        return Stream
                .of(values())
                .filter(each -> each.getFlowControl().equals(strValue))
                .findAny()
                .orElse(null);
    }

}