/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.geo.Elevation;
import com.elster.jupiter.util.geo.Latitude;
import com.elster.jupiter.util.geo.Longitude;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import javax.inject.Inject;

public class LocationInfoFactory {

    private final LocationService locationService;
    private final ExceptionFactory exceptionFactory;
    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    public LocationInfoFactory(LocationService locationService, ExceptionFactory exceptionFactory, ThreadPrincipalService threadPrincipalService) {
        this.locationService = locationService;
        this.exceptionFactory = exceptionFactory;
        this.threadPrincipalService = threadPrincipalService;
    }

    public LocationInfo asInfo(Location location) {
        LocationInfo info = new LocationInfo();
        info.locationId = location.getId();
        location.getMember(threadPrincipalService.getLocale().toString()).ifPresent(locationMember -> {
            info.addressDetail = locationMember.getAddressDetail();
            info.administrativeArea = locationMember.getAdministrativeArea();
            info.countryCode = locationMember.getCountryCode();
            info.countryName = locationMember.getCountryName();
            info.defaultLocation = locationMember.isDefaultLocation();
            info.establishmentName = locationMember.getEstablishmentName();
            info.establishmentNumber = locationMember.getEstablishmentNumber();
            info.establishmentType = locationMember.getEstablishmentType();
            info.locale = locationMember.getLocale();
            info.locality = locationMember.getLocality();
            info.streetName = locationMember.getStreetName();
            info.streetNumber = locationMember.getStreetNumber();
            info.streetType = locationMember.getStreetType();
            info.subLocality = locationMember.getSubLocality();
            info.zipCode = locationMember.getZipCode();
        });
        return info;
    }

    public Location fromInfo(LocationInfo location) {
        if (location != null && location.locationId != null) {
            return locationService.findLocationById(location.locationId)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_LOCATION, location.locationId));
        }
        return null;
    }

    public CoordinatesInfo asInfo(SpatialCoordinates spatialCoordinates) {
        CoordinatesInfo info = new CoordinatesInfo();
        info.latitude = spatialCoordinates.getLatitude().getValue();
        info.longitude = spatialCoordinates.getLongitude().getValue();
        info.elevation = spatialCoordinates.getElevation().getValue();
        return info;
    }

    public SpatialCoordinates fromInfo(CoordinatesInfo coordinatesInfo) {
        if (coordinatesInfo != null && coordinatesInfo.latitude != null && coordinatesInfo.longitude != null && coordinatesInfo.elevation != null) {
            return new SpatialCoordinates(
                    new Latitude(coordinatesInfo.latitude),
                    new Longitude(coordinatesInfo.longitude),
                    new Elevation(coordinatesInfo.elevation));
        }
        return null;
    }
}
