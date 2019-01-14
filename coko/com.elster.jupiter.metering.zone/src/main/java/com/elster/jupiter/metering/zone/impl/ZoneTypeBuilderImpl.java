/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.metering.zone.ZoneType;
import com.elster.jupiter.metering.zone.ZoneTypeBuilder;
import com.elster.jupiter.orm.DataModel;

public class ZoneTypeBuilderImpl implements ZoneTypeBuilder {

    private final DataModel dataModel;

    private String name;
    private String application;

    public ZoneTypeBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public ZoneTypeBuilderImpl withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public ZoneTypeBuilderImpl withApplication(String application) {
        this.application = application;
        return this;
    }

    @Override
    public ZoneType create() {
        ZoneTypeImpl zoneType = ZoneTypeImpl.from(dataModel, name, application);
        zoneType.setName(name);
        zoneType.setApplication(application);
        zoneType.save();
        return zoneType;
    }
}
