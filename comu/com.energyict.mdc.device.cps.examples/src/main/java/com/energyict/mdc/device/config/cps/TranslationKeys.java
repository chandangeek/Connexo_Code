/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    DOMAIN_NAME_DEVICE("domain.name.device", "Device"),
    DOMAIN_NAME_CHANNEL("domain.name.device", "Channel"),
    EMI_NAME(DeviceEMeterInfoCustomPropertySet.PREFIX + ".name", "End device E-meter info"),
    EMI_PROPERTY_MANUFACTURER(DeviceEMeterInfoCustomPropertySet.PREFIX + ".manufacturer", "Manufacturer"),
    EMI_PROPERTY_MODEL_NUMBER(DeviceEMeterInfoCustomPropertySet.PREFIX + ".modelNumber", "Model number"),
    EMI_PROPERTY_CONFIG_SCHEME(DeviceEMeterInfoCustomPropertySet.PREFIX + ".configScheme", "Config scheme"),
    EMI_PROPERTY_SERVICE_COMPANY(DeviceEMeterInfoCustomPropertySet.PREFIX + ".serviceCompany", "Service company"),
    EMI_PROPERTY_TECHNICIAN(DeviceEMeterInfoCustomPropertySet.PREFIX + ".technician", "Technician"),
    EMI_PROPERTY_REPLACE_BY(DeviceEMeterInfoCustomPropertySet.PREFIX + ".replaceBy", "Replace by"),
    EMI_PROPERTY_MAX_CURRENT_RATING(DeviceEMeterInfoCustomPropertySet.PREFIX + ".maxCurrentRating", "Max current rating"),
    EMI_PROPERTY_MAX_VOLTAGE(DeviceEMeterInfoCustomPropertySet.PREFIX + ".maxVoltage", "Max voltage"),

    SDI_NAME(DeviceSAPInfoCustomPropertySet.PREFIX + ".name", "End device SAP info"),
    SDI_PROPERTY_USAGE_TYPE(DeviceSAPInfoCustomPropertySet.PREFIX + ".usageType", "Usage type"),
    SDI_PROPERTY_IN_USE(DeviceSAPInfoCustomPropertySet.PREFIX + ".inUse", "In use"),

    SCI_NAME(ChannelSAPInfoCustomPropertySet.PREFIX + ".name", "End device channel SAP info"),
    SCI_PROPERTY_LOGICAL_REGISTER_NUMBER(ChannelSAPInfoCustomPropertySet.PREFIX + ".logicalRegisterNumber", "Logical register number"),
    SCI_PROPERTY_PROFILE_NUMBER(ChannelSAPInfoCustomPropertySet.PREFIX + ".profileNumber", "Profile number"),
    SCI_PROPERTY_IN_USE(ChannelSAPInfoCustomPropertySet.PREFIX + ".inUse", "In use"),
    SCI_PROPERTY_BILLING_FACTOR(ChannelSAPInfoCustomPropertySet.PREFIX + ".billingFactor", "Billing factor"),;

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
