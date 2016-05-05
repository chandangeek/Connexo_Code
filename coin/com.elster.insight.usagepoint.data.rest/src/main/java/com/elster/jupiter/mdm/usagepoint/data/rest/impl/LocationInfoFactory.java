package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;

/**
 * Created by david on 4/29/2016.
 */
public class LocationInfoFactory {
    private final MeteringService meteringService;
    private final Thesaurus thesaurus;

    @Inject
    public LocationInfoFactory(MeteringService meteringService, Thesaurus thesaurus) {
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
    }

    public LocationInfo from(Long locationId) {

        LocationInfo locationInfo = new LocationInfo();
        locationInfo.createLocationInfo(meteringService, thesaurus, locationId);
        return locationInfo;
    }
}
