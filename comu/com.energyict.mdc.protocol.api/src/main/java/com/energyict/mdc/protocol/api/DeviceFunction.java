package com.energyict.mdc.protocol.api;

import com.elster.jupiter.metering.ServiceKind;
import com.energyict.mdc.common.BusinessException;

public enum DeviceFunction {

    NONE(0, "deviceFunction.none"),
    METER(1, "deviceFunction.meter"),
    GATEWAY(2, "deviceFunction.gateway"),
    REPEATER(3, "deviceFunction.repeater"),
    INHOMEDISPLAY(4, "deviceFunction.inhomedisplay"),
    CONCENTRATOR(5, "deviceFunction.concentrator");

    private int code;
    private String nameKey;

    private DeviceFunction(int code, String nameKey) {
        this.code = code;
        this.nameKey = nameKey;
    }

    public int getCode() {
        return code;
    }

    public String getNameKey() {
        return nameKey;
    }

    public ServiceKind getServiceKind() {
        return ServiceKind.ELECTRICITY; // TODO Implement me
    }

    public static DeviceFunction fromDb(int code) throws BusinessException {
        for (DeviceFunction deviceFunction : values()) {
            if (deviceFunction.code==code) {
                return deviceFunction;
            }
        }
        throw new BusinessException("noDeviceUsageTypeDefinedForCodeX", "No DeviceUsageType defined for code {0}", code);
    }

}
