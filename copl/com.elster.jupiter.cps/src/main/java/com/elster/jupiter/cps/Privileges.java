package com.elster.jupiter.cps;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Defines the privileges of the custom properties bundles.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-20 (15:45)
 */
@ProviderType
public enum Privileges implements TranslationKey {

    ADMINISTER_PRIVILEGES(Constants.ADMINISTER_PRIVILEGES, "Administrate"),
    VIEW_PRIVILEGES(Constants.VIEW_PRIVILEGES, "View"),

    VIEW_CUSTOM_PROPERTIES_1(Constants.VIEW_CUSTOM_PROPERTIES_1, "View level 1"),
    VIEW_CUSTOM_PROPERTIES_2(Constants.VIEW_CUSTOM_PROPERTIES_2, "View level 2"),
    VIEW_CUSTOM_PROPERTIES_3(Constants.VIEW_CUSTOM_PROPERTIES_3, "View level 3"),
    VIEW_CUSTOM_PROPERTIES_4(Constants.VIEW_CUSTOM_PROPERTIES_4, "View level 4"),

    EDIT_CUSTOM_PROPERTIES_1(Constants.EDIT_CUSTOM_PROPERTIES_1, "EDIT level 1"),
    EDIT_CUSTOM_PROPERTIES_2(Constants.EDIT_CUSTOM_PROPERTIES_2, "EDIT level 2"),
    EDIT_CUSTOM_PROPERTIES_3(Constants.EDIT_CUSTOM_PROPERTIES_3, "EDIT level 3"),
    EDIT_CUSTOM_PROPERTIES_4(Constants.EDIT_CUSTOM_PROPERTIES_4, "EDIT level 4");

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
        String ADMINISTER_PRIVILEGES = "cps.privilege.administer.privileges";
        String VIEW_PRIVILEGES = "cps.privilege.view.privileges";

        String VIEW_CUSTOM_PROPERTIES_1 = "view.custom.properties.level1";
        String VIEW_CUSTOM_PROPERTIES_2 = "view.custom.properties.level2";
        String VIEW_CUSTOM_PROPERTIES_3 = "view.custom.properties.level3";
        String VIEW_CUSTOM_PROPERTIES_4 = "view.custom.properties.level4";

        String EDIT_CUSTOM_PROPERTIES_1 = "edit.custom.properties.level1";
        String EDIT_CUSTOM_PROPERTIES_2 = "edit.custom.properties.level2";
        String EDIT_CUSTOM_PROPERTIES_3 = "edit.custom.properties.level3";
        String EDIT_CUSTOM_PROPERTIES_4 = "edit.custom.properties.level4";
    }

}