/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.common.device.config.ConnectionStrategy;

public enum TranslationKeys implements TranslationKey {
    STATUS_COMPLETED("completed", "Completed"),
    STATUS_ONGOIND("firmwareManagementDeviceStatus.ongoing", "Ongoing"),
    STATUS_CANCELED("firmwareManagementDeviceStatus.cancelled", "Cancelled"),
    STATUS_CONFIGURATION_ERROR("configurationError", "Configuration error"),
    STATUS_FAILED("firmwareManagementDeviceStatus.failed", "Failed"),
    STATUS_SUCCESSFUL("firmwareManagementDeviceStatus.successful", "Successful"),
    STATUS_PENDING("firmwareManagementDeviceStatus.pending", "Pending"),
    FIRMWARE_COMTASK_NAME("firmwareComTaskName", "Firmware management"),
    MINIMIZE_CONNECTIONS(ConnectionStrategy.MINIMIZE_CONNECTIONS.name(), "Minimize connections"),
    AS_SOON_AS_POSSIBLE(ConnectionStrategy.AS_SOON_AS_POSSIBLE.name(), "As soon as possible"),
    FIRMWARE_FILE("firmware.file.label", "Firmware file"),
    FIRMWARE_IMAGE_IDENTIFIER("general.imageIdentifier", "Image identifier"),
    FIRMWARE_RESUME("FirmwareDeviceMessage.upgrade.resume", "Resume"),
    FIRMWARE_ACTION_CHECK_VERSION_NOW_TRANSLATION_KEY("FirmwareActionCheckVersionNow", "Check firmware version/image now"),
    FIRMWARE_ACTIVATION_DATE("device.firmware.history.ActivationDate", "Activation date"),
    FIRMWARE_COMMUNICATION_TASK_NAME("FirmwareTaskName", "Firmware communication task");

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
