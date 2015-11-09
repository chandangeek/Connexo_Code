package com.energyict.mdc.device.config;

import com.energyict.mdc.device.config.security.Privileges;
import java.util.Optional;

/**
 * Defines the Privileges that relate to device message executions.
 */
public enum DeviceMessageUserAction {

    EXECUTEDEVICEMESSAGE1(Privileges.Constants.EXECUTE_DEVICE_MESSAGE_1),
    EXECUTEDEVICEMESSAGE2(Privileges.Constants.EXECUTE_DEVICE_MESSAGE_2),
    EXECUTEDEVICEMESSAGE3(Privileges.Constants.EXECUTE_DEVICE_MESSAGE_3),
    EXECUTEDEVICEMESSAGE4(Privileges.Constants.EXECUTE_DEVICE_MESSAGE_4),
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
