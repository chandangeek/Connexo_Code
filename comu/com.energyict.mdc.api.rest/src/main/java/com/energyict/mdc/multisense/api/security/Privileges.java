/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.security;

import com.elster.jupiter.nls.TranslationKey;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_PUBLIC_API("public.api", "Public API"),
    RESOURCE_PUBLIC_API_DESCRIPTION("public.api.description", "Manage public API"),

    //Privileges
    PUBLIC_REST_API(Constants.PUBLIC_REST_API, "Invoke");

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
        String PUBLIC_REST_API = "privilege.public.api.rest";
    }
}
