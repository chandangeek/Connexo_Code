/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_USAGE_POINT_GROUPS("usagePointGroup.usagePointGroups", "Usage point groups"),
    RESOURCE_USAGE_POINT_GROUPS_DESCRIPTION("usagePointGroup.usagePointGroups.description", "Manage usage point groups"),

    //Privileges
    ADMINISTER_USAGE_POINT_GROUP(Constants.ADMINISTER_USAGE_POINT_GROUP, "Administrate"),
    ADMINISTER_USAGE_POINT_ENUMERATED_GROUP(Constants.ADMINISTER_USAGE_POINT_ENUMERATED_GROUP, "Administrate static usage point groups"),
    VIEW_USAGE_POINT_GROUP_DETAIL(Constants.VIEW_USAGE_POINT_GROUP_DETAIL, "View detail"),
    ADMINISTER_VALIDATION_CONFIGURATION(Constants.ADMINISTER_VALIDATION_CONFIGURATION, "Administrate validation configuration"),
    ADMINISTER_ESTIMATION_CONFIGURATION(Constants.ADMINISTER_ESTIMATION_CONFIGURATION, "Administrate estimation configuration")
    ;

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
                .toArray(String[]::new);
    }

    public interface Constants {
        String ADMINISTER_USAGE_POINT_GROUP = "privilege.administer.usagePointGroup";
        String ADMINISTER_USAGE_POINT_ENUMERATED_GROUP = "privilege.administer.usagePointEnumeratedGroup";
        String VIEW_USAGE_POINT_GROUP_DETAIL = "privilege.view.usagePointGroupDetail";

        String ADMINISTER_VALIDATION_CONFIGURATION = "privilege.administer.usagepoint.validationConfiguration";
        String ADMINISTER_ESTIMATION_CONFIGURATION = "privilege.administer.usagepoint.estimationConfiguration";
    }
}
