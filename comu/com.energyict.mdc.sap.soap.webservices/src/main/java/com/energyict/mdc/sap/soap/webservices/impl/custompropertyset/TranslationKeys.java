/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    DOMAIN_NAME_DEVICE("domain.name.device", "Device"),
    DOMAIN_NAME_CHANNEL("domain.name.channel", "Channel"),
    DOMAIN_NAME_REGISTER("domain.name.register", "Register"),
    CPS_DEVICE_SAP_INFO("device.cps.properties.devicesapinfo", "Device SAP info"),
    CPS_DEVICE_CHANNEL_SAP_INFO("device.cps.properties.devicechannelsapinfo", "Device channel SAP info"),
    CPS_DEVICE_REGISTER_SAP_INFO("device.cps.properties.deviceregistersapinfo", "Device register SAP info"),
    CPS_DEVICE_IDENTIFIER("device.cps.properties.deviceIdentifier", "Device identifier"),
    CPS_DEVICE_IDENTIFIER_DESCRIPTION("device.cps.properties.deviceIdentifier.description", "Device identifier in SAP."),
    CPS_LOGICAL_REGISTER_NUMBER("device.cps.properties.logicalNumberIdentifier", "Logical register number"),
    CPS_DEVICE_CHANNEL_IDENTIFIER_DESCRIPTION("device.cps.properties.deviceChannelIdentifier.description", "Device channel identifier in SAP."),
    CPS_DEVICE_CHANNEL_PROFILE_IDENTIFIER_DESCRIPTION("device.cps.properties.deviceChannelProfileIdentifier.description", "Device channel profile identifier in SAP."),
    CPS_DEVICE_REGISTER_IDENTIFIER_DESCRIPTION("device.cps.properties.deviceRegisterIdentifier.description", "Device register identifier in SAP."),
    CPS_DEVICE_LOCATION("device.cps.properties.deviceLocationIdentifier", "Device location"),
    CPS_DEVICE_LOCATION_DESCRIPTION("device.cps.properties.deviceLocationIdentifier.description", "Device location in SAP"),
    CPS_DEVICE_LOCATION_INFORMATION("device.cps.properties.deviceLocationIdentifierInformation", "Device location information"),
    CPS_DEVICE_LOCATION_INFORMATION_DESCRIPTION("device.cps.properties.deviceLocationIdentifierInformation.description", "Device location information in SAP"),
    CPS_MODIFICATION_INFORMATION("device.cps.properties.modificationInformation", "Modification information"),
    CPS_MODIFICATION_INFORMATION_DESCRIPTION("device.cps.properties.modificationInformation.description", "Modification information in SAP"),
    CPS_INSTALLATION_NUMBER("device.cps.properties.installationNumber", "Installation number"),
    CPS_INSTALLATION_NUMBER_DESCRIPTION("device.cps.properties.installationNumber.description", "Installation number in SAP"),
    CPS_POINT_OF_DELIVERY("device.cps.properties.pod", "Point of delivery identifier"),
    CPS_POINT_OF_DELIVERY_DESCRIPTION("device.cps.properties.pod.description", "Point of delivery identifier in SAP"),
    CPS_DIVISION_CATEGORY_CODE("device.cps.properties.divisionCategoryCode", "Division category code"),
    CPS_DIVISION_CATEGORY_CODE_DESCRIPTION("device.cps.properties.divisionCategoryCode.description", "Division category code in SAP"),
    CPS_DEVICE_CHANNEL_SAP_PROFILE_INFO("device.cps.properties.devicechannelsapprofileinfo", "Device channel SAP profile info"),
    CPS_PROFILE_ID("device.cps.properties.profileId", "Profile id"),
    REGISTERED("device.cps.properties.registered", "Registered"),
    CPS_ACTIVATION_GROUP_AMI_FUNCTIONS("device.cps.properties.activationgroupamifunctions", "Activation Group AMI Functions"),
    CPS_ACTIVATION_GROUP_AMI_FUNCTIONS_DESCRIPTION("device.cps.properties.activationgroupamifunctions.description", "Activation Group AMI Functions in SAP"),
    CPS_METER_FUNCTION_GROUP("device.cps.properties.meterfunctiongroup", "Meter function group"),
    CPS_METER_FUNCTION_GROUP_DESCRIPTION("device.cps.properties.meterfunctiongroup.description", "Meter function group in SAP"),
    CPS_ATTRIBUTE_MESSAGE("device.cps.properties.attributemessage", "Attribute message"),
    CPS_ATTRIBUTE_MESSAGE_DESCRIPTION("device.cps.properties.attributemessage.description", "Attribute message in SAP"),
    CPS_CHARACTERISTICS_ID("device.cps.properties.characteristicsid", "Characteristics id"),
    CPS_CHARACTERISTICS_ID_DESCRIPTION("device.cps.properties.characteristicsid.description", "Characteristics id in SAP"),
    CPS_CHARACTERISTICS_VALUE("device.cps.properties.characteristicsvalue", "Characteristics value"),
    CPS_CHARACTERISTICS_VALUE_DESCRIPTION("device.cps.properties.characteristicsvalue.description", "Characteristics value in SAP"),

    ;

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