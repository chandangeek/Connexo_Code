/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;

public enum Privileges implements TranslationKey {
    //Resources
    COMMAND_LIMITATION_RULES("commandrule.commandLimitationRules", "Command limitation rules"),
    COMMAND_LIMITATION_RULES_DESCRIPTION("commandrule.commandLimitationRules.description", "Manage command limitation rules"),

    //Privileges
    ADMINISTRATE_LIMITATION_RULES(Constants.ADMINISTRATE_COMMAND_LIMITATION_RULE, "Administrate"),
    VIEW_COMMAND_LIMITATION_RULES(Constants.VIEW_COMMAND_LIMITATION_RULE, "View"),
    APPROVE_COMMAND_LIMITATION_RULES(Constants.APPROVE_COMMAND_LIMITATION_RULE, "Approve/reject command limitation rules")
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
        String ADMINISTRATE_COMMAND_LIMITATION_RULE = "privilege.administrate.commandLimitationRule";
        String VIEW_COMMAND_LIMITATION_RULE = "privilege.view.commandLimitationRule";
        String APPROVE_COMMAND_LIMITATION_RULE = "privilege.approve.commandLimitationRule";

    }
}
