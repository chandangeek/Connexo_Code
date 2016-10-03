package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    EMI_NAME(DeviceEMeterInfoCustomPropertySet.PREFIX + ".name", "E-Meter info"),
    EMI_PROPERTY_MANUFACTURER(DeviceEMeterInfoCustomPropertySet.PREFIX + ".manufacturer", "Manufacturer"),
    EMI_PROPERTY_MODEL_NUMBER(DeviceEMeterInfoCustomPropertySet.PREFIX + ".modelNumber", "Model Number"),
    EMI_PROPERTY_CONFIG_SCHEME(DeviceEMeterInfoCustomPropertySet.PREFIX + ".configScheme", "Config Scheme"),
    EMI_PROPERTY_SERVICE_COMPANY(DeviceEMeterInfoCustomPropertySet.PREFIX + ".serviceCompany", "Service Company"),
    EMI_PROPERTY_TECHNICIAN(DeviceEMeterInfoCustomPropertySet.PREFIX + ".technician", "Technician"),
    EMI_PROPERTY_REPLACE_BY(DeviceEMeterInfoCustomPropertySet.PREFIX + ".replaceBy", "Replace By"),
    EMI_PROPERTY_MAX_CURRENT_RATING(DeviceEMeterInfoCustomPropertySet.PREFIX + ".maxCurrentRating", "Max Current Rating"),
    EMI_PROPERTY_MAX_VOLTAGE(DeviceEMeterInfoCustomPropertySet.PREFIX + ".maxVoltage", "Max Voltage"),

    SDI_NAME(DeviceSAPInfoCustomPropertySet.PREFIX + ".name", "Device SAP info"),
    SDI_PROPERTY_USAGE_TYPE(DeviceSAPInfoCustomPropertySet.PREFIX + ".usageType", "Usage Type"),
    SDI_PROPERTY_IN_USE(DeviceSAPInfoCustomPropertySet.PREFIX + ".inUse", "In Use"),

    SCI_NAME(ChannelSAPInfoCustomPropertySet.PREFIX + ".name", "Channel SAP info"),
    SCI_PROPERTY_LOGICAL_REGISTER_NUMBER(ChannelSAPInfoCustomPropertySet.PREFIX + ".logicalRegisterNumber", "Logical Register Number"),
    SCI_PROPERTY_PROFILE_NUMBER(ChannelSAPInfoCustomPropertySet.PREFIX + ".profileNumber", "Profile Number"),
    SCI_PROPERTY_IN_USE(ChannelSAPInfoCustomPropertySet.PREFIX + ".inUse", "In Use"),
    SCI_PROPERTY_BILLING_FACTOR(ChannelSAPInfoCustomPropertySet.PREFIX + ".billingFactor", "Billing Factor"),;

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
