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
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentRequestConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

@Component(name = "MasterMeterReadingDocumentCreateRequestServiceCallHandler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + MasterMeterReadingDocumentCreateRequestServiceCallHandler.NAME)
public class MasterMeterReadingDocumentCreateRequestServiceCallHandler implements ServiceCallHandler {
    public static final String NAME = "MasterMeterReadingDocumentCreateRequestServiceCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private volatile ServiceCallService serviceCallService;
    private volatile Thesaurus thesaurus;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile WebServiceActivator webServiceActivator;
    private volatile Clock clock;

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                MasterMeterReadingDocumentCreateRequestDomainExtension masterExtension = serviceCall
                        .getExtension(MasterMeterReadingDocumentCreateRequestDomainExtension.class)
                        .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
                masterExtension.setAttemptNumber(masterExtension.getAttemptNumber().add(BigDecimal.ONE));
                serviceCall.update(masterExtension);
                serviceCall.findChildren().stream().forEach(child -> child.transitionWithLockIfPossible(DefaultState.PENDING));
                break;
            case ONGOING:
                if (oldState.equals(DefaultState.PAUSED)) {
                    List<ServiceCall> openChildren = serviceCall.findChildren()
                            .stream()
                            .filter(child -> child.getState().isOpen())
                            .collect(Collectors.toList());
                    if (openChildren.isEmpty()) {
                        resultTransition(serviceCall);
                    } else {
                        openChildren.forEach(child -> child.requestTransition(DefaultState.ONGOING));
                    }
                } else {
                    resultTransition(serviceCall);
                }
                break;
            case CANCELLED:
            case FAILED:
            case PARTIAL_SUCCESS:
            case SUCCESSFUL:
                sendConfirmationMessage(serviceCall);
                if (allChildrenCancelledBySap(serviceCall)) {
                    serviceCall.log(LogLevel.INFO, "All orders are cancelled by SAP.");
                } else {
                    //if not continue to create result service calls
                    createResultServiceCalls(serviceCall);
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
                resultTransition(parentServiceCall);
                break;
            case CANCELLED:
            case FAILED:
                resultTransition(parentServiceCall);
                break;
            case SCHEDULED:
                parentServiceCall = lock(parentServiceCall);
                parentServiceCall.requestTransition(DefaultState.SCHEDULED);
                break;
            case PAUSED:
                parentServiceCall = lock(parentServiceCall);
                List<ServiceCall> children = findChildren(parentServiceCall);
                if (ServiceCallHelper.isLastPausedChild(children)) {
                    if (!parentServiceCall.getState().equals(DefaultState.PAUSED)) {
                        if (parentServiceCall.canTransitionTo(DefaultState.ONGOING)) {
                            parentServiceCall.requestTransition(DefaultState.ONGOING);
                        }
                        parentServiceCall.requestTransition(DefaultState.PAUSED);
                    }
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

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public final void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    private List<ServiceCall> findChildren(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
    }

    private void resultTransition(ServiceCall parent) {
        parent = lock(parent);
        List<ServiceCall> children = ServiceCallHelper.findChildren(parent);
        if (ServiceCallHelper.isLastPausedChild(children)) {
            if (parent.getState().equals(DefaultState.PENDING) && parent.canTransitionTo(DefaultState.ONGOING)) {
                parent.requestTransition(DefaultState.ONGOING);
            } else if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.SUCCESSFUL) && parent.canTransitionTo(DefaultState.SUCCESSFUL)) {
                parent.requestTransition(DefaultState.SUCCESSFUL);
            } else if (ServiceCallHelper.hasAnyChildState(children, DefaultState.PAUSED)) {
                if (parent.canTransitionTo(DefaultState.PAUSED)) {
                    parent.requestTransition(DefaultState.PAUSED);
                }
            } else if (ServiceCallHelper.hasAnyChildState(children, DefaultState.SUCCESSFUL) && parent.canTransitionTo(DefaultState.PARTIAL_SUCCESS)) {
                parent.requestTransition(DefaultState.PARTIAL_SUCCESS);
            } else if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.CANCELLED) && parent.canTransitionTo(DefaultState.CANCELLED)) {
                parent.requestTransition(DefaultState.CANCELLED);
            } else if (parent.canTransitionTo(DefaultState.FAILED)) {
                parent.requestTransition(DefaultState.FAILED);
            } else if (parent.canTransitionTo(DefaultState.ONGOING)) {
                parent.requestTransition(DefaultState.ONGOING);
            }
        }
    }

    private void createResultServiceCalls(ServiceCall serviceCall) {
        Set<ServiceCall> immediately = new HashSet<>();
        Set<ServiceCall> scheduled = new HashSet<>();
        ServiceCallFilter serviceCallFilter = new ServiceCallFilter();

        serviceCallFilter.states.add(DefaultState.SUCCESSFUL.name());
        serviceCall.findChildren(serviceCallFilter).stream().forEach(childServiceCall -> {
            MeterReadingDocumentCreateRequestDomainExtension requestDomainExtension = childServiceCall.getExtension(MeterReadingDocumentCreateRequestDomainExtension.class)
                    .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
            if (requestDomainExtension.isFutureCase()) {
                scheduled.add(childServiceCall);
            } else {
                immediately.add(childServiceCall);
            }
        });

        if (!immediately.isEmpty()) {
            createMasterMeterReadingDocumentResultServiceCallForImmediateProcessing(serviceCall, immediately);
        }

        if (!scheduled.isEmpty()) {
            createMasterMeterReadingDocumentResultServiceCallForScheduling(serviceCall, scheduled);
        }
    }

    private void createMasterMeterReadingDocumentResultServiceCallForImmediateProcessing(ServiceCall serviceCall, Set<ServiceCall> immediately) {
        ServiceCall parentServiceCall = createMasterMeterReadingDocumentResultServiceCall(serviceCall);
        parentServiceCall.requestTransition(DefaultState.PENDING);
        immediately.forEach(call -> createMeterReadingDocumentResultServiceCall(parentServiceCall, call)
                .ifPresent(child -> child.transitionWithLockIfPossible(DefaultState.PENDING))
        );

        if (parentServiceCall.findChildren().paged(0, 0).find().isEmpty()) {
            parentServiceCall.requestTransition(DefaultState.CANCELLED);
            parentServiceCall.log(LogLevel.SEVERE, "Parent service call doesn't have children");
        }
    }

    private void createMasterMeterReadingDocumentResultServiceCallForScheduling(ServiceCall serviceCall, Set<ServiceCall> scheduled) {
        ServiceCall parentServiceCall = createMasterMeterReadingDocumentResultServiceCall(serviceCall);
        parentServiceCall.requestTransition(DefaultState.SCHEDULED);
        scheduled.forEach(call -> createMeterReadingDocumentResultServiceCall(parentServiceCall, call)
                .ifPresent(child -> child.transitionWithLockIfPossible(DefaultState.SCHEDULED))
        );

        if (parentServiceCall.findChildren().paged(0, 0).find().isEmpty()) {
            parentServiceCall.requestTransition(DefaultState.CANCELLED);
            parentServiceCall.log(LogLevel.SEVERE, "Parent service call doesn't have children");
        }
    }

    private ServiceCall createMasterMeterReadingDocumentResultServiceCall(ServiceCall serviceCall) {
        MasterMeterReadingDocumentCreateRequestDomainExtension extension = serviceCall.getExtension(MasterMeterReadingDocumentCreateRequestDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

        MasterMeterReadingDocumentCreateResultDomainExtension masterDomainExtension = new MasterMeterReadingDocumentCreateResultDomainExtension();
        masterDomainExtension.setRequestUUID(UUID.randomUUID().toString());
        masterDomainExtension.setReferenceID(extension.getRequestID());
        masterDomainExtension.setReferenceUuid(extension.getUuid());
        masterDomainExtension.setBulk(extension.isBulk());

        ServiceCallBuilder serviceCallBuilder = getServiceCallTypeOrThrowException(ServiceCallTypes.MASTER_METER_READING_DOCUMENT_CREATE_RESULT)
                .newServiceCall()
                .origin(APPLICATION_NAME)
                .extendedWith(masterDomainExtension);
        return serviceCallBuilder.create();
    }

    private Optional<ServiceCall> createMeterReadingDocumentResultServiceCall(ServiceCall parent, ServiceCall child) {
        child = lock(child);
        MeterReadingDocumentCreateRequestDomainExtension requestDomainExtension = child.getExtension(MeterReadingDocumentCreateRequestDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

        if (!requestDomainExtension.isCancelledBySap()) {
            MeterReadingDocumentCreateResultDomainExtension childDomainExtension = new MeterReadingDocumentCreateResultDomainExtension();
            childDomainExtension.setParentServiceCallId(BigDecimal.valueOf(parent.getId()));
            childDomainExtension.setMeterReadingDocumentId(requestDomainExtension.getMeterReadingDocumentId());
            childDomainExtension.setDeviceId(requestDomainExtension.getDeviceId());
            childDomainExtension.setDeviceName(requestDomainExtension.getDeviceName());
            childDomainExtension.setProcessingDate(requestDomainExtension.getProcessingDate());
            childDomainExtension.setReadingReasonCode(requestDomainExtension.getReadingReasonCode());
            childDomainExtension.setDataSourceTypeCode(requestDomainExtension.getDataSourceTypeCode());
            childDomainExtension.setChannelId(requestDomainExtension.getChannelId());
            childDomainExtension.setDataSource(requestDomainExtension.getDataSource());
            childDomainExtension.setExtraDataSource(requestDomainExtension.getExtraDataSource());
            childDomainExtension.setFutureCase(requestDomainExtension.isFutureCase());
            childDomainExtension.setLrn(requestDomainExtension.getLrn());
            childDomainExtension.setRequestedScheduledReadingDate(requestDomainExtension.getRequestedScheduledReadingDate());
            childDomainExtension.setScheduledReadingDate(requestDomainExtension.getScheduledReadingDate());
            childDomainExtension.setReferenceID(requestDomainExtension.getReferenceID());
            childDomainExtension.setReferenceUuid(requestDomainExtension.getReferenceUuid());

            ServiceCallType serviceCallType = getServiceCallTypeOrThrowException(ServiceCallTypes.METER_READING_DOCUMENT_CREATE_RESULT);
            ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType).extendedWith(childDomainExtension);
            sapCustomPropertySets.getDevice(requestDomainExtension.getDeviceId()).ifPresent(serviceCallBuilder::targetObject);
            return Optional.of(serviceCallBuilder.create());
        } else {
            return Optional.empty();
        }
    }

    private ServiceCallType getServiceCallTypeOrThrowException(ServiceCallTypes serviceCallType) {
        return serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE)
                        .format(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())));
    }

    private boolean allChildrenCancelledBySap(ServiceCall serviceCall) {
        List<ServiceCall> children = findChildren(serviceCall);
        return children.stream().allMatch(this::isServiceCallCancelledBySap);
    }

    private boolean isServiceCallCancelledBySap(ServiceCall sc) {
        Optional<MeterReadingDocumentCreateRequestDomainExtension> extension = sc.getExtension(MeterReadingDocumentCreateRequestDomainExtension.class);
        return extension.map(MeterReadingDocumentCreateRequestDomainExtension::isCancelledBySap).orElse(false);
    }

    private ServiceCall lock(ServiceCall serviceCall) {
        return serviceCallService.lockServiceCall(serviceCall.getId())
                .orElseThrow(() -> new IllegalStateException("Service call " + serviceCall.getNumber() + " disappeared."));
    }

    private void sendConfirmationMessage(ServiceCall serviceCall) {
        MasterMeterReadingDocumentCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new MasterMeterReadingDocumentCreateRequestCustomPropertySet()).get();
        MeterReadingDocumentRequestConfirmationMessage confirmationMessage = MeterReadingDocumentRequestConfirmationMessage
                .builder()
                .from(extension, findChildren(serviceCall), clock.instant(), webServiceActivator.getMeteringSystemId())
                .build();

        if (extension.isBulk()) {
            WebServiceActivator.METER_READING_DOCUMENT_BULK_REQUEST_CONFIRMATIONS
                    .forEach(service -> service.call(confirmationMessage));
        } else {
            WebServiceActivator.METER_READING_DOCUMENT_REQUEST_CONFIRMATIONS
                    .forEach(service -> service.call(confirmationMessage));
        }
    }
}