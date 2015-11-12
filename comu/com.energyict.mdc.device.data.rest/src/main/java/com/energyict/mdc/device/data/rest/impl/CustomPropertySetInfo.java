package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomPropertySetInfo {

    public long id;
    public long parent;
    public long version;
    public String name;
    public boolean editable;
    public boolean timesliced;
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
}