/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.kpi;

import com.elster.jupiter.nls.TranslationKey;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_REGISTERED_DEVICES_KPI("registereddeviceskpi", "Registered devices KPI"),
    RESOURCE_REGISTERED_DEVICES_KPI_DESCRIPTION("registereddeviceskpi.description", "Manage registered devices KPIs"),

    //Privileges
    ADMINISTRATE_REGISTERED_DEVICES_KPI(Constants.ADMINISTRATE, "Administrate"),
    VIEW_REGISTERED_DEVICES_KPI(Constants.VIEW, "View")
    ;

    private final String key;
    private final String defaultFormat;

    Privileges(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public interface Constants {
        String ADMINISTRATE = "registereddeviceskpi.privileges.administrate";
        String VIEW = "registereddeviceskpi.privileges.view";
    }
}
