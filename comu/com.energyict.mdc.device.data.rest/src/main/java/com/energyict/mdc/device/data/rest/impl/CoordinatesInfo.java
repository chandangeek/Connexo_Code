package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Device;

public class CoordinatesInfo {

    public String coordinatesDisplay;
    public String spatialCoordinates;
    public boolean isInherited = false;
    public String usagePointCoordinatesDisplay;
    public String usagePointSpatialCoordinates;


    public CoordinatesInfo() {
    }

    public CoordinatesInfo(Device device) {
        device.getSpatialCoordinates().ifPresent(deviceGeoCoordinates -> {
            coordinatesDisplay = deviceGeoCoordinates.toString();
            spatialCoordinates = String.format("%s:%s:%s", deviceGeoCoordinates.getLatitude().getValue().toString(),
                    deviceGeoCoordinates.getLongitude().getValue().toString(),
                    deviceGeoCoordinates.getElevation().getValue().toString());
        });
        device.getUsagePoint().ifPresent(usagePoint -> {
            usagePoint.getSpatialCoordinates().ifPresent(usagePointGeoCoordinates -> {
                if ((spatialCoordinates != null) && (usagePointGeoCoordinates.equals(device.getSpatialCoordinates()))) {
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
