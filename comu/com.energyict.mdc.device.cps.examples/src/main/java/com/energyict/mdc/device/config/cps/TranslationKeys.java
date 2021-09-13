/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    DOMAIN_NAME_DEVICE("domain.name.device", "Device"),
    DOMAIN_NAME_CHANNEL("domain.name.channel", "Channel"),
    DOMAIN_NAME_REGISTER("domain.name.register", "Register"),
    DOMAIN_NAME_USAGEPOINT("domain.name.usagePoint", "Usage point"),
    EMI_NAME(DeviceEMeterInfoCustomPropertySet.PREFIX + ".name", "End device E-meter info"),
    EMI_PROPERTY_MANUFACTURER(DeviceEMeterInfoCustomPropertySet.PREFIX + ".manufacturer", "Manufacturer"),
    EMI_PROPERTY_MODEL_NUMBER(DeviceEMeterInfoCustomPropertySet.PREFIX + ".modelNumber", "Model number"),
    EMI_PROPERTY_CONFIG_SCHEME(DeviceEMeterInfoCustomPropertySet.PREFIX + ".configScheme", "Config scheme"),
    EMI_PROPERTY_SERVICE_COMPANY(DeviceEMeterInfoCustomPropertySet.PREFIX + ".serviceCompany", "Service company"),
    EMI_PROPERTY_TECHNICIAN(DeviceEMeterInfoCustomPropertySet.PREFIX + ".technician", "Technician"),
    EMI_PROPERTY_REPLACE_BY(DeviceEMeterInfoCustomPropertySet.PREFIX + ".replaceBy", "Replace by"),
    EMI_PROPERTY_MAX_CURRENT_RATING(DeviceEMeterInfoCustomPropertySet.PREFIX + ".maxCurrentRating", "Max current rating"),
    EMI_PROPERTY_MAX_VOLTAGE(DeviceEMeterInfoCustomPropertySet.PREFIX + ".maxVoltage", "Max voltage"),

    DMI_NAME(DeviceTypeManufacturerInfoCustomPropertySet.PREFIX + ".name", "Manufacturer-info"),
    DMI_DOMAIN_NAME(DeviceTypeManufacturerInfoCustomPropertySet.PREFIX + ".domain.name", "Device type"),
    DMI_MANUFACTURER_NAME(DeviceTypeManufacturerInfoCustomPropertySet.PREFIX + ".manufacturername", "Manufacturer Name"),
    DMI_MANUFACTURER_ID(DeviceTypeManufacturerInfoCustomPropertySet.PREFIX + ".manufacturerid", "Manufacturer Id");

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
