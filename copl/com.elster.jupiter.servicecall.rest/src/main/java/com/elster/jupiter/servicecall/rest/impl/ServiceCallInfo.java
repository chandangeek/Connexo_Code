package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public ServiceCallInfo() {

    }

    public ServiceCallInfo(ServiceCall serviceCall, Thesaurus thesaurus) {
        number = serviceCall.getNumber();
        version = serviceCall.getVersion();
        creationTime = serviceCall.getCreationTime().toEpochMilli();
        lastModificationTime = serviceCall.getLastModificationTime().toEpochMilli();
        Optional<Instant> lastCompletedOptional = serviceCall.getLastCompletedTime();
        if (lastCompletedOptional.isPresent()) {
            lastCompletedTime = lastCompletedOptional.get().toEpochMilli();
        }
        state = serviceCall.getState();
        origin = serviceCall.getOrigin().isPresent() ? serviceCall.getOrigin().get() : null;
        externalReference = serviceCall.getExternalReference().isPresent() ? serviceCall.getExternalReference().get() : null;
//        targetObject = serviceCall.getTargetObject().isPresent() ? serviceCall.getTargetObject().get() : null;
        //addParents(serviceCall.getParent());
        type = serviceCall.getType().getName();
    }

    private void addParents(Optional<ServiceCall> parent) {
        boolean stillHasParent = true;
        parents = new ArrayList<>();

        while(stillHasParent) {
            if(parent.isPresent()) {
                parents.add(parent.get().getNumber());
                parent = parent.get().getParent();
            } else {
                stillHasParent = false;
            }
        }
    }
}
