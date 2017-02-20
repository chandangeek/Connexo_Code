/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2.issue;

import com.elster.jupiter.metering.Location;
import com.elster.jupiter.security.thread.ThreadPrincipalService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class LocationShortInfoFactory {

    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    public LocationShortInfoFactory(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    public LocationShortInfo asInfo(Location location) {
        LocationShortInfo info = new LocationShortInfo();
        location.getMember(threadPrincipalService.getLocale().toString()).ifPresent(locationMember -> {
            info.address = new ArrayList<String>() {{
                add(locationMember.getCountryCode());
                add(locationMember.getCountryName());
                add(locationMember.getAdministrativeArea());
                add(locationMember.getLocality());
                add(locationMember.getSubLocality());
                add(locationMember.getStreetType());
                add(locationMember.getStreetName());
                add(locationMember.getStreetNumber());
                add(locationMember.getEstablishmentType());
                add(locationMember.getEstablishmentName());
                add(locationMember.getEstablishmentNumber());
                add(locationMember.getZipCode());
                add(locationMember.isDefaultLocation() ? "isDefaultLocation" : "isNotDefaultLocation");
            }}.stream().collect(Collectors.joining(", "));
        });
        return info;
    }
}
