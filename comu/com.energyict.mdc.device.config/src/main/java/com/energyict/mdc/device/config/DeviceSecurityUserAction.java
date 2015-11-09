package com.energyict.mdc.device.config;

import com.energyict.mdc.device.config.security.Privileges;

import java.util.Optional;

/**
 * Defines the Privileges that relate to device security properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-01-03 (11:32)
 */
public enum DeviceSecurityUserAction {

    VIEWDEVICESECURITYPROPERTIES1(Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1) {
        @Override
        public boolean isViewing () {
            return true;
        }
    },
    VIEWDEVICESECURITYPROPERTIES2(Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2) {
        @Override
        public boolean isViewing () {
            return true;
        }
    },
    VIEWDEVICESECURITYPROPERTIES3(Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3) {
        @Override
        public boolean isViewing () {
            return true;
        }
    },
    VIEWDEVICESECURITYPROPERTIES4(Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4) {
        @Override
        public boolean isViewing () {
            return true;
        }
    },
    EDITDEVICESECURITYPROPERTIES1(Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1) {
        @Override
        public boolean isEditing () {
            return true;
        }
    },
    EDITDEVICESECURITYPROPERTIES2(Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2) {
        @Override
        public boolean isEditing () {
            return true;
        }
    },
    EDITDEVICESECURITYPROPERTIES3(Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3) {
        @Override
        public boolean isEditing () {
            return true;
        }
    },
    EDITDEVICESECURITYPROPERTIES4(Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4) {
        @Override
        public boolean isEditing () {
            return true;
        }
    };

    public String getPrivilege() {
        return privilege;
    }

    private String privilege;

    DeviceSecurityUserAction (String privilege) {
        this.privilege = privilege;
    }

    public boolean isViewing () {
        return false;
    }

    public boolean isEditing () {
        return false;
    }

    public boolean isExecutable(){
        return false;
    }

    public int databaseIdentifier () {
        return this.ordinal();
    }

    public static Optional<DeviceSecurityUserAction> forPrivilege(String privilege) {
        for (DeviceSecurityUserAction userAction : values()) {
            if (userAction.getPrivilege().equals(privilege)) {
                return Optional.of(userAction);
            }
        }
        return Optional.empty();
    }
}