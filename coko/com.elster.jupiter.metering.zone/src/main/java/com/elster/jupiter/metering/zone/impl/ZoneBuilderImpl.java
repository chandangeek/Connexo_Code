/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.metering.zone.ZoneBuilder;
import com.elster.jupiter.metering.zone.ZoneType;
import com.elster.jupiter.orm.DataModel;

public class ZoneBuilderImpl implements ZoneBuilder {

    private final DataModel dataModel;

    private String name;
    private ZoneType zoneType;

    public ZoneBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public ZoneBuilderImpl withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public ZoneBuilderImpl withZoneType(ZoneType zoneType) {
        this.zoneType = zoneType;
        return this;
    }

    @Override
    public Zone create() {
        ZoneImpl zone = ZoneImpl.from(dataModel, name, zoneType);
        zone.setName(name);
        zone.setZoneType(zoneType);
        zone.save();
        return zone;
    }
}
