/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.VersionInfo;

import java.time.Instant;
import java.util.List;

/**
 * Created by bvn on 10/22/14.
 */
public class DeviceMessageInfo {

    public long id;
    public IdWithNameInfo trackingIdAndName;
    public TrackingCategoryInfo trackingCategory;
    public DeviceMessageSpecInfo messageSpecification;
    public String category;
    public StatusInfo status;
    public Instant creationDate;
    public Instant releaseDate;
    public Instant sentDate;
    public String user;
    public String errorMessage;
    public Boolean willBePickedUpByComTask;
    public Boolean willBePickedUpByPlannedComTask;
    public List<PropertyInfo> properties;
    public IdWithNameInfo preferredComTask;
    public boolean userCanAdministrate;
    public long version;
    public VersionInfo<String> parent;

    public DeviceMessageInfo() {
    }

    static class StatusInfo {
        public String displayValue;
        public String value;
    }

    static class TrackingCategoryInfo {
        public String id;
        public String name;
        public boolean activeLink;
    }
}

