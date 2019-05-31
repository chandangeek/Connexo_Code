/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.energyict.mdc.cim.webservices.inbound.soap.meterreadings.MeterReadingsBuilder;
import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;

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

public class ParentGetMeterReadingsServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "ParentGetMeterReadingsServiceCallHandler";
    /// TODO version update to v2.0 is required?
    public static final String VERSION = "v1.0";

    private final MeteringService meteringService;
    private final SendMeterReadingsProvider sendMeterReadingsProvider;
    private final Provider<MeterReadingsBuilder> readingBuilderProvider;
    private final EndPointConfigurationService endPointConfigurationService;

    @Inject
    public ParentGetMeterReadingsServiceCallHandler(MeteringService meteringService, SendMeterReadingsProvider sendMeterReadingsProvider,
                                                    Provider<MeterReadingsBuilder> readingBuilderProvider,
                                                    EndPointConfigurationService endPointConfigurationService) {
        this.meteringService = meteringService;
        this.sendMeterReadingsProvider = sendMeterReadingsProvider;
        this.readingBuilderProvider = readingBuilderProvider;
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Parent service call is switched to state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
//                serviceCall.findChildren().stream().forEach(child -> child.requestTransition(DefaultState.PENDING));
                break;
            case ONGOING: // normally result collection is performed when state is changed PAUSED --> ONGOING
                if (oldState == DefaultState.PAUSED) {
                    collectAndSendResult(serviceCall);
                }
                break;
            case PARTIAL_SUCCESS:
                collectAndSendResult(serviceCall);
                break;
            case SUCCESSFUL:
            case FAILED:
            default:
                // No specific action required for these states
                break;
        }
    }

    @Override
    public void onChildStateChange(ServiceCall parentServiceCall, ServiceCall subParentServiceCall, DefaultState oldState, DefaultState newState) {
        /// TODO check why log doesn't works
//        subParentServiceCall.log(LogLevel.FINE, "Service call is switched to state " + newState.getDefaultFormat());
        ServiceCallTransitionUtils.resultTransition(parentServiceCall, true);
    }

    private void collectAndSendResult(ServiceCall serviceCall) {
        ParentGetMeterReadingsDomainExtension extension = serviceCall.getExtension(ParentGetMeterReadingsDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
        Instant timePeriodStart = extension.getTimePeriodStart();
        Instant timePeriodEnd = extension.getTimePeriodEnd();
        String readingTypesString = extension.getReadingTypes();
        List<String> endDevicesMRIDs = serviceCall.findChildren().stream()
                .map(c -> c.getExtension(SubParentGetMeterReadingsDomainExtension.class)
                        .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"))
                        .getEndDevice())
                .collect(Collectors.toList());
        String source = extension.getSource();
        String callbackUrl = extension.getCallbackUrl();

        RangeSet<Instant> timeRangeSet =  getTimeRangeSet(timePeriodStart, timePeriodEnd);
        List<EndDevice> endDevices = getEndDevices(endDevicesMRIDs, serviceCall);
        Set<String> readingTypesMRIDs = getReadingTypes(readingTypesString);

        MeterReadingsBuilder meterReadingsBuilder = readingBuilderProvider.get();
        MeterReadings meterReadings = null;
        serviceCall.log(LogLevel.FINE, MessageFormat.format("Result collection is started for source ''{0}'', time range {1}",
                source, timeRangeSet));
        try {
            meterReadings = meterReadingsBuilder.withEndDevices(endDevices)
                    .ofReadingTypesWithMRIDs(readingTypesMRIDs)
                    .inTimeIntervals(timeRangeSet)
                    .build();
        } catch (FaultMessage faultMessage) {
            serviceCall.requestTransition(DefaultState.FAILED);
            serviceCall.log(LogLevel.SEVERE,
                    MessageFormat.format("Unable to collect meter readings for source ''{0}'', time range {1}, du to error: " + faultMessage.getMessage(),
                        source, timeRangeSet));
            return;
        }
        if (meterReadings == null || meterReadings.getMeterReading().isEmpty()) {
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            serviceCall.log(LogLevel.FINE,
                    MessageFormat.format("No meter readings are found for source ''{0}'', time range {1}",
                            source, timeRangeSet));
            return;
        }
        EndPointConfiguration endPointConfiguration = getEndPointConfiguration(serviceCall, callbackUrl);
        if (endPointConfiguration == null) {
            serviceCall.requestTransition(DefaultState.FAILED);
            return;
        }
        boolean isOk = sendMeterReadingsProvider.call(meterReadings, HeaderType.Verb.CREATED, endPointConfiguration);
        if (!isOk) {
            serviceCall.requestTransition(DefaultState.FAILED);
            serviceCall.log(LogLevel.SEVERE,
                    MessageFormat.format("Unable to send meter readings data for source ''{0}'', time range {1}", source, timeRangeSet));
            return;
        }
        serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        serviceCall.log(LogLevel.FINE,
                MessageFormat.format("Data successfully sent for source ''{0}'', time range {1}",
                        source, timeRangeSet));
    }

    private EndPointConfiguration getEndPointConfiguration(ServiceCall serviceCall, String url) {
        EndPointConfiguration endPointConfig = endPointConfigurationService.findEndPointConfigurations()
                .stream()
                .filter(EndPointConfiguration::isActive)
                .filter(endPointConfiguration -> !endPointConfiguration.isInbound())
                .filter(endPointConfiguration -> endPointConfiguration.getUrl().equals(url))
                .findFirst().orElseGet(() -> {
                    serviceCall.log(LogLevel.SEVERE, MessageFormat.format("No end point configuration is found by URL ''{0}''.", url));
                    return null;
                });
        return endPointConfig;
    }

    private RangeSet<Instant> getTimeRangeSet(Instant start, Instant end) {
        RangeSet<Instant> rangeSet = TreeRangeSet.create();
        rangeSet.add(Range.openClosed(start, end));
        return rangeSet;
    }

    private Set<String> getReadingTypes(String readingTypesString) {
        return Arrays.stream(readingTypesString.split(";")).collect(Collectors.toSet());
    }

    private List<EndDevice> getEndDevices(List<String> endDevicesMRIDs, ServiceCall serviceCall) {
        List<EndDevice> existedEndDevices = meteringService.getEndDeviceQuery()
                .select(where("MRID").in(endDevicesMRIDs));
        if (existedEndDevices == null || existedEndDevices.isEmpty()) {
            serviceCall.log(LogLevel.WARNING, "No end devices was found");
        }
        return existedEndDevices;
    }
}
