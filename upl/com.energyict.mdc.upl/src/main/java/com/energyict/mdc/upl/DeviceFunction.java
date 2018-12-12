package com.energyict.mdc.upl;

public enum DeviceFunction {

    NONE(0, "deviceFunction.none"),
    METER(1, "deviceFunction.meter"),
    GATEWAY(2, "deviceFunction.gateway"),
    REPEATER(3, "deviceFunction.repeater"),
    INHOMEDISPLAY(4, "deviceFunction.inhomedisplay"),
    CONCENTRATOR(5, "deviceFunction.concentrator");

    private int code;
    private String nameKey;

    DeviceFunction(int code, String nameKey) {
        this.code = code;
        this.nameKey = nameKey;
    }

    public int getCode() {
        return code;
    }
}