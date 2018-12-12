/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;

public class LocationInfoFactory {
    private final MeteringService meteringService;
    private final Thesaurus thesaurus;
    private final LocationService locationService;

    @Inject
    public LocationInfoFactory(MeteringService meteringService, LocationService locationService, Thesaurus thesaurus) {
        this.meteringService = meteringService;
        this.locationService = locationService;
        this.thesaurus = thesaurus;
    }

    public EditLocationInfo from(Long locationId) {

        EditLocationInfo locationInfo = new EditLocationInfo();
        locationInfo.createLocationInfo(meteringService, locationService, thesaurus, locationId);
        return locationInfo;
    }
}
