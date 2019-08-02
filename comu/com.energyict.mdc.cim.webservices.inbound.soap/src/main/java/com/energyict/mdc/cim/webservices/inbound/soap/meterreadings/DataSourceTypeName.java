/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterreadings;

import java.util.Arrays;
import java.util.Optional;

public enum DataSourceTypeName {

    REGISTER_GROUP("Register group"),
    LOAD_PROFILE("Load profile");

    private final String name;

    DataSourceTypeName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Optional<DataSourceTypeName> getByName(String name) {
        return Arrays.stream(DataSourceTypeName.values())
                .filter(typeName -> typeName.getName().equalsIgnoreCase(name))
                .findFirst();
    }
}