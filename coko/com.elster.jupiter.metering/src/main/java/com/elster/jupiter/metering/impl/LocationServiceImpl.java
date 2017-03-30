/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Optional;

@Component(name = "com.elster.jupiter.location",
        service = {LocationService.class},
        property = "name=" + LocationService.COMPONENT_NAME)
@SuppressWarnings("unused")
public class LocationServiceImpl implements LocationService {

    private volatile DataModel dataModel;

    // For OSGi purposes
    public LocationServiceImpl() {
    }

    // For testing purposes
    @Inject
    public LocationServiceImpl(OrmService ormService) {
        this();
        this.setOrmService(ormService);
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        ormService.getDataModel("MTR").ifPresent(found -> this.dataModel = found);
    }

    @Override
    public Optional<Location> findLocationById(long id) {
        return dataModel.mapper(Location.class).getOptional(id);
    }

}