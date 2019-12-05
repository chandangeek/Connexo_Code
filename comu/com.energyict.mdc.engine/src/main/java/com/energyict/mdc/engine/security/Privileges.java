/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.security;
/*
public interface Privileges {
}

*/

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    //Resources
    RESOURCE_MOBILE_COMSERVER("comserver.mobileComServer", "Mobile ComServer"),
    RESOURCE_MOBILE_COMSERVER_DESCRIPTION("comserver.mobileComServer.description", "Employ the Mobile ComServer"),

    //Privileges
    OPERATE_MOBILE_COMSERVER(Constants.OPERATE_MOBILE_COMSERVER, "Operate");

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
        String OPERATE_MOBILE_COMSERVER = "privilege.operate.mobileComServer";
    }
}