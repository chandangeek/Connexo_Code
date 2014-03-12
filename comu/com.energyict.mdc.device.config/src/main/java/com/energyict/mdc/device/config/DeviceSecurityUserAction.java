package com.energyict.mdc.device.config;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.UserAction;

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

    public boolean viewingIsAuthorizedFor (Role role) {
        return this.isViewing() && this.isAuthorized(role);
    }

    public boolean editingIsAuthorizedFor (Role role) {
        return this.isEditing() && this.isAuthorized(role);
    }

    public boolean isExecutableIsAuthorizedFor(Role rol){
        return this.isExecutable() && this.isAuthorized(rol);
    }

    private boolean isAuthorized (Role role) {
        return true;
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

    public String getLocalizedName() {
        return Environment.DEFAULT.get().getTranslation(userAction.getDisplayNameKey());
    }
}