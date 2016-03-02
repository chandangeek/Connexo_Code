package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.servicecall.DefaultState;

import java.util.List;

public class ServiceCallInfo {
    public String number;
    public long version;
    public long creationTime;
    public long lastModificationTime;
    public long lastCompletedTime;
    public DefaultState state;
    public String origin;
    public String externalReference;
    public Object targetObject;
    public List<String> parents;
    public String type;
    public boolean hasChildren;

    public ServiceCallInfo() {

    }

}
