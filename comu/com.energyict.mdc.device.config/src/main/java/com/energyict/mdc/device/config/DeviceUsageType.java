package com.energyict.mdc.device.config;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Environment;

/**
 * User: gde
 * Date: 30/10/12
 */
public enum DeviceUsageType {

    NONE(0, "deviceUsageType.none"),
    METER(1, "deviceUsageType.meter"),
    CONVERTOR(2, "deviceUsageType.convertor"),
    CALCULATOR(3, "deviceUsageType.calculator"),
    OTHER(999, "deviceUsageType.other");


    private int code;
    private String nameKey;

    private DeviceUsageType(int code, String nameKey) {
        this.code = code;
        this.nameKey = nameKey;
    }

    public int getCode() {
        return code;
    }

    public String getNameKey() {
        return nameKey;
    }

    public String getLocalizedName() {
        return Environment.DEFAULT.get().getTranslation(getNameKey());
    }

    public static DeviceUsageType fromDb(int code) throws BusinessException {
        switch (code) {
            case 0: return NONE;
            case 1: return METER;
            case 2: return CONVERTOR;
            case 3: return CALCULATOR;
            case 999: return OTHER;
            default:
                throw new BusinessException("noDeviceUsageTypeDefinedForCodeX", "No DeviceUsageType defined for code {0}", code);
        }
    }

    @Override // eg. for in Combo boxes
    public String toString() {
        return getLocalizedName();
    }
}
