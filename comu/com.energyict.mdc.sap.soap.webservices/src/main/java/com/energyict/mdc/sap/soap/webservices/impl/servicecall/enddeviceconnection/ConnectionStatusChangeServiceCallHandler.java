/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestBulkCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.text.MessageFormat;
import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

@Component(name = "com.energyict.mdc.sap.servicecall.connectionstatuschange", service = ServiceCallHandler.class,
        property = "name=" + ConnectionStatusChangeServiceCallHandler.NAME, immediate = true)
public class ConnectionStatusChangeServiceCallHandler implements ServiceCallHandler {

    public static final String NAME = "ConnectionStatusChangeServiceCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private volatile Clock clock;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile WebServiceActivator webServiceActivator;

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case CANCELLED:
                sendConfirmation(serviceCall);
                break;
            case FAILED:
            case PARTIAL_SUCCESS:
            case SUCCESSFUL:
                serviceCall.log(LogLevel.INFO, "All child service call operations have been executed");
            default:
                break;
        }
    }

    public void onChildStateChange(ServiceCall parent, ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case CANCELLED:
            case FAILED:
            case SUCCESSFUL:
                parent.log(LogLevel.INFO, MessageFormat.format("Service call {0} (type={1}) was " +
                        newState.getDefaultFormat().toLowerCase(), serviceCall.getId(), serviceCall.getType().getName()));
            default:
                break;
        }
    }

    private void sendConfirmation(ServiceCall parent) {
        ConnectionStatusChangeDomainExtension extension = parent.getExtensionFor(new ConnectionStatusChangeCustomPropertySet()).get();

        if (!extension.isCancelledBySap()) {
            if (extension.isBulk()) {
                StatusChangeRequestBulkCreateConfirmationMessage responseMessage = StatusChangeRequestBulkCreateConfirmationMessage
                        .builder(sapCustomPropertySets)
                        .from(parent, ServiceCallHelper.findChildren(parent), webServiceActivator.getMeteringSystemId(), clock.instant())
                        .build();

                WebServiceActivator.STATUS_CHANGE_REQUEST_BULK_CREATE_CONFIRMATIONS.forEach(sender -> sender.call(responseMessage, parent));
            } else {
                StatusChangeRequestCreateConfirmationMessage responseMessage = StatusChangeRequestCreateConfirmationMessage
                        .builder(sapCustomPropertySets)
                        .from(parent, ServiceCallHelper.findChildren(parent), webServiceActivator.getMeteringSystemId(), clock.instant())
                        .build();

                WebServiceActivator.STATUS_CHANGE_REQUEST_CREATE_CONFIRMATIONS.forEach(sender -> sender.call(responseMessage, parent));
            }
        }
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }
}