/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.sap.soap.webservices.impl.AdditionalProperties;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentCreateResultMessage;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "MasterMeterReadingDocumentCreateResultServiceCallHandler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + MasterMeterReadingDocumentCreateResultServiceCallHandler.NAME)
public class MasterMeterReadingDocumentCreateResultServiceCallHandler implements ServiceCallHandler {

    public static final String NAME = "MasterMeterReadingDocumentCreateResultServiceCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private volatile Clock clock;

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case ONGOING:
                if (!oldState.equals(DefaultState.WAITING)) {
                    sendResultMessage(serviceCall);
                    setConfirmationTime(serviceCall);
                    serviceCall.requestTransition(DefaultState.WAITING);
                }
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    @Override
    public void onChildStateChange(ServiceCall parentServiceCall, ServiceCall childServiceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case FAILED:
            case SUCCESSFUL:
            case CANCELLED:
            case WAITING:
                List<ServiceCall> children = findChildren(parentServiceCall);
                if (isLastClosedChild(children)) {
                    if (allChildCancelledBySap(children) && parentServiceCall.canTransitionTo(DefaultState.CANCELLED)) {
                        parentServiceCall.log(LogLevel.INFO, "All orders are cancelled by SAP.");
                        parentServiceCall.requestTransition(DefaultState.CANCELLED);
                    } else if (parentServiceCall.getState().equals(DefaultState.WAITING)) {
                        parentServiceCall.requestTransition(DefaultState.ONGOING);
                        resultTransition(parentServiceCall, children);
                    }
                } else if (isLastWaitingOrCancelledChild(children)) {
                    if (parentServiceCall.getState().equals(DefaultState.PENDING)) {
                        parentServiceCall.requestTransition(DefaultState.ONGOING);
                    } else if (parentServiceCall.getState().equals(DefaultState.SCHEDULED)) {
                        parentServiceCall.requestTransition(DefaultState.PENDING);
                        parentServiceCall.requestTransition(DefaultState.ONGOING);
                    }
                }
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    private void resultTransition(ServiceCall parent, List<ServiceCall> children) {
        if (hasAllChildrenInState(children, DefaultState.SUCCESSFUL) && parent.canTransitionTo(DefaultState.SUCCESSFUL)) {
            parent.requestTransition(DefaultState.SUCCESSFUL);
        } else if (hasAnyChildState(children, DefaultState.CANCELLED) && parent.canTransitionTo(DefaultState.CANCELLED)) {
            parent.requestTransition(DefaultState.CANCELLED);
        } else if (hasAnyChildState(children, DefaultState.SUCCESSFUL) && parent.canTransitionTo(DefaultState.PARTIAL_SUCCESS)) {
            parent.requestTransition(DefaultState.PARTIAL_SUCCESS);
        } else if (parent.canTransitionTo(DefaultState.FAILED)) {
            parent.requestTransition(DefaultState.FAILED);
        }
    }

    private List<ServiceCall> findChildren(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
    }

    private boolean hasAllChildrenInState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().allMatch(sc -> sc.getState().equals(defaultState));
    }

    private boolean hasAnyChildState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().anyMatch(sc -> sc.getState().equals(defaultState));
    }

    private boolean isLastClosedChild(List<ServiceCall> serviceCalls) {
        return serviceCalls.stream().noneMatch(sc -> sc.getState().isOpen());
    }

    private boolean isLastWaitingOrCancelledChild(List<ServiceCall> serviceCalls) {
        return serviceCalls
                .stream()
                .allMatch(sc -> sc.getState().equals(DefaultState.WAITING) || sc.getState().equals(DefaultState.CANCELLED));
    }

    private void sendResultMessage(ServiceCall serviceCall) {
        MeterReadingDocumentCreateResultMessage resultMessage = MeterReadingDocumentCreateResultMessage
                .builder()
                .from(serviceCall, findChildren(serviceCall), clock.instant())
                .build();

        int childrenTotal = resultMessage.getDocumentsTotal();
        int childrenCanceledBySap = resultMessage.getDocumentsCanceledBySap();
        int childrenSuccessfullyProcessed = resultMessage.getDocumentsSuccessfullyProcessed();
        serviceCall.log(LogLevel.INFO, "Total orders: " + childrenTotal +
                ". Successfully processed orders: " + childrenSuccessfullyProcessed +
                ". Failed processed orders: " + (childrenTotal - childrenSuccessfullyProcessed - childrenCanceledBySap) +
                ". Cancelled by Sap orders: " + childrenCanceledBySap + ".");

        serviceCall.log(LogLevel.INFO, "Sent the results to Sap.");

        if (resultMessage.isBulk()) {
            WebServiceActivator.METER_READING_DOCUMENT_BULK_RESULTS.forEach(sender -> sender.call(resultMessage));
        } else {
            WebServiceActivator.METER_READING_DOCUMENT_RESULTS.forEach(sender -> sender.call(resultMessage));
        }
    }

    private void setConfirmationTime(ServiceCall serviceCall) {
        MasterMeterReadingDocumentCreateResultDomainExtension masterExtension = serviceCall.getExtensionFor(new MasterMeterReadingDocumentCreateResultCustomPropertySet()).get();
        Integer interval = WebServiceActivator.SAP_PROPERTIES.get(AdditionalProperties.CONFIRMATION_TIMEOUT); // in mins
        masterExtension.setConfirmationTime(clock.instant().plusSeconds(interval * 60));
        serviceCall.update(masterExtension);
    }

    private boolean allChildCancelledBySap(List<ServiceCall> children) {
        return children.stream().allMatch(sc -> isServiceCallCancelledBySap(sc));
    }

    private boolean isServiceCallCancelledBySap(ServiceCall sc) {
        Optional<MeterReadingDocumentCreateRequestDomainExtension> extension = sc.getExtension(MeterReadingDocumentCreateRequestDomainExtension.class);
        if (extension.isPresent()) {
            return extension.get().isCancelledBySap();
        } else {
            return false;
        }
    }
}

