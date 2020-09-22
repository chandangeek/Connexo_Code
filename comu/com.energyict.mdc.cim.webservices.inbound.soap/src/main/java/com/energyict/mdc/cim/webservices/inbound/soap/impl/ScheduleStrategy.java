/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import java.util.Arrays;

public enum ScheduleStrategy {

    RUN_NOW("Run now"),
    RUN_WITH_PRIORITY("Run with priority"),
    USE_SCHEDULE("Use schedule");

    private final String name;

    ScheduleStrategy(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ScheduleStrategy getByName(String name) {
        return Arrays.stream(ScheduleStrategy.values())
                .filter(strategy -> strategy.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }
}