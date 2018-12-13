/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import java.util.Optional;

/**
 * Created by david on 4/29/2016.
 */
public class CoordinatesInfo {
    public String coordinatesDisplay;
    public String spatialCoordinates;

    public CoordinatesInfo() {
    }

    public CoordinatesInfo(UsagePoint usagePoint) {
        Optional<SpatialCoordinates> geoCoordinates = usagePoint.getSpatialCoordinates();
        coordinatesDisplay = geoCoordinates.map(SpatialCoordinates::toString).orElse(null);
        spatialCoordinates = geoCoordinates
                .map(coordinates -> String.format("%s:%s:%s",
                        coordinates.getLatitude().getValue().toString(),
                        coordinates.getLongitude().getValue().toString(),
                        coordinates.getElevation().getValue().toString()))
                .orElse(null);
    }

}
