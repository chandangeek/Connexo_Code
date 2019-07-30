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
                if (isLastChild(findChildren(parentServiceCall))) {
                    if (parentServiceCall.getState().equals(DefaultState.PENDING)) {
                        parentServiceCall.requestTransition(DefaultState.ONGOING);
                    } else if (parentServiceCall.getState().equals(DefaultState.SCHEDULED)) {
                        parentServiceCall.requestTransition(DefaultState.PENDING);
                        parentServiceCall.requestTransition(DefaultState.ONGOING);
                    }
                }
            default:
                // No specific action required for these states
                break;
        }
    }

    private List<ServiceCall> findChildren(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
    }

    private boolean isLastChild(List<ServiceCall> serviceCalls) {
        return serviceCalls
                .stream()
                .allMatch(sc -> sc.getState().equals(DefaultState.FAILED) || sc.getState().equals(DefaultState.SUCCESSFUL));
    }

    private void sendResultMessage(ServiceCall serviceCall) {
        MeterReadingDocumentCreateResultMessage resultMessage = MeterReadingDocumentCreateResultMessage
                .builder()
                .from(serviceCall, findChildren(serviceCall))
                .build();
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
}

