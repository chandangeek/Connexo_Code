/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config;


import com.elster.jupiter.nls.TranslationKey;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_USAGE_POINT_LIFECYCLE("resource.usagePoint.lifecycle", "Usage point life cycle"),
    RESOURCE_USAGE_POINT_LIFECYCLE_DESCRIPTION("resource.usagePoint.lifecycle.description", "Manage usage point life cycle"),
    RESOURCE_USAGE_POINT_LIFECYCLE_LEVELS("resource.usagePoint.lifecycle.access.levels", "Usage point life cycle access levels"),
    RESOURCE_USAGE_POINT_LIFECYCLE_LEVELS_DESCRIPTION("resource.usagePoint.lifecycle.access.levels.description", "Manage usage point life cycle access levels"),

    //Privileges
    USAGE_POINT_LIFE_CYCLE_VIEW(Constants.USAGE_POINT_LIFE_CYCLE_VIEW, "View"),
    USAGE_POINT_LIFE_CYCLE_ADMINISTER(Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER, "Administrate"),
    EXECUTE_TRANSITION_1(Constants.EXECUTE_TRANSITION_1, "Execute transition level 1"),
    EXECUTE_TRANSITION_2(Constants.EXECUTE_TRANSITION_2, "Execute transition level 2"),
    EXECUTE_TRANSITION_3(Constants.EXECUTE_TRANSITION_3, "Execute transition level 3"),
    EXECUTE_TRANSITION_4(Constants.EXECUTE_TRANSITION_4, "Execute transition level 4");

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

    public interface Constants {
        String USAGE_POINT_LIFE_CYCLE_VIEW = "privilege.usagePoint.lifecycle.view";
        String USAGE_POINT_LIFE_CYCLE_ADMINISTER = "privilege.usagePoint.lifecycle.administer";
        String EXECUTE_TRANSITION_1 = "usagePoint.lifecycle.execute.transition.level1";
        String EXECUTE_TRANSITION_2 = "usagePoint.lifecycle.execute.transition.level2";
        String EXECUTE_TRANSITION_3 = "usagePoint.lifecycle.execute.transition.level3";
        String EXECUTE_TRANSITION_4 = "usagePoint.lifecycle.execute.transition.level4";
    }
}