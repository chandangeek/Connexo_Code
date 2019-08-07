/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.measurementtaskassignment;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import org.osgi.service.component.annotations.Component;

@Component(name = MeasurementTaskAssignmentChangeServiceCallHandler.NAME,
        service = ServiceCallHandler.class, immediate = true,
        property = "name=" + MeasurementTaskAssignmentChangeServiceCallHandler.NAME)
public class MeasurementTaskAssignmentChangeServiceCallHandler implements ServiceCallHandler {

    public static final String NAME = "MeasurementTaskAssignmentChangeServiceCallHandler";
    public static final String VERSION = "v1.0";

    public static final String ROLE_INFO_SEPARATOR = ":";
    public static final String ROLE_INFO_PARAMETER_SEPARATOR = ",";

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case ONGOING:
            case SUCCESSFUL:
            case FAILED:
            default:
                // No specific action required for these states
                break;
        }
    }
}
