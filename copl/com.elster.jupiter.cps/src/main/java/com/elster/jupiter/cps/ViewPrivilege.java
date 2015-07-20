package com.elster.jupiter.cps;

import java.util.Optional;

/**
 * Defines the privileges that allow a user to view
 * custom property values provided by {@link CustomPropertySet}s
 * that are registered with the custom properties bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-20 (15:51)
 */
public enum ViewPrivilege {
    LEVEL_1(Privileges.VIEW_CUSTOM_PROPERTIES_1),
    LEVEL_2(Privileges.VIEW_CUSTOM_PROPERTIES_2),
    LEVEL_3(Privileges.VIEW_CUSTOM_PROPERTIES_3),
    LEVEL_4(Privileges.VIEW_CUSTOM_PROPERTIES_4);

    public String getPrivilege() {
        return privilege;
    }

    private String privilege;

    ViewPrivilege (String privilege) {
        this.privilege = privilege;
    }

    public static Optional<ViewPrivilege> forPrivilege(String privilege) {
        for (ViewPrivilege viewPrivilege : values()) {
            if (viewPrivilege.getPrivilege().equals(privilege)) {
                return Optional.of(viewPrivilege);
            }
        }
        return Optional.empty();
    }

}