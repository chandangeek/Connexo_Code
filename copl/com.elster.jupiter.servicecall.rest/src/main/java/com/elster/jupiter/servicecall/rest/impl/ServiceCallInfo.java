package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.DefaultState;

import java.util.List;
import java.util.Map;

public class ServiceCallInfo {
    public long id;
    public String name;
    public long version;
    public long creationTime;
    public long lastModificationTime;
    public long lastCompletedTime;
    public String state;
    public String origin;
    public String externalReference;
    public Object targetObject;
    public List<IdWithNameInfo> parents;
    public List<ServiceCallChildrenInfo> children;
    public String type;
    public long numberOfChildren;
    public List<ServiceCallCustomPropertySetInfo> customPropertySets;

    public ServiceCallInfo() {

    }

}
