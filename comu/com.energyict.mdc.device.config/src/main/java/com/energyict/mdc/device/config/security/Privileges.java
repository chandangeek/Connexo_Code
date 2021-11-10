/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.security;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.common.device.config.DeviceConfigConstants;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_MASTER_DATA("masterData.masterData", "Device master data"),
    RESOURCE_MASTER_DATA_DESCRIPTION("masterData.masterData.description", "Manage device master data"),
    RESOURCE_DEVICE_TYPES("deviceType.deviceTypes", "Device types"),
    RESOURCE_DEVICE_TYPES_DESCRIPTION("deviceType.deviceTypes.description", "Manage device types"),
    RESOURCE_DEVICE_COMMANDS("deviceCommand.deviceCommands", "Device commands"),
    RESOURCE_DEVICE_COMMANDS_DESCRIPTION("deviceCommand.deviceCommands.description", "Manage device commands"),

    //Privileges
    ADMINISTRATE_MASTER_DATA(DeviceConfigConstants.ADMINISTRATE_MASTER_DATA, "Administrate"),
    VIEW_MASTER_DATA(DeviceConfigConstants.VIEW_MASTER_DATA, "View"),
    ADMINISTRATE_DEVICE_TYPE(DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE, "Administrate"),
    VIEW_DEVICE_TYPE(DeviceConfigConstants.VIEW_DEVICE_TYPE, "View"),
    EXECUTE_COM_TASK_1(DeviceConfigConstants.EXECUTE_COM_TASK_1, "Execute level 1"),
    EXECUTE_COM_TASK_2(DeviceConfigConstants.EXECUTE_COM_TASK_2, "Execute level 2"),
    EXECUTE_COM_TASK_3(DeviceConfigConstants.EXECUTE_COM_TASK_3, "Execute level 3"),
    EXECUTE_COM_TASK_4(DeviceConfigConstants.EXECUTE_COM_TASK_4, "Execute level 4"),
    EXECUTE_DEVICE_MESSAGE_1(DeviceConfigConstants.EXECUTE_DEVICE_MESSAGE_1, "Execute level 1"),
    EXECUTE_DEVICE_MESSAGE_2(DeviceConfigConstants.EXECUTE_DEVICE_MESSAGE_2, "Execute level 2"),
    EXECUTE_DEVICE_MESSAGE_3(DeviceConfigConstants.EXECUTE_DEVICE_MESSAGE_3, "Execute level 3"),
    EXECUTE_DEVICE_MESSAGE_4(DeviceConfigConstants.EXECUTE_DEVICE_MESSAGE_4, "Execute level 4");


    private final String key;
    private final String description;

    Privileges(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return getDescription();
    }

    public String getDescription() {
        return description;
    }

    public static String[] keys() {
        return Arrays.stream(Privileges.values())
                .map(Privileges::getKey)
                .collect(Collectors.toList())
                .toArray(new String[Privileges.values().length]);
    }

    public interface Constants {
        String ADMINISTRATE_MASTER_DATA = "privilege.administrate.masterData";
        String VIEW_MASTER_DATA = "privilege.view.masterData";

        String ADMINISTRATE_DEVICE_TYPE = "privilege.administrate.deviceType";
        String VIEW_DEVICE_TYPE = "privilege.view.deviceType";

        String EXECUTE_COM_TASK_1 = "execute.com.task.level1";
        String EXECUTE_COM_TASK_2 = "execute.com.task.level2";
        String EXECUTE_COM_TASK_3 = "execute.com.task.level3";
        String EXECUTE_COM_TASK_4 = "execute.com.task.level4";

        String EXECUTE_DEVICE_MESSAGE_1 = "execute.device.message.level1";
        String EXECUTE_DEVICE_MESSAGE_2 = "execute.device.message.level2";
        String EXECUTE_DEVICE_MESSAGE_3 = "execute.device.message.level3";
        String EXECUTE_DEVICE_MESSAGE_4 = "execute.device.message.level4";
    }
}
