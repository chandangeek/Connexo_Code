/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl.zone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoneInfo {

    public long id;
    public String name;
    public String application;
    public long zoneTypeId;
    public String zoneTypeName;
    public long version;

    public ZoneInfo() {
    }

    public ZoneInfo(long id, String name, String application, long zoneTypeId, String zoneTypeName, long version) {
        this.id = id;
        this.application = application;
        this.name = name;
        this.zoneTypeId = zoneTypeId;
        this.zoneTypeName = zoneTypeName;
        this.version = version;
    }
}
