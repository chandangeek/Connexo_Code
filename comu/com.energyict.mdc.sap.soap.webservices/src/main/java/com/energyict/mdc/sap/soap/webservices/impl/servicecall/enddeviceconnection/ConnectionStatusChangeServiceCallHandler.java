/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import org.osgi.service.component.annotations.Component;

import java.text.MessageFormat;

@Component(name = "com.energyict.mdc.sap.servicecall.connectionstatuschange", service = ServiceCallHandler.class,
        property = "name=" + ConnectionStatusChangeServiceCallHandler.NAME, immediate = true)
public class ConnectionStatusChangeServiceCallHandler implements ServiceCallHandler {

    public static final String NAME = "ConnectionStatusChangeServiceCallHandler";
    public static final String VERSION = "v1.0";

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case FAILED:
            case CANCELLED:
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
}