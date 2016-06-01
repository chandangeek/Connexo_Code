package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.device.data.Device;

public class CoordinatesInfo {

    public String coordinatesDisplay;
    public String spatialCoordinates;
    public Long deviceGeoCoordinatesId;
    public boolean isInherited = false;
    public String usagePointCoordinatesDisplay;
    public String usagePointSpatialCoordinates;
    public Long usagePointGeoCoordinatesId;

    public CoordinatesInfo() {
    }

    public CoordinatesInfo(MeteringService meteringService, Device device) {
        meteringService.findDeviceGeoCoordinates(device.getmRID()).ifPresent(deviceGeoCoordinates -> {
            coordinatesDisplay = deviceGeoCoordinates.getCoordinates().toString();
            spatialCoordinates = String.format("%s:%s:%s", deviceGeoCoordinates.getCoordinates().getLatitude().getValue().toString(),
                    deviceGeoCoordinates.getCoordinates().getLongitude().getValue().toString(),
                    deviceGeoCoordinates.getCoordinates().getElevation().getValue().toString());
            deviceGeoCoordinatesId = deviceGeoCoordinates.getId();
        });
        device.getUsagePoint().ifPresent(usagePoint -> {
            usagePoint.getGeoCoordinates().ifPresent(usagePointGeoCoordinates -> {
                if((deviceGeoCoordinatesId != null) && (usagePointGeoCoordinates.getId() == deviceGeoCoordinatesId)){
                    isInherited = true;
                }
                usagePointCoordinatesDisplay = usagePointGeoCoordinates.getCoordinates().toString();
                usagePointSpatialCoordinates = String.format("%s:%s:%s", usagePointGeoCoordinates.getCoordinates().getLatitude().getValue().toString(),
                        usagePointGeoCoordinates.getCoordinates().getLongitude().getValue().toString(),
                        usagePointGeoCoordinates.getCoordinates().getElevation().getValue().toString());
                usagePointGeoCoordinatesId = usagePointGeoCoordinates.getId();
            });
        });
    }

}
