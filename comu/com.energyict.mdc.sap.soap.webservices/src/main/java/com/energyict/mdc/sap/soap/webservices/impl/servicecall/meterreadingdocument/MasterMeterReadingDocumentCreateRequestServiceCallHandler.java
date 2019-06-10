/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

@Component(name = "MasterMeterReadingDocumentCreateRequestServiceCallHandler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + MasterMeterReadingDocumentCreateRequestServiceCallHandler.NAME)
public class MasterMeterReadingDocumentCreateRequestServiceCallHandler implements ServiceCallHandler {
    public static final String NAME = "MasterMeterReadingDocumentCreateRequestServiceCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MultiSense";

    private volatile ServiceCallService serviceCallService;
    private volatile Thesaurus thesaurus;

    private Map<Long, List<ServiceCall>> immediately = new HashMap<>();
    private Map<Long, List<ServiceCall>> scheduled = new HashMap<>();

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                immediately.remove(serviceCall.getId());
                scheduled.remove(serviceCall.getId());
                serviceCall.findChildren().stream().forEach(child -> child.requestTransition(DefaultState.PENDING));
                break;
            case ONGOING:
                if (oldState.equals(DefaultState.PAUSED)) {
                    serviceCall.findChildren()
                            .stream()
                            .filter(child -> child.getState().isOpen())
                            .forEach(child -> child.requestTransition(DefaultState.ONGOING));
                } else {
                    resultTransition(serviceCall);
                }
                break;
            case CANCELLED:
            case FAILED:
            case PARTIAL_SUCCESS:
            case SUCCESSFUL:
                if (immediately.get(serviceCall.getId()) != null) {
                    createMasterMeterReadingDocumentResultServiceCallForImmediateProcessing(serviceCall);
                }
                if (scheduled.get(serviceCall.getId()) != null) {
                    createMasterMeterReadingDocumentResultServiceCallForScheduling(serviceCall);
                }
                break;
            case SCHEDULED:
                // the recurrent task will take care about, no specific action required
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    @Override
    public void onChildStateChange(ServiceCall parentServiceCall, ServiceCall childServiceCall, DefaultState oldState, DefaultState newState) {
        MeterReadingDocumentCreateRequestDomainExtension requestDomainExtension = childServiceCall.getExtension(MeterReadingDocumentCreateRequestDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
        switch (newState) {
            case SUCCESSFUL:
                if (requestDomainExtension.isFutureCase()) {
                    scheduled.merge(parentServiceCall.getId(), Collections.singletonList(childServiceCall), (list1, list2) ->
                            Stream.of(list1, list2)
                                    .flatMap(Collection::stream)
                                    .collect(Collectors.toList()));
                } else {
                    immediately.merge(parentServiceCall.getId(), Collections.singletonList(childServiceCall), (list1, list2) ->
                            Stream.of(list1, list2)
                                    .flatMap(Collection::stream)
                                    .collect(Collectors.toList()));
                }
                resultTransition(parentServiceCall);
                break;
            case CANCELLED:
            case FAILED:
                immediately.merge(parentServiceCall.getId(), Collections.singletonList(childServiceCall), (list1, list2) ->
                        Stream.of(list1, list2)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList()));
                resultTransition(parentServiceCall);
                break;
            case SCHEDULED:
                parentServiceCall.requestTransition(DefaultState.SCHEDULED);
                break;
            case PAUSED:
                if (!parentServiceCall.getState().equals(DefaultState.PAUSED)) {
                    if (parentServiceCall.canTransitionTo(DefaultState.ONGOING)) {
                        parentServiceCall.requestTransition(DefaultState.ONGOING);
                    }
                    parentServiceCall.requestTransition(DefaultState.PAUSED);
                }
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(WebServiceActivator.COMPONENT_NAME, Layer.SOAP);
    }

    private List<ServiceCall> findChildren(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
    }

    private boolean isLastChild(List<ServiceCall> serviceCalls) {
        return serviceCalls.stream().noneMatch(sc -> sc.getState().isOpen());
    }

    private boolean hasAllChildState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().allMatch(sc -> sc.getState().equals(defaultState));
    }

    private boolean hasAnyChildState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().anyMatch(sc -> sc.getState().equals(defaultState));
    }

    private void resultTransition(ServiceCall parent) {
        List<ServiceCall> children = findChildren(parent);
        if (isLastChild(children)) {
            if (parent.getState().equals(DefaultState.PENDING) && parent.canTransitionTo(DefaultState.ONGOING)) {
                parent.requestTransition(DefaultState.ONGOING);
            } else if (hasAllChildState(children, DefaultState.SUCCESSFUL) && parent.canTransitionTo(DefaultState.SUCCESSFUL)) {
                parent.requestTransition(DefaultState.SUCCESSFUL);
            } else if (hasAnyChildState(children, DefaultState.SUCCESSFUL) && parent.canTransitionTo(DefaultState.PARTIAL_SUCCESS)) {
                parent.requestTransition(DefaultState.PARTIAL_SUCCESS);
            } else if (parent.canTransitionTo(DefaultState.FAILED) && parent.canTransitionTo(DefaultState.FAILED)) {
                parent.requestTransition(DefaultState.FAILED);
            }
        }
    }

    private void createMasterMeterReadingDocumentResultServiceCallForImmediateProcessing(ServiceCall serviceCall) {
        ServiceCall parentServiceCall = createMasterMeterReadingDocumentResultServiceCall(serviceCall);
        parentServiceCall.requestTransition(DefaultState.PENDING);
        immediately.get(serviceCall.getId()).forEach(call -> {
            ServiceCall child = createMeterReadingDocumentResultServiceCall(parentServiceCall, call);
            child.requestTransition(DefaultState.PENDING);
        });
    }

    private void createMasterMeterReadingDocumentResultServiceCallForScheduling(ServiceCall serviceCall) {
        ServiceCall parentServiceCall = createMasterMeterReadingDocumentResultServiceCall(serviceCall);
        parentServiceCall.requestTransition(DefaultState.SCHEDULED);
        scheduled.get(serviceCall.getId()).forEach(call -> {
            ServiceCall child = createMeterReadingDocumentResultServiceCall(parentServiceCall, call);
            child.requestTransition(DefaultState.SCHEDULED);
        });
    }

    private ServiceCall createMasterMeterReadingDocumentResultServiceCall(ServiceCall serviceCall) {
        MasterMeterReadingDocumentCreateRequestDomainExtension extension = serviceCall.getExtension(MasterMeterReadingDocumentCreateRequestDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

        MasterMeterReadingDocumentCreateResultDomainExtension masterDomainExtension = new MasterMeterReadingDocumentCreateResultDomainExtension();
        masterDomainExtension.setRequestUUID(UUID.randomUUID().toString());
        masterDomainExtension.setReferenceID(extension.getRequestID());
        masterDomainExtension.setResultURL(extension.getResultURL());
        masterDomainExtension.setBulk(extension.isBulk());

        ServiceCallBuilder serviceCallBuilder = getServiceCallTypeOrThrowException(ServiceCallTypes.MASTER_METER_READING_DOCUMENT_CREATE_RESULT)
                .newServiceCall()
                .origin(APPLICATION_NAME)
                .extendedWith(masterDomainExtension);
        return serviceCallBuilder.create();
    }

    private ServiceCall createMeterReadingDocumentResultServiceCall(ServiceCall parent, ServiceCall child) {
        MeterReadingDocumentCreateRequestDomainExtension requestDomainExtension = child.getExtension(MeterReadingDocumentCreateRequestDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

        MeterReadingDocumentCreateResultDomainExtension childDomainExtension = new MeterReadingDocumentCreateResultDomainExtension();
        childDomainExtension.setParentServiceCallId(BigDecimal.valueOf(parent.getId()));
        childDomainExtension.setMeterReadingDocumentId(requestDomainExtension.getMeterReadingDocumentId());
        childDomainExtension.setDeviceId(requestDomainExtension.getDeviceId());
        childDomainExtension.setDeviceName(requestDomainExtension.getDeviceName());
        childDomainExtension.setProcessingDate(requestDomainExtension.getProcessingDate());
        childDomainExtension.setReadingReasonCode(requestDomainExtension.getReadingReasonCode());
        childDomainExtension.setChannelId(requestDomainExtension.getChannelId());
        childDomainExtension.setDataSource(requestDomainExtension.getDataSource());
        childDomainExtension.setFutureCase(requestDomainExtension.isFutureCase());
        childDomainExtension.setLrn(requestDomainExtension.getLrn());
        childDomainExtension.setScheduledReadingDate(requestDomainExtension.getScheduledReadingDate());

        ServiceCallType serviceCallType = getServiceCallTypeOrThrowException(ServiceCallTypes.METER_READING_DOCUMENT_CREATE_RESULT);
        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType).extendedWith(childDomainExtension);
        return serviceCallBuilder.create();
    }

    private ServiceCallType getServiceCallTypeOrThrowException(ServiceCallTypes serviceCallType) {
        return serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE)
                        .format(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())));
    }
}