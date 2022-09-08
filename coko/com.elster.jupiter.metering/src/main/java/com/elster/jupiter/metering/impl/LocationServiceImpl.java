/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.util.Optional;

public class LocationServiceImpl implements LocationService {
    private final DataModel dataModel;

    @Inject
    public LocationServiceImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public Optional<Location> findLocationById(long id) {
        return dataModel.mapper(Location.class).getOptional(id);
    }
}
