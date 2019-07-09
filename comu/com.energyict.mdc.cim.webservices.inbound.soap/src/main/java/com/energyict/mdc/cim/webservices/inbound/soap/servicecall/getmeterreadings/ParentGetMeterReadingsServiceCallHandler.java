/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.energyict.mdc.cim.webservices.inbound.soap.meterreadings.MeterReadingsBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.NoSuchElementException;
import com.energyict.mdc.masterdata.MasterDataService;
import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.servicecall.DefaultState.CANCELLED;
import static com.elster.jupiter.servicecall.DefaultState.FAILED;
import static com.elster.jupiter.servicecall.DefaultState.SUCCESSFUL;
import static com.elster.jupiter.util.conditions.Where.where;

public class ParentGetMeterReadingsServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "ParentGetMeterReadingsServiceCallHandler";
    public static final String VERSION = "v1.0";

    private final MeteringService meteringService;
    private final SendMeterReadingsProvider sendMeterReadingsProvider;
    private final Provider<MeterReadingsBuilder> readingBuilderProvider;
    private final EndPointConfigurationService endPointConfigurationService;
    private final DeviceService deviceService;
    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();

    @Inject
    public ParentGetMeterReadingsServiceCallHandler(MeteringService meteringService, SendMeterReadingsProvider sendMeterReadingsProvider,
                                                    Provider<MeterReadingsBuilder> readingBuilderProvider,
                                                    EndPointConfigurationService endPointConfigurationService,
                                                    DeviceService deviceService) {
        this.meteringService = meteringService;
        this.sendMeterReadingsProvider = sendMeterReadingsProvider;
        this.readingBuilderProvider = readingBuilderProvider;
        this.endPointConfigurationService = endPointConfigurationService;
        this.deviceService = deviceService;
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
                processChilds(serviceCall, SUCCESSFUL);
                break;
            case FAILED: // no break expressly
            case CANCELLED:
                processChilds(serviceCall, CANCELLED);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    @Override
    public void onChildStateChange(ServiceCall parentServiceCall, ServiceCall subParentServiceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case SUCCESSFUL:
            case PARTIAL_SUCCESS:
            case CANCELLED:
            case FAILED:
                switch (parentServiceCall.getState()) {
                    case PENDING:
                    case ONGOING:
                    case WAITING:
                         ServiceCallTransitionUtils.resultTransition(parentServiceCall, true);
                         break;
            }
                break;
        }
    }

    private void processChilds(ServiceCall parentServiceCall, DefaultState newState) {
        parentServiceCall.findChildren().stream()
                .filter(subParentServiceCall -> subParentServiceCall.getState() != newState)
                .forEach(subParentServiceCall -> subParentServiceCall.requestTransition(newState));
    }

    private void collectAndSendResult(ServiceCall serviceCall) {
        ParentGetMeterReadingsDomainExtension extension = serviceCall.getExtension(ParentGetMeterReadingsDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
        Instant timePeriodStart = extension.getTimePeriodStart();
        Instant timePeriodEnd = extension.getTimePeriodEnd();
        String readingTypesString = extension.getReadingTypes();
        String loadProfilesString = extension.getLoadProfiles();
        String registerGroupsString = extension.getRegisterGroups();
        String correlationId = extension.getCorelationId();
        List<String> endDevicesMRIDs = serviceCall.findChildren().stream()
                .map(c -> c.getExtension(SubParentGetMeterReadingsDomainExtension.class)
                        .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"))
                        .getEndDevice())
                .collect(Collectors.toList());
        String source = extension.getSource();
        String callbackUrl = extension.getCallbackUrl();

        if (timePeriodEnd == null) {
            timePeriodEnd = calculateEndDateFromChildrenServiceCall(serviceCall);
        }
        RangeSet<Instant> timeRangeSet = null;
        if (timePeriodStart != null) {
            timeRangeSet = getTimeRangeSet(timePeriodStart, timePeriodEnd);
        }
        Set<Meter> endDevices = getEndDevices(endDevicesMRIDs, serviceCall);
        Set<String> readingTypesMRIDs = getSetOfValuesFromString(readingTypesString);
        Set<String> loadProfilesNames = getSetOfValuesFromString(loadProfilesString);
        Set<String> registerGroupsNames = getSetOfValuesFromString(registerGroupsString);

        MeterReadingsBuilder meterReadingsBuilder = readingBuilderProvider.get();
        MeterReadings meterReadings = null;
        serviceCall.log(LogLevel.FINE, MessageFormat.format("Result collection is started for source ''{0}'', time range {1}",
                source, timeRangeSet));
        try {
            meterReadings = meterReadingsBuilder.withEndDevices(endDevices)
                    .ofReadingTypesWithMRIDs(readingTypesMRIDs)
                    .withLoadProfiles(loadProfilesNames)
                    .withRegisterGroups(registerGroupsNames)
                    .inTimeIntervals(timeRangeSet)
                    .withReadingTypesMRIDsTimeRangeMap(createReadingTypesMRIDsTimeRangeMap(endDevices, readingTypesMRIDs, timePeriodStart, timePeriodEnd))
                    .build();
        } catch (FaultMessage faultMessage) {
            serviceCall.requestTransition(FAILED);
            serviceCall.log(LogLevel.SEVERE,
                    MessageFormat.format("Unable to collect meter readings for source ''{0}'', time range {1}, du to error: " + faultMessage.getMessage(),
                        source, timeRangeSet));
            return;
        }
        if (meterReadings == null || meterReadings.getMeterReading().isEmpty()) {
            serviceCall.requestTransition(SUCCESSFUL);
            serviceCall.log(LogLevel.FINE,
                    MessageFormat.format("No meter readings are found for source ''{0}'', time range {1}",
                            source, timeRangeSet));
            return;
        }
        EndPointConfiguration endPointConfiguration = getEndPointConfiguration(serviceCall, callbackUrl);
        if (endPointConfiguration == null) {
            serviceCall.requestTransition(FAILED);
            return;
        }
        boolean isOk = sendMeterReadingsProvider.call(meterReadings, getHeader(correlationId), endPointConfiguration);
        if (!isOk) {
            serviceCall.requestTransition(FAILED);
            serviceCall.log(LogLevel.SEVERE,
                    MessageFormat.format("Unable to send meter readings data for source ''{0}'', time range {1}", source, timeRangeSet));
            return;
        }
        serviceCall.requestTransition(SUCCESSFUL);
        serviceCall.log(LogLevel.FINE,
                MessageFormat.format("Data successfully sent for source ''{0}'', time range {1}",
                        source, timeRangeSet));
    }

    private Device findDeviceForEndDevice(com.elster.jupiter.metering.Meter meter) {
        long deviceId = Long.parseLong(meter.getAmrId());
        return deviceService.findDeviceById(deviceId)
                .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Unable to find device with id: {0}", deviceId)));
    }

    private Map<String, RangeSet<Instant>> createReadingTypesMRIDsTimeRangeMap(Set<Meter> meters,
                                                                               Set<String> readingTypesMRIDs,
                                                                               Instant start,
                                                                               Instant end) {
        if (start != null) { // required only for case when start date is absent
            return null;
        }
        Map<String, RangeSet<Instant>> readingTypesMRIDsTimeRangeMap = new HashMap<>();
        Set<Device> devices = meters.stream()
                .map(meter -> findDeviceForEndDevice(meter))
                .collect(Collectors.toSet());
        devices.stream()
                .map(device -> device.getLoadProfiles())
                .flatMap(Collection::stream)
                .forEach(loadProfile -> {
                        loadProfile.getLoadProfileSpec().getLoadProfileType().getChannelTypes().stream()
                                .map(ct -> ct.getReadingType().getMRID())
                                .filter(mrid -> readingTypesMRIDs.contains(mrid))
                                .forEach(mrid ->
                                        readingTypesMRIDsTimeRangeMap.put(mrid, getTimeRangeSet(loadProfile.getLastReading().toInstant(), end))
                                );
                        });
        return readingTypesMRIDsTimeRangeMap;
    }

    private Instant calculateEndDateFromChildrenServiceCall(ServiceCall parent) {
        ServiceCall childServiceCall = parent.findChildren().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No SubParentServiceCall was found"))
                .findChildren().stream().findFirst().get();
        ChildGetMeterReadingsDomainExtension extension = childServiceCall.getExtension(ChildGetMeterReadingsDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for child service call"));
        return Optional.ofNullable(extension.getActialEndDate())
                .orElseThrow(() -> new IllegalStateException("Unable to get actual end date for child service call"));
    }

    private HeaderType getHeader(String correlationId) {
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.CREATED);
        header.setNoun("MeterReadings");
        header.setCorrelationID(correlationId);
        return header;
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

    private Set<String> getSetOfValuesFromString(String readingTypesString) {
        if (readingTypesString != null) {
            return Arrays.stream(readingTypesString.split(";")).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    private Set<Meter> getEndDevices(List<String> endDevicesMRIDs, ServiceCall serviceCall) {
        List<EndDevice> existedEndDevices = meteringService.getEndDeviceQuery()
                .select(where("MRID").in(endDevicesMRIDs));
        if (existedEndDevices == null || existedEndDevices.isEmpty()) {
            serviceCall.log(LogLevel.WARNING, "No end devices was found");
        }
        Set<Meter> existedMeters = new HashSet<>();
        for (com.elster.jupiter.metering.EndDevice endDevice: existedEndDevices) {
            if (endDevice instanceof Meter) {
                existedMeters.add((Meter)endDevice);
            }
        }
        return existedMeters;
    }
}
