package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.DefaultState;

import java.util.List;

public class ServiceCallInfo {
    public long id;
    public String name;
    public long version;
    public long creationTime;
    public long lastModificationTime;
    public long lastCompletedTime;
    public DefaultState state;
    public String origin;
    public String externalReference;
    public Object targetObject;
    public List<IdWithNameInfo> parents;
    public String type;
    public boolean hasChildren;

    public ServiceCallInfo() {

    }

}
