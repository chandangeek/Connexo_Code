/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

public enum KnownAmrSystem {
    MDC(1, "MDC"), ENERGY_AXIS(2, "EnergyAxis");

    private final int id;
    private final String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    KnownAmrSystem(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
