/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.profile;

import com.energyict.mdc.common.Unit;

public class ChannelConfig {

    private final int id;
    private final String description;
    private final Unit unit;

    public ChannelConfig(int id, Unit unit, String description) {
        this.description = description;
        this.id = id;
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public Unit getUnit() {
        return unit;
    }
}
