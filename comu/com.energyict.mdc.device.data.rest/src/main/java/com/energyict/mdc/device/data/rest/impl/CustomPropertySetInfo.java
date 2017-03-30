/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.properties.rest.PropertyInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomPropertySetInfo {

    public long id;
    public long parent;
    public long version;
    public long objectTypeId;
    public long objectTypeVersion;
    public String name;
    public boolean editable;
    public boolean timesliced;
    public long versionId;
    public Long startTime;
    public Long endTime;
    public Boolean isActive;
    public String customPropertySetId;
    public List<PropertyInfo> properties;

    public CustomPropertySetInfo() {
    }

    public CustomPropertySetInfo(RegisteredCustomPropertySet registeredCustomPropertySet, List<PropertyInfo> properties, long objectId, long objectVersion, long objectTypeId, long objectTypeVersion) {
        this.id = registeredCustomPropertySet.getId();
        this.parent = objectId;
        this.version = objectVersion;
        this.objectTypeId = objectTypeId;
        this.objectTypeVersion = objectTypeVersion;
        this.name = registeredCustomPropertySet.getCustomPropertySet().getName();
        this.editable = registeredCustomPropertySet.isEditableByCurrentUser();
        this.timesliced = registeredCustomPropertySet.getCustomPropertySet().isVersioned();
        this.properties = properties;
        this.customPropertySetId = registeredCustomPropertySet.getCustomPropertySet().getId();
    }

    public CustomPropertySetInfo(RegisteredCustomPropertySet registeredCustomPropertySet, List<PropertyInfo> properties, long objectId, long objectVersion, long objectTypeId, long objectTypeVersion, Range<Instant> effective) {
        this.id = registeredCustomPropertySet.getId();
        this.parent = objectId;
        this.version = objectVersion;
        this.objectTypeId = objectTypeId;
        this.objectTypeVersion = objectTypeVersion;
        this.name = registeredCustomPropertySet.getCustomPropertySet().getName();
        this.editable = registeredCustomPropertySet.isEditableByCurrentUser();
        this.timesliced = registeredCustomPropertySet.getCustomPropertySet().isVersioned();
        this.properties = properties;
        this.versionId =  effective.hasLowerBound() ? effective.lowerEndpoint().toEpochMilli() : 0;
        this.startTime = effective.hasLowerBound() ? effective.lowerEndpoint().toEpochMilli() : null;
        this.endTime = effective.hasUpperBound() ? effective.upperEndpoint().toEpochMilli() : null;
        this.isActive = !properties.isEmpty() && properties.get(0).getPropertyValueInfo().getValue() != null;
        this.customPropertySetId = registeredCustomPropertySet.getCustomPropertySet().getId();
    }
}