/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;

/**
 * Created by david on 4/29/2016.
 */
public class LocationInfoFactory {
    private final MeteringService meteringService;
    private final Thesaurus thesaurus;
    private final LocationService locationService;

    @Inject
    public LocationInfoFactory(MeteringService meteringService, Thesaurus thesaurus, LocationService locationService) {
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
        this.locationService = locationService;
    }

    public LocationInfo from(Long locationId) {

        LocationInfo locationInfo = new LocationInfo();
        locationInfo.createLocationInfo(meteringService, locationService, thesaurus, locationId);
        return locationInfo;
    }
}
