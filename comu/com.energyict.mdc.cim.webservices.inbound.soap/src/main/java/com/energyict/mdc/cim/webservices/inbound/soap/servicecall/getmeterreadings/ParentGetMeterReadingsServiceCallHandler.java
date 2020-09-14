/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.cim.webservices.inbound.soap.meterreadings.MeterReadingsBuilder;
import com.energyict.mdc.common.device.data.Channel;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LoadProfile;
import com.energyict.mdc.device.data.DeviceService;

import ch.iec.tc57._2011.getmeterreadings.FaultMessage;
import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.schema.message.HeaderType;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import javax.inject.Inject;
import javax.inject.Provider;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.servicecall.DefaultState.CANCELLED;
import static com.elster.jupiter.servicecall.DefaultState.FAILED;
import static com.elster.jupiter.servicecall.DefaultState.PARTIAL_SUCCESS;
import static com.elster.jupiter.servicecall.DefaultState.SUCCESSFUL;
import static com.elster.jupiter.util.conditions.Where.where;
import static com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator.actualRecurrentTaskFrequency;
import static com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator.actualRecurrentTaskReadOutDelay;

public class ParentGetMeterReadingsServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "ParentGetMeterReadingsServiceCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

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
            case ONGOING: // normally result collection is performed when state is changed PAUSED --> ONGOING
                if (oldState == DefaultState.PAUSED) {
                    collectAndSendResult(serviceCall);
                }
                break;
            case SUCCESSFUL:
                processChildren(serviceCall, SUCCESSFUL);
                break;
            case REJECTED:
            case FAILED:
            case CANCELLED:
                processChildren(serviceCall, CANCELLED);
                sendResponse(serviceCall, new MeterReadings(), true);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    @Override
    public void onChildStateChange(ServiceCall parentServiceCall, ServiceCall subParentServiceCall, DefaultState oldState, DefaultState newState) {
        if (!newState.isOpen() && parentServiceCall.getState() == DefaultState.WAITING) {
            ServiceCallTransitionUtils.resultTransition(parentServiceCall, true);
        }
    }

    private void processChildren(ServiceCall parentServiceCall, DefaultState newState) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.states.addAll(Arrays.stream(DefaultState.values())
                .filter(state -> state.isOpen() && state != newState)
                .map(state -> state.name())
                .collect(Collectors.toSet()));
        parentServiceCall.findChildren(filter).stream()
                .forEach(subParentServiceCall -> subParentServiceCall.requestTransition(newState));
    }

    private boolean doAllClosedServiceCallsHaveState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().filter(sc -> !sc.getState().isOpen()).allMatch(sc -> sc.getState().equals(defaultState));
    }

    private void collectAndSendResult(ServiceCall serviceCall) {
        ParentGetMeterReadingsDomainExtension extension = serviceCall.getExtension(ParentGetMeterReadingsDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
        Instant timePeriodStart = extension.getTimePeriodStart();
        Instant timePeriodEnd = extension.getTimePeriodEnd();
        String readingTypesString = extension.getReadingTypes();
        String loadProfilesString = extension.getLoadProfiles();
        String registerGroupsString = extension.getRegisterGroups();
        Finder<ServiceCall> subParentChildren = serviceCall.findChildren();
        List<String> endDevicesMRIDs = subParentChildren.stream()
                .map(c -> c.getExtension(SubParentGetMeterReadingsDomainExtension.class)
                        .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"))
                        .getEndDeviceMrid())
                .collect(Collectors.toList());
        String source = extension.getSource();

        if (timePeriodEnd == null) {
            timePeriodEnd = calculateEndDateFromChild(getChildDomainExtension(serviceCall));
        }
        if (timePeriodStart == null) {
            timePeriodStart = calculateStartDateFromChild(getChildDomainExtension(serviceCall));
        }
        RangeSet<Instant> timeRangeSet = TreeRangeSet.create();
        if (timePeriodStart != null) {
            timeRangeSet = getTimeRangeSet(timePeriodStart, timePeriodEnd);
        }
        Set<Meter> endDevices = getEndDevices(endDevicesMRIDs, serviceCall);
        Set<String> readingTypesMRIDs = getSetOfValuesFromString(readingTypesString);
        Set<String> loadProfilesNames = getSetOfValuesFromString(loadProfilesString);
        Set<String> registerGroupsNames = getSetOfValuesFromString(registerGroupsString);

        setBackLastReadingForLoadProfiles(subParentChildren, loadProfilesNames, readingTypesMRIDs);

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
                    .withReadingTypesMRIDsTimeRangeMap(createReadingTypesMRIDsTimeRangeMap(endDevices, readingTypesMRIDs, loadProfilesNames, timePeriodStart, timePeriodEnd))
                    .withRegisterUpperBoundShift(calculateRegisterUpperBoundShift())
                    .build();
        } catch (FaultMessage faultMessage) {
            serviceCall.requestTransition(FAILED);
            serviceCall.log(LogLevel.SEVERE,
                    MessageFormat.format("Unable to collect meter readings for source ''{0}'', time range {1}, due to error: " + faultMessage
                                    .getMessage(),
                            source, timeRangeSet));
            return;
        }
        if (meterReadings == null || meterReadings.getMeterReading().isEmpty()) {
            serviceCall.log(LogLevel.FINE,
                    MessageFormat.format("No meter readings are found for source ''{0}'', time range {1}",
                            source, timeRangeSet));
        }
        if (!sendResponse(serviceCall, meterReadings, false)) {
            return;
        }

        if (doAllClosedServiceCallsHaveState(ServiceCallTransitionUtils.findAllChildren(serviceCall), SUCCESSFUL)) {
            serviceCall.requestTransition(SUCCESSFUL);
        } else if (ServiceCallTransitionUtils.hasAllChildrenStates(ServiceCallTransitionUtils.findAllChildren(serviceCall), CANCELLED)) {
            serviceCall.requestTransition(CANCELLED);
        } else if (ServiceCallTransitionUtils.hasAnyChildState(ServiceCallTransitionUtils.findAllChildren(serviceCall), PARTIAL_SUCCESS) ||
                ServiceCallTransitionUtils.hasAnyChildState(ServiceCallTransitionUtils.findAllChildren(serviceCall), SUCCESSFUL)) {
            serviceCall.requestTransition(PARTIAL_SUCCESS);
        } else {
            serviceCall.requestTransition(FAILED);
        }

        serviceCall.log(LogLevel.FINE,
                MessageFormat.format("Data successfully sent for source ''{0}'', time range {1}",
                        source, timeRangeSet));
    }

    /**
     * Set back last reading date to the most recent entry we have in our database.
     * Last reading date was set to start date during parsing get meter readings request and creating communication child services.
     * {@link com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands}
     */
    private void setBackLastReadingForLoadProfiles(Finder<ServiceCall> subParentChildren, Set<String> loadProfilesNames, Set<String> readingTypesMRIDs) {
        List<String> endDevicesMRIDs = subParentChildren.stream()
                .filter(subParent -> !subParent.findChildren().paged(0, 0).find().isEmpty())
                .map(c -> c.getExtension(SubParentGetMeterReadingsDomainExtension.class)
                        .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"))
                        .getEndDeviceMrid())
                .collect(Collectors.toList());

        if (!endDevicesMRIDs.isEmpty()) {
            deviceService.findAllDevices(where("mRID").in(endDevicesMRIDs))
                    .stream()
                    .forEach(device -> {
                        Set<LoadProfile> loadProfiles = getExistedOnDeviceLoadProfiles(device, loadProfilesNames);
                        loadProfiles.addAll(getLoadProfilesForReadingTypes(device, readingTypesMRIDs));
                        loadProfiles.forEach(loadProfile -> {
                            Optional<Instant> lastReading = loadProfile.getChannels().stream()
                                    .map(Channel::getLastDateTime)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .max(Comparator.naturalOrder());

                            if (lastReading.isPresent() && (loadProfile.getLastReading() == null || !lastReading.get().equals(loadProfile.getLastReading().toInstant()))) {
                                device.getLoadProfileUpdaterFor(loadProfile).setLastReading(lastReading.get()).update();
                            }
                        });
                    });
        }
    }

    private Set<LoadProfile> getLoadProfilesForReadingTypes(Device device, Set<String> readingTypes) {
        return device.getLoadProfiles().stream()
                .filter(lp -> lp.getLoadProfileSpec().getChannelSpecs().stream()
                        .anyMatch(c -> readingTypes.contains(c.getReadingType().getMRID())
                        || (c.getReadingType().getCalculatedReadingType().isPresent()
                                        && readingTypes.contains(c.getReadingType().getCalculatedReadingType().get().getMRID())))
                )
                .collect(Collectors.toSet());
    }

    private Set<LoadProfile> getExistedOnDeviceLoadProfiles(Device device, Set<String> loadProfilesNames) {
        return device.getLoadProfiles().stream()
                .filter(lp -> loadProfilesNames.contains(lp.getLoadProfileSpec().getLoadProfileType().getName()))
                .collect(Collectors.toSet());
    }

    private int calculateRegisterUpperBoundShift() {
        // CXO-10805: To make sure the latest register value is included
        return actualRecurrentTaskFrequency + actualRecurrentTaskReadOutDelay + 5;
    }

    private boolean sendResponse(ServiceCall serviceCall, MeterReadings meterReadings, boolean sendFromFailedState) {
        ParentGetMeterReadingsDomainExtension extension = serviceCall.getExtension(ParentGetMeterReadingsDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

        if (sendFromFailedState && extension.getResponseStatus().equals(ParentGetMeterReadingsDomainExtension.ResponseStatus.NOT_CONFIRMED.getName())) {
            return false;
        }

        Optional<EndPointConfiguration> endPointConfigurationOptional = getEndPointConfiguration(serviceCall, extension.getCallbackUrl());
        if (!endPointConfigurationOptional.isPresent()) {
            serviceCall.log(LogLevel.SEVERE, MessageFormat.format("No end point configuration is found by URL ''{0}''.", extension.getCallbackUrl()));
            if (!sendFromFailedState) {
                serviceCall.requestTransition(FAILED);
            }
            return false;
        }
        if (!sendMeterReadingsProvider.call(meterReadings, getHeader(extension.getCorrelationId(), sendFromFailedState ? "Failure occurred" : null), endPointConfigurationOptional.get())) {
            if (!sendFromFailedState) {
                serviceCall.requestTransition(FAILED);
                extension.setResponseStatus(ParentGetMeterReadingsDomainExtension.ResponseStatus.NOT_CONFIRMED.getName());
                serviceCall.update(extension);
            }

            RangeSet<Instant> timeRangeSet = null;
            if (extension.getTimePeriodStart() != null) {
                timeRangeSet = getTimeRangeSet(extension.getTimePeriodStart(), extension.getTimePeriodEnd());
            }
            serviceCall.log(LogLevel.SEVERE,
                    MessageFormat.format("Unable to send meter readings data for source ''{0}'', time range {1}", extension
                            .getSource(), timeRangeSet));
            return false;
        }
        extension.setResponseStatus(ParentGetMeterReadingsDomainExtension.ResponseStatus.CONFIRMED.getName());
        serviceCall.update(extension);
        return true;
    }

    private Set<Device> getDevices(Set<com.elster.jupiter.metering.Meter> existedMeters) {
        return ImmutableSet.copyOf(deviceService.findAllDevices(where("id").in(existedMeters.stream()
                .map(meter -> meter.getAmrId())
                .collect(Collectors.toList()))).stream().collect(Collectors.toSet()));
    }

    private Map<String, RangeSet<Instant>> createReadingTypesMRIDsTimeRangeMap(Set<Meter> meters,
                                                                               Set<String> readingTypesMRIDs,
                                                                               Set<String> loadProfilesNames,
                                                                               Instant start,
                                                                               Instant end) {
        if (start != null) { // required only for case when start date is absent
            return null;
        }
        Map<String, RangeSet<Instant>> readingTypesMRIDsTimeRangeMap = new HashMap<>();
        Set<Device> devices = getDevices(meters);
        devices.stream()
                .map(device -> device.getLoadProfiles())
                .flatMap(Collection::stream)
                .filter(lp -> lp.getLastReading() != null)
                .forEach(loadProfile -> {
                    loadProfile.getLoadProfileSpec().getLoadProfileType().getChannelTypes().stream()
                            .map(ct -> ct.getReadingType().getMRID())
                            .filter(mrid -> readingTypesMRIDs.contains(mrid)
                                    || loadProfilesNames.contains(loadProfile.getLoadProfileSpec()
                                    .getLoadProfileType()
                                    .getName()))
                            .forEach(mrid ->
                                    readingTypesMRIDsTimeRangeMap.put(mrid, getTimeRangeSet(loadProfile.getLastReading()
                                            .toInstant(), end))
                            );
                });
        return readingTypesMRIDsTimeRangeMap;
    }

    private ChildGetMeterReadingsDomainExtension getChildDomainExtension(ServiceCall parent) {
        ServiceCall childServiceCall = parent.findChildren().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No SubParentServiceCall was found"))
                .findChildren().stream().findFirst().get();
        return childServiceCall.getExtension(ChildGetMeterReadingsDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for child service call"));
    }

    private Instant calculateEndDateFromChild(ChildGetMeterReadingsDomainExtension extension) {
        return Optional.ofNullable(extension.getActualEndDate())
                .orElseThrow(() -> new IllegalStateException("Unable to get actual end date for child service call"));
    }

    private Instant calculateStartDateFromChild(ChildGetMeterReadingsDomainExtension extension) {
        return Optional.ofNullable(extension.getActualStartDate())
                .orElseThrow(() -> new IllegalStateException("Unable to get actual start date for child service call"));
    }

    private HeaderType getHeader(String correlationId, String comment) {
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.REPLY);
        header.setNoun("MeterReadings");
        header.setCorrelationID(correlationId);
        if (comment != null) {
            header.setComment(comment);
        }
        return header;
    }

    private Optional<EndPointConfiguration> getEndPointConfiguration(ServiceCall serviceCall, String url) {
        return endPointConfigurationService.findEndPointConfigurations()
                .stream()
                .filter(EndPointConfiguration::isActive)
                .filter(endPointConfiguration -> !endPointConfiguration.isInbound())
                .filter(endPointConfiguration -> endPointConfiguration.getUrl().equals(url))
                .findFirst();
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
        List<Meter> existedMeters = meteringService.getMeterQuery()
                .select(where("mRID").in(endDevicesMRIDs));
        if (existedMeters == null || existedMeters.isEmpty()) {
            serviceCall.log(LogLevel.WARNING, "No end devices were found");
        }
        return new HashSet<>(existedMeters);
    }
}
