/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import java.util.Optional;

/**
 * Defines the Privileges that relate to device message executions.
 */
public enum DeviceMessageUserAction {

    EXECUTEDEVICEMESSAGE1(DeviceConfigConstants.EXECUTE_DEVICE_MESSAGE_1),
    EXECUTEDEVICEMESSAGE2(DeviceConfigConstants.EXECUTE_DEVICE_MESSAGE_2),
    EXECUTEDEVICEMESSAGE3(DeviceConfigConstants.EXECUTE_DEVICE_MESSAGE_3),
    EXECUTEDEVICEMESSAGE4(DeviceConfigConstants.EXECUTE_DEVICE_MESSAGE_4),
    ;

    private final String privilege;

    DeviceMessageUserAction(String privilege) {
        this.privilege = privilege;
    }

    public String getPrivilege() {
        return privilege;
    }

    public int databaseIdentifier() {
        return this.ordinal();
    }

    public static Optional<DeviceMessageUserAction> forPrivilege(String privilege) {
        for (DeviceMessageUserAction userAction : values()) {
            if (userAction.getPrivilege().equals(privilege)) {
                return Optional.of(userAction);
            }
        }
        return Optional.empty();
    }
}
