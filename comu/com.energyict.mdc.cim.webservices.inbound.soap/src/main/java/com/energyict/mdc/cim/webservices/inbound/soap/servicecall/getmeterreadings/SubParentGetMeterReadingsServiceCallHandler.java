/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.cim.webservices.inbound.soap.meterreadings.MeterReadingsBuilder;

import ch.iec.tc57._2011.getmeterreadings.FaultMessage;
import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.schema.message.HeaderType;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import javax.inject.Inject;
import javax.inject.Provider;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class SubParentGetMeterReadingsServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "SubParentGetMeterReadingsServiceCallHandler";
    public static final String VERSION = "v1.0";

    private final MeteringService meteringService;
    private final SendMeterReadingsProvider sendMeterReadingsProvider;
    private final Provider<MeterReadingsBuilder> readingBuilderProvider;
    private final EndPointConfigurationService endPointConfigurationService;

    @Inject
    public SubParentGetMeterReadingsServiceCallHandler(MeteringService meteringService, SendMeterReadingsProvider sendMeterReadingsProvider,
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
     // Do nothing
    }

    @Override
    public void onChildStateChange(ServiceCall subParentServiceCall, ServiceCall childServiceCall, DefaultState oldState, DefaultState newState) {
//        childServiceCall.log(LogLevel.FINE, "Service call is switched to state " + newState.getDefaultFormat());
        ServiceCallTransitionUtils.resultTransition(subParentServiceCall);
    }
}
