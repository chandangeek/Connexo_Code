package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.whiteboard.ReferenceInfo;

import java.util.List;

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
    public ReferenceInfo targetObject;
    public List<IdWithNameInfo> parents;
    public List<ServiceCallChildrenInfo> children;
    public String type;
    public long numberOfChildren;
    public List<ServiceCallCustomPropertySetInfo> customPropertySets;
    public boolean canCancel;

    public ServiceCallInfo() {

    }

}
