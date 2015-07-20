package com.elster.jupiter.cps;

import java.util.Optional;

/**
 * Defines the privileges that allow a user to edit
 * custom property values provided by {@link CustomPropertySet}s
 * that are registered with the custom properties bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-20 (15:51)
 */
public enum EditPrivilege {
    LEVEL_1(Privileges.EDIT_CUSTOM_PROPERTIES_1),
    LEVEL_2(Privileges.EDIT_CUSTOM_PROPERTIES_2),
    LEVEL_3(Privileges.EDIT_CUSTOM_PROPERTIES_3),
    LEVEL_4(Privileges.EDIT_CUSTOM_PROPERTIES_4);

    public String getPrivilege() {
        return privilege;
    }

    private String privilege;

    EditPrivilege(String privilege) {
        this.privilege = privilege;
    }

    public static Optional<EditPrivilege> forPrivilege(String privilege) {
        for (EditPrivilege editPrivilege : values()) {
            if (editPrivilege.getPrivilege().equals(privilege)) {
                return Optional.of(editPrivilege);
            }
        }
        return Optional.empty();
    }

}