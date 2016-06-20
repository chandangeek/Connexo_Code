package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.whiteboard.ReferenceInfo;
import com.elster.jupiter.rest.whiteboard.ReferenceResolver;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.rest.ServiceCallInfo;
import com.elster.jupiter.servicecall.rest.ServiceCallInfoFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.servicecall.rest.ServiceCallInfoFactory",
        immediate = true,
        service = ServiceCallInfoFactory.class)
class ServiceCallInfoFactoryImpl implements ServiceCallInfoFactory {

    private Thesaurus thesaurus;
    private PropertyUtils propertyUtils;
    private ReferenceResolver referenceResolver;

    //osgi
    @SuppressWarnings("unused")
    ServiceCallInfoFactoryImpl() {
        this(new PropertyUtils());
    }

    @Inject
    ServiceCallInfoFactoryImpl(Thesaurus thesaurus, PropertyUtils propertyUtils, ReferenceResolver referenceResolver) {
        this(propertyUtils);
        this.thesaurus = thesaurus;
        this.setReferenceResolver(referenceResolver);
    }

    private ServiceCallInfoFactoryImpl(PropertyUtils propertyUtils) {
        super();
        this.propertyUtils = propertyUtils;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(ServiceCallApplication.COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    @Override
    public ServiceCallInfo detailed(ServiceCall serviceCall, Map<DefaultState, Long> childrenInformation) {
        ServiceCallInfo serviceCallInfo = summarized(serviceCall);
        Optional<Instant> lastCompletedOptional = serviceCall.getLastCompletedTime();
        if (lastCompletedOptional.isPresent()) {
            serviceCallInfo.lastCompletedTime = lastCompletedOptional.get().toEpochMilli();
        }
        serviceCallInfo.state = toInfo(serviceCall.getState());
        serviceCallInfo.origin = serviceCall.getOrigin().isPresent() ? serviceCall.getOrigin().get() : null;
        Optional<ReferenceInfo> referenceInfo = serviceCall.getTargetObject().isPresent() ? referenceResolver.resolve(serviceCall.getTargetObject().get()) : Optional.empty();
        serviceCallInfo.targetObject = referenceInfo.isPresent() ? referenceInfo.get() : null;
        addCustomPropertySetInfos(serviceCall, serviceCallInfo);
        addParents(serviceCallInfo, serviceCall.getParent());
        addChildrenInfo(serviceCallInfo, childrenInformation);
        return serviceCallInfo;
    }

    @Override
    public ServiceCallInfo summarized(ServiceCall serviceCall) {
        ServiceCallInfo serviceCallInfo = new ServiceCallInfo();
        serviceCallInfo.id = serviceCall.getId();
        serviceCallInfo.name = serviceCall.getNumber();
        serviceCallInfo.version = serviceCall.getVersion();
        serviceCallInfo.creationTime = serviceCall.getCreationTime().toEpochMilli();
        serviceCallInfo.lastModificationTime = serviceCall.getLastModificationTime().toEpochMilli();
        serviceCallInfo.state = toInfo(serviceCall.getState());
        serviceCallInfo.externalReference = serviceCall.getExternalReference()
                .isPresent() ? serviceCall.getExternalReference().get() : null;
        serviceCallInfo.type = serviceCall.getType().getName() + " (" + serviceCall.getType().getVersionName() + ")";
        serviceCallInfo.typeId = serviceCall.getType().getId();
        serviceCallInfo.canCancel = serviceCall.canTransitionTo(DefaultState.CANCELLED);
        return serviceCallInfo;
    }

    @Override
    public ServiceCallFilter convertToServiceCallFilter(JsonQueryFilter filter) {
        ServiceCallFilter serviceCallFilter = new ServiceCallFilterImpl();
        if (filter.hasProperty("name")) {
            serviceCallFilter.setReference(filter.getString("name"));
        }
        if (filter.hasProperty("type")) {
            serviceCallFilter.setTypes(filter.getStringList("type"));
        }
        if (filter.hasProperty("status")) {
            serviceCallFilter.setStates(filter.getStringList("status"));
        }
        if (filter.hasProperty("receivedDateFrom")) {
            serviceCallFilter.setReceivedDateFrom(filter.getInstant("receivedDateFrom"));
        }
        if (filter.hasProperty("receivedDateTo")) {
            serviceCallFilter.setReceivedDateTo(filter.getInstant("receivedDateTo"));
        }
        if (filter.hasProperty("modificationDateFrom")) {
            serviceCallFilter.setModificationDateFrom(filter.getInstant("modificationDateFrom"));
        }
        if (filter.hasProperty("modificationDateTo")) {
            serviceCallFilter.setModificationDateTo(filter.getInstant("modificationDateTo"));
        }

        return serviceCallFilter;
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

    private void addChildrenInfo(ServiceCallInfo serviceCallInfo, Map<DefaultState, Long> childStateCounts) {
        serviceCallInfo.children = new ArrayList<>();
        Long total = childStateCounts.values()
                .stream()
                .reduce(0L, Long::sum);
        serviceCallInfo.numberOfChildren = total;
        Map<DefaultState, Integer> childStatePercentages = childStateCounts.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> (int) Math.round((entry.getValue().doubleValue() / total
                        .doubleValue()) * 100)));
        verifyRoundingErrors(childStatePercentages);
        childStatePercentages.entrySet()
                .stream()
                .forEach(entry -> serviceCallInfo.children.add(new ServiceCallChildrenInfo(entry.getKey().name(),entry.getKey().getDisplayName(thesaurus),entry.getValue(), childStateCounts.get(entry.getKey()))));
    }

    private void verifyRoundingErrors(Map<DefaultState, Integer> childStatePercentages) {
        childStatePercentages.values()
                .stream()
                .reduce(Integer::sum)
                .ifPresent(sum -> handleDifference(childStatePercentages, sum));
    }

    private void handleDifference(Map<DefaultState, Integer> childStatePercentages, Integer sum) {
        int difference = sum - 100;
        if (difference != 0) {
            childStatePercentages.entrySet()
                    .stream()
                    .max(Comparator.comparing(Map.Entry::getValue))
                    .ifPresent(maxEntry -> maxEntry.setValue(maxEntry.getValue() - difference));
        }
    }

    private void addCustomPropertySetInfos(ServiceCall serviceCall, ServiceCallInfo serviceCallInfo) {
        serviceCallInfo.customPropertySets = serviceCall.getType().getCustomPropertySets()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .map(registeredCustomPropertySet -> getServiceCallCustomPropertySetInfo(registeredCustomPropertySet, serviceCall))
                .collect(Collectors.toList());
    }

    private ServiceCallCustomPropertySetInfo getServiceCallCustomPropertySetInfo(RegisteredCustomPropertySet propertySet, ServiceCall serviceCall) {
        CustomPropertySetValues extension = serviceCall.getValuesFor(propertySet.getCustomPropertySet());
        Map<String, Object> values = new HashMap<>();
        extension.propertyNames()
                .stream()
                .forEach(propertyName -> values.put(propertyName, extension.getProperty(propertyName)));
        return new ServiceCallCustomPropertySetInfo(propertySet, propertyUtils.convertPropertySpecsToPropertyInfos(propertySet.getCustomPropertySet().getPropertySpecs(), values));
    }

    private IdWithDisplayValueInfo<String> toInfo(DefaultState state) {
        return new IdWithDisplayValueInfo<>(state.getKey(), state.getDisplayName(thesaurus));
    }
}
