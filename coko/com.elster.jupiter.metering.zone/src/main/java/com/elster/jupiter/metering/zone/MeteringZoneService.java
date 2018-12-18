/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone;

import com.elster.jupiter.domain.util.Finder;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface MeteringZoneService {

    String COMPONENTNAME = "MTZ";

    List<ZoneType> getZoneTypes(String application);

    Optional<ZoneType> getZoneType(String name, String application);

    ZoneTypeBuilder newZoneTypeBuilder();

    Finder<Zone> getZones(String application, ZoneFilter zoneFilter);

    Optional<Zone> getZone(long id);

    Optional<Zone> getAndLockZone(long id, long version);

    ZoneBuilder newZoneBuilder();

    ZoneFilter newZoneFilter();


}
