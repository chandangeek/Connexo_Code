/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    //Resources
    RESOURCE_WEB_SERVICES("webservices.webservices", "Web services"),
    RESOURCE_WEB_SERVICES_DESCRIPTION("webservices.webservices.description", "Manage web services"),


    //Privileges
    VIEW_WEB_SERVICES(Constants.VIEW_WEB_SERVICES, "View"),
    ADMINISTRATE_WEB_SERVICES(Constants.ADMINISTRATE_WEB_SERVICES, "Administrate");

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
        String VIEW_WEB_SERVICES = "privilege.view.webservices";
        String ADMINISTRATE_WEB_SERVICES = "privilege.administrate.webservices";

    }
}