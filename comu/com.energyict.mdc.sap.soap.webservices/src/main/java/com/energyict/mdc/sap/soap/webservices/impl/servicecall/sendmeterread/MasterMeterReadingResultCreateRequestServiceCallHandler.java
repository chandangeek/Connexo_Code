/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.sendmeterread;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.sendmeterread.MeterReadingResultCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.util.List;

@Component(name = MasterMeterReadingResultCreateRequestServiceCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + MasterMeterReadingResultCreateRequestServiceCallHandler.NAME, immediate = true)
public class MasterMeterReadingResultCreateRequestServiceCallHandler implements ServiceCallHandler {

    public static final String NAME = "MasterMeterReadingResultCreateRequest";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private volatile WebServiceActivator webServiceActivator;
    private volatile Clock clock;


    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                serviceCall.findChildren().stream().forEach(child -> {
                    if (child.canTransitionTo(DefaultState.PENDING)) {
                        child.requestTransition(DefaultState.PENDING);
                    }
                });
                break;
            case ONGOING:
                resultTransition(serviceCall);
                break;
            case CANCELLED:
            case FAILED:
            case PARTIAL_SUCCESS:
            case SUCCESSFUL:
                sendResultMessage(serviceCall);
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
                if (ServiceCallHelper.isLastChild(ServiceCallHelper.findChildren(parentServiceCall))) {
                    if (parentServiceCall.getState().equals(DefaultState.PENDING)) {
                        parentServiceCall.requestTransition(DefaultState.ONGOING);
                    }
                }
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    private void resultTransition(ServiceCall parent) {
        List<ServiceCall> children = ServiceCallHelper.findChildren(parent);
        if (ServiceCallHelper.isLastChild(children)) {
            if (parent.getState().equals(DefaultState.PENDING) && parent.canTransitionTo(DefaultState.ONGOING)) {
                parent.requestTransition(DefaultState.ONGOING);
            } else if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.SUCCESSFUL) && parent.canTransitionTo(DefaultState.SUCCESSFUL)) {
                parent.requestTransition(DefaultState.SUCCESSFUL);
            } else if (parent.canTransitionTo(DefaultState.FAILED)) {
                parent.requestTransition(DefaultState.FAILED);
            }
        }
    }


    private void sendResultMessage(ServiceCall serviceCall) {
        MeterReadingResultCreateConfirmationMessage resultMessage = MeterReadingResultCreateConfirmationMessage
                .builder()
                .from(serviceCall, webServiceActivator.getMeteringSystemId(), clock.instant())
                .build();
        WebServiceActivator.METER_READING_RESULT_CREATE_CONFIRMATIONS.forEach(sender -> sender.call(resultMessage));
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }

}
