package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.GeoCoordinates;
import com.elster.jupiter.metering.MeteringService;

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
        Optional<GeoCoordinates> geoCoordinates = meteringService.findUsagePointGeoCoordinates(mRID);
        coordinatesDisplay = geoCoordinates.isPresent() ? geoCoordinates.get().getCoordinates().toString() : null;
        spatialCoordinates = geoCoordinates.isPresent() ?
                String.format("%s:%s:%s", geoCoordinates.get().getCoordinates().getLatitude().getValue().toString(),
                        geoCoordinates.get().getCoordinates().getLongitude().getValue().toString(),
                        geoCoordinates.get().getCoordinates().getElevation().getValue().toString()) : null;
    }
}
