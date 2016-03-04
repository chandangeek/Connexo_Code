package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.ServiceCall;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ServiceCallInfoFactory {

    public ServiceCallInfo detailed(ServiceCall serviceCall, Map<String, Long> childrenInformation) {
        ServiceCallInfo serviceCallInfo = new ServiceCallInfo();
        serviceCallInfo.id  = serviceCall.getId();
        serviceCallInfo.name = serviceCall.getNumber();
        serviceCallInfo.version = serviceCall.getVersion();
        serviceCallInfo.creationTime = serviceCall.getCreationTime().toEpochMilli();
        serviceCallInfo.lastModificationTime = serviceCall.getLastModificationTime().toEpochMilli();
        Optional<Instant> lastCompletedOptional = serviceCall.getLastCompletedTime();
        if (lastCompletedOptional.isPresent()) {
            serviceCallInfo.lastCompletedTime = lastCompletedOptional.get().toEpochMilli();
        }
        serviceCallInfo.state = serviceCall.getState();
        serviceCallInfo.origin = serviceCall.getOrigin().isPresent() ? serviceCall.getOrigin().get() : null;
        serviceCallInfo.externalReference = serviceCall.getExternalReference()
                .isPresent() ? serviceCall.getExternalReference().get() : null;
//        targetObject = serviceCall.getTargetObject().isPresent() ? serviceCall.getTargetObject().get() : null;
        addParents(serviceCallInfo, serviceCall.getParent());
        addChildrenInfo(serviceCallInfo, childrenInformation);
        serviceCallInfo.type = serviceCall.getType().getName();
        return serviceCallInfo;
    }

    public ServiceCallInfo summarized(ServiceCall serviceCall) {
        ServiceCallInfo serviceCallInfo = new ServiceCallInfo();
        serviceCallInfo.id = serviceCall.getId();
        serviceCallInfo.name = serviceCall.getNumber();
        serviceCallInfo.version = serviceCall.getVersion();
        serviceCallInfo.creationTime = serviceCall.getCreationTime().toEpochMilli();
        serviceCallInfo.lastModificationTime = serviceCall.getLastModificationTime().toEpochMilli();
        serviceCallInfo.state = serviceCall.getState();
        serviceCallInfo.externalReference = serviceCall.getExternalReference()
                .isPresent() ? serviceCall.getExternalReference().get() : null;
        serviceCallInfo.type = serviceCall.getType().getName();
        return serviceCallInfo;
    }

    private void addParents(ServiceCallInfo serviceCallInfo, Optional<ServiceCall> parent) {
        boolean stillHasParent = true;
        serviceCallInfo.parents = new ArrayList<>();

        while (stillHasParent) {
            if (parent.isPresent()) {
                serviceCallInfo.parents.add(new IdWithNameInfo( parent.get().getId(),parent.get().getNumber()));
                parent = parent.get().getParent();
            } else {
                stillHasParent = false;
            }
        }
        Collections.reverse(serviceCallInfo.parents);
    }

    private void addChildrenInfo(ServiceCallInfo serviceCallInfo, Map<String, Long> childrenInformation) {
        if (childrenInformation.size() > 0) {
            serviceCallInfo.hasChildren = true;
        }
    }

}
