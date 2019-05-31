/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterreadings;

import java.util.Arrays;

public enum DataSourceTypeNameEnum {

    REGISTER_GROUP("Register group"),
    LOAD_PROFILE("Load profile");

    private final String name;

    DataSourceTypeNameEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static DataSourceTypeNameEnum getByName(String name) {
        return Arrays.stream(DataSourceTypeNameEnum.values())
                .filter(typeName -> typeName.getName().equalsIgnoreCase(name))
                .findFirst().orElseGet(null);
    }
}