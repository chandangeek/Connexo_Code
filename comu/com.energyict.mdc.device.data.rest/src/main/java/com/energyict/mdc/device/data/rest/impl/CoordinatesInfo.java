package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.GeoCoordinates;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.rest.util.properties.PropertyInfo;

import java.util.Optional;
import java.util.Set;

public class CoordinatesInfo {

    public String coordinatesDisplay;
    public String spatialCoordinates;

    public CoordinatesInfo() {
    }

    public CoordinatesInfo(MeteringService meteringService, String mRID) {
        Optional<GeoCoordinates> geoCoordinates = meteringService.findDeviceGeoCoordinates(mRID);
        coordinatesDisplay = geoCoordinates.isPresent() ? geoCoordinates.get().getCoordinates().toString(): null;
        spatialCoordinates = geoCoordinates.isPresent() ?
                String.format("%s:%s:%s", geoCoordinates.get().getCoordinates().getLatitude().getValue().toString(),
                        geoCoordinates.get().getCoordinates().getLongitude().getValue().toString(),
                        geoCoordinates.get().getCoordinates().getElevation().getValue().toString()): null;
    }

}
