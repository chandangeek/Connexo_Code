/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki;

import com.elster.jupiter.pki.security.Privileges;

import java.util.Optional;

/**
 * Defines the Privileges that relate to properties of security accessors.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-01-03 (11:32)
 */
public enum SecurityAccessorUserAction {

    VIEW_SECURITY_PROPERTIES_1(Privileges.Constants.VIEW_SECURITY_PROPERTIES_1) {
        @Override
        public boolean isViewing() {
            return true;
        }
    },
    VIEW_SECURITY_PROPERTIES_2(Privileges.Constants.VIEW_SECURITY_PROPERTIES_2) {
        @Override
        public boolean isViewing() {
            return true;
        }
    },
    VIEW_SECURITY_PROPERTIES_3(Privileges.Constants.VIEW_SECURITY_PROPERTIES_3) {
        @Override
        public boolean isViewing() {
            return true;
        }
    },
    VIEW_SECURITY_PROPERTIES_4(Privileges.Constants.VIEW_SECURITY_PROPERTIES_4) {
        @Override
        public boolean isViewing() {
            return true;
        }
    },
    EDIT_SECURITY_PROPERTIES_1(Privileges.Constants.EDIT_SECURITY_PROPERTIES_1) {
        @Override
        public boolean isEditing() {
            return true;
        }
    },
    EDIT_SECURITY_PROPERTIES_2(Privileges.Constants.EDIT_SECURITY_PROPERTIES_2) {
        @Override
        public boolean isEditing() {
            return true;
        }
    },
    EDIT_SECURITY_PROPERTIES_3(Privileges.Constants.EDIT_SECURITY_PROPERTIES_3) {
        @Override
        public boolean isEditing() {
            return true;
        }
    },
    EDIT_SECURITY_PROPERTIES_4(Privileges.Constants.EDIT_SECURITY_PROPERTIES_4) {
        @Override
        public boolean isEditing() {
            return true;
        }
    };

    private String privilege;

    SecurityAccessorUserAction(String privilege) {
        this.privilege = privilege;
    }

    public String getPrivilege() {
        return privilege;
    }

    public boolean isViewing() {
        return false;
    }

    public boolean isEditing() {
        return false;
    }

    public boolean isExecutable() {
        return false;
    }

    public static Optional<SecurityAccessorUserAction> forPrivilege(String privilege) {
        for (SecurityAccessorUserAction userAction : values()) {
            if (userAction.getPrivilege().equals(privilege)) {
                return Optional.of(userAction);
            }
        }
        return Optional.empty();
    }
}
