/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.cim.webservices.inbound.soap.meterreadings.MeterReadingsBuilder;

import javax.inject.Inject;
import javax.inject.Provider;

public class ComTaskExecutionServiceCallHandler implements ServiceCallHandler {

    public static final String VERSION = "v1.0";
    public static final String SERVICE_CALL_HANDLER_NAME = "ComTaskExecutionServiceCallHandler";

    private final MeteringService meteringService;
    private final SendMeterReadingsProvider sendMeterReadingsProvider;
    private final Provider<MeterReadingsBuilder> readingBuilderProvider;
    private final EndPointConfigurationService endPointConfigurationService;

    @Inject
    public ComTaskExecutionServiceCallHandler(MeteringService meteringService, SendMeterReadingsProvider sendMeterReadingsProvider,
                                              Provider<MeterReadingsBuilder> readingBuilderProvider,
                                              EndPointConfigurationService endPointConfigurationService) {
        this.meteringService = meteringService;
        this.sendMeterReadingsProvider = sendMeterReadingsProvider;
        this.readingBuilderProvider = readingBuilderProvider;
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Service call is switched to state " + newState.getDefaultFormat());
        // do nothing
//        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
//        switch (newState) {
//            case SUCCESSFUL:
//                // Do Nothing
//                break;
//            case FAILED:
//            case PARTIAL_SUCCESS:
//            case CANCELLED:
//                // Do Nothing
//                break;
//            default:
//                // No specific action required for these states
//                break;
//        }
    }
}