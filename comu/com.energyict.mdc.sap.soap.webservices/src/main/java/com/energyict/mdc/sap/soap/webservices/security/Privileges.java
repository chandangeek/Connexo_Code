/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_SAP("sap.sap", "SAP"),
    RESOURCE_SAP_DESCRIPTION("sap.sap.description", "Manage SAP"),

    //Privileges
    SEND_WEB_SERVICE_REQUEST(Constants.SEND_WEB_SERVICE_REQUEST, "Send web service request")
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
                .collect(Collectors.toList())
                .toArray(new String[Privileges.values().length]);
    }

    public interface Constants {
        String SEND_WEB_SERVICE_REQUEST = "privilege.send.webservice.request";
    }
}
