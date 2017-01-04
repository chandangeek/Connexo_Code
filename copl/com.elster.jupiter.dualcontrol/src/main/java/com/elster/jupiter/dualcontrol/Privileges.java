package com.elster.jupiter.dualcontrol;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_DUAL_CONTROL_CALENDARS("dualcontrol", "Dual control"),
    RESOURCE_DUAL_CONTROL_DESCRIPTION("dualcontrol.description", "Manage dual control"),

    //Privileges
    GRANT_DUAL_CONTROL_APPROVAL(Constants.GRANT_APPROVAL, "Grant privileges to approve dual controlled entities.")
    ;

    private final String key;
    private final String description;

    Privileges(String key, String description) {
        this.key = key;
        this.description = description;
    }

    @Override
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
        String GRANT_APPROVAL = "dualcontrol.grant.approval";
    }
}
