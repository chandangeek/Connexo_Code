package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.MeteringService;
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

    public CoordinatesInfo(MeteringService meteringService, String mRID) {
        meteringService.findUsagePoint(mRID).ifPresent(usagePoint ->
        {
            Optional<SpatialCoordinates> geoCoordinates = usagePoint.getSpatialCoordinates();
            coordinatesDisplay = geoCoordinates.isPresent() ? geoCoordinates.get().toString() : null;
            spatialCoordinates = geoCoordinates.isPresent() ?
                    String.format("%s:%s:%s", geoCoordinates.get().getLatitude().getValue().toString(),
                            geoCoordinates.get().getLongitude().getValue().toString(),
                            geoCoordinates.get().getElevation().getValue().toString()) : null;
        });
    }

}
