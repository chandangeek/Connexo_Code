/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
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
    private volatile ServiceCallService serviceCallService;
    private volatile WebServiceActivator webServiceActivator;

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public final void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case ONGOING:
                if (!oldState.equals(DefaultState.WAITING)) {
                    if (sendResultMessage(serviceCall)) {
                        setConfirmationTime(serviceCall);
                        serviceCall.requestTransition(DefaultState.WAITING);
                    }
                }
                break;
            case CANCELLED:
                if (!isConfirmationTimeAlreadySet(serviceCall)) {
                    List<ServiceCall> children = findChildren(serviceCall);
                    if (!areAllCancelledBySap(children)) {
                        sendResultMessage(serviceCall);
                    }
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
                    parentServiceCall = lock(parentServiceCall);
                    if (areAllCancelledBySap(children) && parentServiceCall.canTransitionTo(DefaultState.CANCELLED)) {
                        parentServiceCall.log(LogLevel.INFO, "All orders are cancelled by SAP.");
                        parentServiceCall.requestTransition(DefaultState.CANCELLED);
                    } else if (parentServiceCall.getState().equals(DefaultState.WAITING)) {
                        parentServiceCall.requestTransition(DefaultState.ONGOING);
                        resultTransition(parentServiceCall, children);
                    } else if (parentServiceCall.getState().equals(DefaultState.PENDING)) {
                        parentServiceCall.requestTransition(DefaultState.ONGOING);
                        // All children are closed - close parent even if it was still pending
                        resultTransition(parentServiceCall, children);
                    } else if (parentServiceCall.getState().equals(DefaultState.SCHEDULED)) {
                        parentServiceCall.requestTransition(DefaultState.PENDING);
                        parentServiceCall.requestTransition(DefaultState.ONGOING);
                        // All children are closed - close parent even if it was still scheduled
                        resultTransition(parentServiceCall, children);
                    }
                } else if (areAllWaitingOrCancelled(children)) {
                    parentServiceCall = lock(parentServiceCall);
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
        long confirmedOrders = children.stream().filter(sc -> sc.getState().equals(DefaultState.SUCCESSFUL)).count();
        parent.log(LogLevel.INFO, "Orders successfully confirmed by SAP: " + confirmedOrders + ".");

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

    private boolean areAllWaitingOrCancelled(List<ServiceCall> serviceCalls) {
        return serviceCalls
                .stream()
                .allMatch(sc -> sc.getState().equals(DefaultState.WAITING) || sc.getState().equals(DefaultState.CANCELLED));
    }

    private boolean sendResultMessage(ServiceCall serviceCall) {
        MeterReadingDocumentCreateResultMessage resultMessage = MeterReadingDocumentCreateResultMessage
                .builder()
                .from(serviceCall, findChildren(serviceCall), clock.instant(), webServiceActivator.getMeteringSystemId())
                .build();

        if (resultMessage.isBulk()) {
            if (!resultMessage.getBulkResultMessage().isPresent()) {
                return false;
            } else if (resultMessage.getBulkResultMessage().get().getMeterReadingDocumentERPResultCreateRequestMessage().isEmpty()) {
                return false;
            }
        }

        if (!resultMessage.isBulk()) {
            if (!resultMessage.getResultMessage().isPresent()) {
                return false;
            } else if (resultMessage.getResultMessage().get().getMeterReadingDocument() == null) {
                return false;
            }
        }

        int childrenTotal = resultMessage.getDocumentsTotal();
        int childrenCanceledBySap = resultMessage.getDocumentsCancelledBySap();
        int childrenSuccessfullyProcessed = resultMessage.getDocumentsSuccessfullyProcessed();
        serviceCall.log(LogLevel.INFO, "Total orders: " + childrenTotal +
                ", successfully processed orders: " + childrenSuccessfullyProcessed +
                ", failed orders: " + (childrenTotal - childrenSuccessfullyProcessed - childrenCanceledBySap) +
                ", orders cancelled by SAP: " + childrenCanceledBySap + ".");

        serviceCall.log(LogLevel.INFO, "Sent the results to Sap.");

        if (resultMessage.isBulk()) {
            WebServiceActivator.METER_READING_DOCUMENT_BULK_RESULTS.forEach(sender -> sender.call(resultMessage));
        } else {
            WebServiceActivator.METER_READING_DOCUMENT_RESULTS.forEach(sender -> sender.call(resultMessage));
        }
        return true;
    }

    private void setConfirmationTime(ServiceCall serviceCall) {
        MasterMeterReadingDocumentCreateResultDomainExtension masterExtension = serviceCall.getExtensionFor(new MasterMeterReadingDocumentCreateResultCustomPropertySet()).get();
        Integer interval = webServiceActivator.getSapProperty(AdditionalProperties.CONFIRMATION_TIMEOUT); // in mins
        masterExtension.setConfirmationTime(clock.instant().plusSeconds(interval * 60));
        serviceCall.update(masterExtension);
    }

    private boolean isConfirmationTimeAlreadySet(ServiceCall serviceCall) {
        MasterMeterReadingDocumentCreateResultDomainExtension masterExtension = serviceCall.getExtensionFor(new MasterMeterReadingDocumentCreateResultCustomPropertySet()).get();
        return masterExtension.getConfirmationTime() != null;
    }

    private boolean areAllCancelledBySap(List<ServiceCall> children) {
        return children.stream().allMatch(sc -> isServiceCallCancelledBySap(sc));
    }

    private boolean isServiceCallCancelledBySap(ServiceCall sc) {
        Optional<MeterReadingDocumentCreateResultDomainExtension> extension = sc.getExtension(MeterReadingDocumentCreateResultDomainExtension.class);
        if (extension.isPresent()) {
            return extension.get().isCancelledBySap();
        } else {
            return false;
        }
    }

    private ServiceCall lock(ServiceCall serviceCall) {
        return serviceCallService.lockServiceCall(serviceCall.getId())
                .orElseThrow(() -> new IllegalStateException("Service call " + serviceCall.getNumber() + " disappeared."));
    }
}

