/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterreadings;

import java.util.Arrays;

public enum ScheduleStrategyEnum {

    RUN_NOW("Run now"),
    USE_SCHEDULE("Use schedule");

    private final String name;

    ScheduleStrategyEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ScheduleStrategyEnum getByName(String name) {
        return Arrays.stream(ScheduleStrategyEnum.values())
                .filter(strategy -> strategy.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }
}