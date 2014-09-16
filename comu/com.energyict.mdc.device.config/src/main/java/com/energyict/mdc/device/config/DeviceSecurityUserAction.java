package com.energyict.mdc.device.config;

import com.energyict.mdc.common.UserAction;
import com.energyict.mdc.device.config.security.Privileges;
import com.google.common.base.Optional;

/**
 * Defines the {@link UserAction}s that relate to device security properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-01-03 (11:32)
 */
public enum DeviceSecurityUserAction {

    VIEWDEVICESECURITYPROPERTIES1(Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_1) {
        @Override
        public boolean isViewing () {
            return true;
        }
    },
    VIEWDEVICESECURITYPROPERTIES2(Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_2) {
        @Override
        public boolean isViewing () {
            return true;
        }
    },
    VIEWDEVICESECURITYPROPERTIES3(Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_3) {
        @Override
        public boolean isViewing () {
            return true;
        }
    },
    VIEWDEVICESECURITYPROPERTIES4(Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_4) {
        @Override
        public boolean isViewing () {
            return true;
        }
    },
    EDITDEVICESECURITYPROPERTIES1(Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_1) {
        @Override
        public boolean isEditing () {
            return true;
        }
    },
    EDITDEVICESECURITYPROPERTIES2(Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_2) {
        @Override
        public boolean isEditing () {
            return true;
        }
    },
    EDITDEVICESECURITYPROPERTIES3(Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_3) {
        @Override
        public boolean isEditing () {
            return true;
        }
    },
    EDITDEVICESECURITYPROPERTIES4(Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_4) {
        @Override
        public boolean isEditing () {
            return true;
        }
    },
    ALLOWCOMTASKEXECUTION1(Privileges.EXECUTE_COM_TASK_1){
        @Override
        public boolean isExecutable() {
            return true;
        }
    },
    ALLOWCOMTASKEXECUTION2(Privileges.EXECUTE_COM_TASK_2){
        @Override
        public boolean isExecutable() {
            return true;
        }
    },
    ALLOWCOMTASKEXECUTION3(Privileges.EXECUTE_COM_TASK_3){
        @Override
        public boolean isExecutable() {
            return true;
        }
    },
    ALLOWCOMTASKEXECUTION4(Privileges.EXECUTE_COM_TASK_4){
        @Override
        public boolean isExecutable() {
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

    public static Optional<DeviceSecurityUserAction> forName(String name) {
        for (DeviceSecurityUserAction userAction : values()) {
            if (userAction.name().equals(name)) {
                return Optional.of(userAction);
            }
        }
        return Optional.absent();
    }
}