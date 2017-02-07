/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    //Resources
    RESOURCE_SERVICE_CALL_TYPES("serviceCallTypes.serviceCallTypes", "Service call types"),
    RESOURCE_SERVICE_CALL_TYPES_DESCRIPTION("serviceCallTypes.serviceCallTypes.description", "Manage service call types"),
    RESOURCE_SERVICE_CALL("serviceCalls.serviceCalls", "Service calls"),
    RESOURCE_SERVICE_CALL_DESCRIPTION("serviceCalls.serviceCalls.description", "Manage service calls"),



    //Privileges
    VIEW_SERVICE_CALL_TYPES(Constants.VIEW_SERVICE_CALL_TYPES, "View"),
    ADMINISTRATE_SERVICE_CALL_TYPES(Constants.ADMINISTRATE_SERVICE_CALL_TYPES, "Administrate"),
    VIEW_SERVICE_CALL_OVERVIEW(Constants.VIEW_SERVICE_CALLS, "View service calls"),
    CHANGE_SERVICE_CALL_STATE(Constants.CHANGE_SERVICE_CALL_STATE, "Change state");

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
        String VIEW_SERVICE_CALL_TYPES = "privilege.view.serviceCallTypes";
        String ADMINISTRATE_SERVICE_CALL_TYPES = "privilege.administrate.serviceCallTypes";
        String VIEW_SERVICE_CALLS = "privilege.view.serviceCalls";
        String CHANGE_SERVICE_CALL_STATE = "privilege.administrate.serviceCall";

    }
}