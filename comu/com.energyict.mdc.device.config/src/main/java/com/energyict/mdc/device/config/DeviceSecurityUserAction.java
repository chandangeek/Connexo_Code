package com.energyict.mdc.device.config;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.UserAction;

import com.google.common.base.Optional;

/**
 * Defines the {@link UserAction}s that relate to device security properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-01-03 (11:32)
 */
public enum DeviceSecurityUserAction {

    VIEWDEVICESECURITYPROPERTIES1(UserAction.VIEWDEVICESECURITYPROPERTIES1) {
        @Override
        public boolean isViewing () {
            return true;
        }
    },
    VIEWDEVICESECURITYPROPERTIES2(UserAction.VIEWDEVICESECURITYPROPERTIES2) {
        @Override
        public boolean isViewing () {
            return true;
        }
    },
    VIEWDEVICESECURITYPROPERTIES3(UserAction.VIEWDEVICESECURITYPROPERTIES3) {
        @Override
        public boolean isViewing () {
            return true;
        }
    },
    VIEWDEVICESECURITYPROPERTIES4(UserAction.VIEWDEVICESECURITYPROPERTIES4) {
        @Override
        public boolean isViewing () {
            return true;
        }
    },
    EDITDEVICESECURITYPROPERTIES1(UserAction.EDITDEVICESECURITYPROPERTIES1) {
        @Override
        public boolean isEditing () {
            return true;
        }
    },
    EDITDEVICESECURITYPROPERTIES2(UserAction.EDITDEVICESECURITYPROPERTIES2) {
        @Override
        public boolean isEditing () {
            return true;
        }
    },
    EDITDEVICESECURITYPROPERTIES3(UserAction.EDITDEVICESECURITYPROPERTIES3) {
        @Override
        public boolean isEditing () {
            return true;
        }
    },
    EDITDEVICESECURITYPROPERTIES4(UserAction.EDITDEVICESECURITYPROPERTIES4) {
        @Override
        public boolean isEditing () {
            return true;
        }
    },
    ALLOWCOMTASKEXECUTION1(UserAction.EXECUTECOMTASK1){
        @Override
        public boolean isExecutable() {
            return true;
        }
    },
    ALLOWCOMTASKEXECUTION2(UserAction.EXECUTECOMTASK2){
        @Override
        public boolean isExecutable() {
            return true;
        }
    },
    ALLOWCOMTASKEXECUTION3(UserAction.EXECUTECOMTASK3){
        @Override
        public boolean isExecutable() {
            return true;
        }
    },
    ALLOWCOMTASKEXECUTION4(UserAction.EXECUTECOMTASK4){
        @Override
        public boolean isExecutable() {
            return true;
        }
    };

    private UserAction userAction;

    DeviceSecurityUserAction (UserAction userAction) {
        this.userAction = userAction;
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

    public static DeviceSecurityUserAction fromDb (int databaseIdentifier) {
        for (DeviceSecurityUserAction deviceSecurityUserAction : values()) {
            if (databaseIdentifier == deviceSecurityUserAction.databaseIdentifier()) {
                return deviceSecurityUserAction;
            }
        }
        throw new ApplicationException("Unknown or unsupported DeviceSecurityUserAction " + databaseIdentifier);
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