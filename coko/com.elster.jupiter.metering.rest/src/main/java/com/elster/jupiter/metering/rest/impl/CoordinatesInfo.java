/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import java.util.Optional;

public class CoordinatesInfo {

    public String coordinatesDisplay;
    public String spatialCoordinates;
    public boolean isInherited = false;
    public String usagePointCoordinatesDisplay;
    public String usagePointSpatialCoordinates;


    public CoordinatesInfo() {
    }

    public CoordinatesInfo(UsagePoint usagePoint) {
        Optional<SpatialCoordinates> coordinates = usagePoint.getSpatialCoordinates();
        coordinates.ifPresent(deviceGeoCoordinates -> {
            coordinatesDisplay = deviceGeoCoordinates.toString();
            spatialCoordinates = String.format("%s:%s:%s", deviceGeoCoordinates.getLatitude().getValue().toString(),
                    deviceGeoCoordinates.getLongitude().getValue().toString(),
                    deviceGeoCoordinates.getElevation().getValue().toString());
        });
    }


}
