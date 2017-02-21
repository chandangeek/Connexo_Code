/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2.issue;

import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.security.thread.ThreadPrincipalService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class LocationShortInfoFactory {

    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    public LocationShortInfoFactory(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    public LocationShortInfo asInfo(Location location) {
        LocationShortInfo info = new LocationShortInfo();

        if (location != null && location.getMembers().size() > 0) {
            LocationMember locationMember = getLocationMember(location);
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
            }}.stream().filter(Objects::nonNull).collect(Collectors.joining(", "));
        }
        return info;
    }

    private LocationMember getLocationMember(Location location) {
        List<? extends LocationMember> members = location.getMembers();
            Optional<LocationMember> member = location.getMember(threadPrincipalService.getLocale().toString());
            if (member.isPresent()) {
                return member.get();
            } else {
                return members.get(0);
            }
    }

}
