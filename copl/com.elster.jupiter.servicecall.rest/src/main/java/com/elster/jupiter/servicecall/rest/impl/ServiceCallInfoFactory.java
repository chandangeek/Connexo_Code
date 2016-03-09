package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ServiceCallInfoFactory {

    private final Thesaurus thesaurus;

    @Inject
    public ServiceCallInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ServiceCallInfo detailed(ServiceCall serviceCall, Map<DefaultState, Long> childrenInformation, PropertyUtils propertyUtils) {
        ServiceCallInfo serviceCallInfo = new ServiceCallInfo();
        serviceCallInfo.id = serviceCall.getId();
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
        addCustomPropertySetInfos(serviceCall, serviceCallInfo, propertyUtils);
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
                serviceCallInfo.parents.add(new IdWithNameInfo(parent.get().getId(), parent.get().getNumber()));
                parent = parent.get().getParent();
            } else {
                stillHasParent = false;
            }
        }
        Collections.reverse(serviceCallInfo.parents);
    }

    private void addChildrenInfo(ServiceCallInfo serviceCallInfo, Map<DefaultState, Long> childrenInformation) {
        serviceCallInfo.childrenInfo = new ArrayList<>();
        Long total = childrenInformation.values()
                .stream()
                .reduce(0L, (a, b) -> a + b);
        serviceCallInfo.numberOfChildren = total;
        childrenInformation.keySet().stream()
                .forEach(key -> serviceCallInfo.childrenInfo.add(new ServiceCallChildrenInfo(key.name(), key.getDisplayName(thesaurus),
                        Math.round((childrenInformation.get(key).doubleValue() / total.doubleValue()) * 100))));

        verifyRoundingErrors(childrenInformation);
    }

    private void verifyRoundingErrors(Map<DefaultState, Long> childrenInformation) {
        DefaultState keyMax = null;
        long max = 0;
        int sum = 0;
        for (DefaultState state : childrenInformation.keySet()) {
            long value = childrenInformation.get(state);
            sum += value;
            if (value > max) {
                max = value;
                keyMax = state;
            }
        }
        int difference = sum - 100;
        if (difference > 0 && keyMax != null) {
            childrenInformation.put(keyMax, childrenInformation.get(keyMax) - difference);
        }
    }

    private void addCustomPropertySetInfos(ServiceCall serviceCall, ServiceCallInfo serviceCallInfo, PropertyUtils propertyUtils) {
        serviceCallInfo.customPropertySetInfos = serviceCall.getType().getCustomPropertySets()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .map(registeredCustomPropertySet -> getServiceCallCustomPropertySetInfo(registeredCustomPropertySet, serviceCall, propertyUtils))
                .collect(Collectors.toList());
    }

    private ServiceCallCustomPropertySetInfo getServiceCallCustomPropertySetInfo(RegisteredCustomPropertySet propertySet, ServiceCall serviceCall, PropertyUtils propertyUtils) {
        CustomPropertySetValues extension = serviceCall.getValuesFor(propertySet.getCustomPropertySet());
        Map<String, Object> values = new HashMap<>();
        extension.propertyNames()
                .stream()
                .forEach(propertyName -> values.put(propertyName, extension.getProperty(propertyName)));
        return new ServiceCallCustomPropertySetInfo(propertySet, propertyUtils.convertPropertySpecsToPropertyInfos(propertySet.getCustomPropertySet().getPropertySpecs(), values));
    }

}
