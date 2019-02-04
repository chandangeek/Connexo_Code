/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.EndDevice;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface MeteringZoneService {

    String COMPONENTNAME = "MTZ";

    String BULK_ZONE_QUEUE_DESTINATION = "BulkZoneQD";
    String BULK_ZONE_QUEUE_SUBSCRIBER = "BulkZoneQS";
    String BULK_ZONE_QUEUE_DISPLAYNAME = "Handle zone link/unlink to/from device";


    /*
    Zone type section
    */
    List<ZoneType> getZoneTypes(String application);

    Optional<ZoneType> getZoneType(String name, String application);

    ZoneTypeBuilder newZoneTypeBuilder();

    /*
    Zone section
    */

    Finder<Zone> getZones(String application, ZoneFilter zoneFilter);

    Optional<Zone> getZone(long id);

    Optional<Zone> getAndLockZone(long id, long version);

    ZoneBuilder newZoneBuilder();

    ZoneFilter newZoneFilter();

    /*
    End device zone
    */
    Finder<EndDeviceZone> getByEndDevice(EndDevice endDevice);

    EndDeviceZoneBuilder newEndDeviceZoneBuilder();

    Optional<EndDeviceZone> getEndDeviceZone(long endDeviceZoneId);

    boolean isZoneInUse(long zoneId);
}
