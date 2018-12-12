/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by albertv on 12/9/2016.
 */
public enum Privileges implements TranslationKey {

    //Resources
    RESOURCE_ALARMS("device.alarms", "Alarms"),
    RESOURCE_ALARMS_DESCRIPTION("device.alarms.description", "Manage alarms"),
    RESOURCE_ALARMS_CONFIGURATION("alarmConfiguration.alarmConfigurations", "Alarms configuration"),
    RESOURCE_ALARMS_CONFIGURATION_DESCRIPTION("alarmConfiguration.alarmConfigurations.description", "Manage alarms configuration"),

    //Privileges
    VIEW_ALARM(Constants.VIEW_ALARM, "View"),
    COMMENT_ALARM(Constants.COMMENT_ALARM, "Comment"),
    CLOSE_ALARM(Constants.CLOSE_ALARM, "Close"),
    ASSIGN_ALARM(Constants.ASSIGN_ALARM, "Assign"),
    ACTION_ALARM(Constants.ACTION_ALARM, "Action"),
    VIEW_ALARM_CREATION_RULE(Constants.VIEW_ALARM_CREATION_RULE, "View creation rules"),
    ADMINISTRATE_ALARM_CREATION_RULE(Constants.ADMINISTRATE_ALARM_CREATION_RULE, "Administrate creation rules"),
    VIEW_ALARM_ASSIGNMENT_RULE(Constants.VIEW_ALARM_ASSIGNMENT_RULE, "View assignment rules");


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
        String VIEW_ALARM = "privilege.view.alarm";
        String COMMENT_ALARM = "privilege.comment.alarm";
        String CLOSE_ALARM = "privilege.close.alarm";
        String ASSIGN_ALARM = "privilege.assign.alarm";
        String ACTION_ALARM = "privilege.action.alarm";
        String VIEW_ALARM_CREATION_RULE = "privilege.view.alarm.creationRule";
        String ADMINISTRATE_ALARM_CREATION_RULE= "privilege.administrate.alarm.creationRule";
        String VIEW_ALARM_ASSIGNMENT_RULE = "privilege.view.alarm.assignmentRule";
    }
}
