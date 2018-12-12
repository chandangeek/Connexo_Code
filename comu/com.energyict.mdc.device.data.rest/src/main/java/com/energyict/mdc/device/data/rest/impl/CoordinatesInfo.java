/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.energyict.mdc.device.data.Device;

import java.util.Optional;

public class CoordinatesInfo {

    public String coordinatesDisplay;
    public String spatialCoordinates;
    public boolean isInherited = false;
    public String usagePointCoordinatesDisplay;
    public String usagePointSpatialCoordinates;


    public CoordinatesInfo() {
    }

    public CoordinatesInfo(Device device) {
        Optional<SpatialCoordinates> coordinates = device.getSpatialCoordinates();
        coordinates.ifPresent(deviceGeoCoordinates -> {
            coordinatesDisplay = deviceGeoCoordinates.toString();
            spatialCoordinates = String.format("%s:%s:%s", deviceGeoCoordinates.getLatitude().getValue().toString(),
                    deviceGeoCoordinates.getLongitude().getValue().toString(),
                    deviceGeoCoordinates.getElevation().getValue().toString());
        });
        device.getUsagePoint().ifPresent(usagePoint -> {
            usagePoint.getSpatialCoordinates().ifPresent(usagePointGeoCoordinates -> {
                if (spatialCoordinates != null && usagePointGeoCoordinates.equals(coordinates.get())) {
                    isInherited = true;
                }
                usagePointCoordinatesDisplay = usagePointGeoCoordinates.toString();
                usagePointSpatialCoordinates = String.format("%s:%s:%s", usagePointGeoCoordinates.getLatitude().getValue().toString(),
                        usagePointGeoCoordinates.getLongitude().getValue().toString(),
                        usagePointGeoCoordinates.getElevation().getValue().toString());
            });
        });
    }


}
