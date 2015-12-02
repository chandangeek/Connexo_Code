package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomPropertySetInfo {

    public long id;
    public long parent;
    public long version;
    public String name;
    public boolean editable;
    public boolean timesliced;
    public long versionId;
    public Long startTime;
    public Long endTime;
    public Boolean isActive;
    public List<PropertyInfo> properties;

    public CustomPropertySetInfo() {
    }

    public CustomPropertySetInfo(RegisteredCustomPropertySet registeredCustomPropertySet, List<PropertyInfo> properties, long objectId, long objectVersion) {
        this.id = registeredCustomPropertySet.getId();
        this.parent = objectId;
        this.version = objectVersion;
        this.name = registeredCustomPropertySet.getCustomPropertySet().getName();
        this.editable = registeredCustomPropertySet.isEditableByCurrentUser();
        this.timesliced = registeredCustomPropertySet.getCustomPropertySet().isVersioned();
        this.properties = properties;
    }

    public CustomPropertySetInfo(RegisteredCustomPropertySet registeredCustomPropertySet, List<PropertyInfo> properties, long objectId, long objectVersion, Range<Instant> effective) {
        this.id = registeredCustomPropertySet.getId();
        this.parent = objectId;
        this.version = objectVersion;
        this.name = registeredCustomPropertySet.getCustomPropertySet().getName();
        this.editable = registeredCustomPropertySet.isEditableByCurrentUser();
        this.timesliced = registeredCustomPropertySet.getCustomPropertySet().isVersioned();
        this.properties = properties;
        this.versionId =  effective.hasLowerBound() ? effective.lowerEndpoint().toEpochMilli() : 0;
        this.startTime = effective.hasLowerBound() ? effective.lowerEndpoint().toEpochMilli() : null;
        this.endTime = effective.hasUpperBound() ? effective.upperEndpoint().toEpochMilli() : null;
        this.isActive = !properties.isEmpty() && properties.get(0).getPropertyValueInfo().getValue() != null;
    }
}