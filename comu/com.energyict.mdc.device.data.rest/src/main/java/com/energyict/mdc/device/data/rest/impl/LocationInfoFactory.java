package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;


import javax.inject.Inject;

public class LocationInfoFactory {
    private final MeteringService meteringService;
    private final Thesaurus thesaurus;

    @Inject
    public LocationInfoFactory(MeteringService meteringService, Thesaurus thesaurus) {
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
    }

    public EditLocationInfo from(Long locationId) {

        EditLocationInfo locationInfo = new EditLocationInfo();
        locationInfo.createLocationInfo(meteringService, thesaurus, locationId);
        return locationInfo;
    }
}
