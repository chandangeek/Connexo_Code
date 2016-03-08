package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.rest.util.properties.PropertyInfo;

import java.util.List;

public class ServiceCallCustomPropertySetInfo {
    public long id;
    public long parent;
    public long version;
    public long objectTypeId;
    public long objectTypeVersion;
    public String name;
    public boolean editable;
    public boolean timesliced;
    public String customPropertySetId;
    public List<PropertyInfo> properties;

    public ServiceCallCustomPropertySetInfo() {
    }

    public ServiceCallCustomPropertySetInfo(RegisteredCustomPropertySet registeredCustomPropertySet, List<PropertyInfo> properties, long objectId, long objectVersion, long objectTypeId, long objectTypeVersion) {
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

}
