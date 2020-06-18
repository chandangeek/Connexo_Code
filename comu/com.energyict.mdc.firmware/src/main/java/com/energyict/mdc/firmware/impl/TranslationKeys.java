/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.common.device.config.ConnectionStrategy;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-26 (11:34)
 */
public enum TranslationKeys implements TranslationKey {
    DOMAIN_NAME("serviceCall", "Service call"),
    NAME_OF_CAMPAIGN("name", "Name"),
    DEVICE_TYPE("deviceType", "Device type"),
    DEVICE_GROUP("deviceGroup", "Device group"),
    UPDATE_START("updateStart", "Update time start"),
    UPDATE_END("updateEnd", "Update time end"),
    ACTIVATION_OPTION("activationOption", "Activation option"),
    ACTIVATION_DATE("activationDate", "Activation date"),
    VALIDATION_TIMEOUT("validationTimeout", "Timeout before validation"),
    DEVICE("device", "Device"),
    ON_DATE("onDate", "On date"),
    WITHOUT_ACTIVATION("withoutActivation", "Without activation"),
    DEVICE_MESSAGE_ID("deviceMessageId", "Device message id"),
    FIRMWARE_CAMPAIGN_CPS("FirmwareCampaignCustomPropertySet", "Firmware campaign custom property set"),
    FIRMWARE_CAMPAIGN_ITEM_CPS("FirmwareCampaignItemCustomPropertySet", "Firmware campaign item custom property set"),
    VALIDATION_COMTASK_ID("validationComTaskId", "Validation communication task id"),
    FIRMWARE_UPLOAD_COMTASK_ID("firmwareUploadComTaskId", "Firmware upload communication task id"),
    VALIDATION_CONNECTIONSTRATEGY("validationConnectionStrategy", "Validation connection strategy"),
    FIRMWARE_UPLOAD_CONNECTIONSTRATEGY("firmwareUploadConnectionStrategy", "Firmware upload connection strategy"),
    MINIMIZE_CONNECTIONS(ConnectionStrategy.MINIMIZE_CONNECTIONS.name(), "Minimize connections"),
    AS_SOON_AS_POSSIBLE(ConnectionStrategy.AS_SOON_AS_POSSIBLE.name(), "As soon as possible"),
    MANUALLY_CANCELLED("manuallyCancelled", "Manually cancelled"),
    WITH_UNIQUE_FIRMWARE_VERSION("withUniqueFirmwareVersion", "With unique firmware version"),
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