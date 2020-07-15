/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateIssueException;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.DataCollectionEventMetadata;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.OccurrenceConditionControllable;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;

import com.google.common.collect.Range;
import com.google.inject.Injector;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public abstract class DataCollectionEvent implements IssueEvent, OccurrenceConditionControllable, Cloneable {
    protected static final Logger LOG = Logger.getLogger(DataCollectionEvent.class.getName());

    private final IssueDataCollectionService issueDataCollectionService;
    private final MeteringService meteringService;
    private final EventService eventService;
    private final CommunicationTaskService communicationTaskService;
    private final DeviceService deviceService;
    private final TopologyService topologyService;
    private final Thesaurus thesaurus;
    private final TimeService timeService;
    private final Clock clock;
    private final IssueService issueService;

    private Device device;

    private Instant timestamp;

    private EventDescription eventDescription;

    private Injector injector;

    private static final String COLON_SEPARATOR = ":";
    private static final String COMMA_SEPARATOR = ",";
    private static final String SEMI_COLON_SEPARATOR = ";";

    private long ruleId;

    public DataCollectionEvent(IssueDataCollectionService issueDataCollectionService,
                               MeteringService meteringService,
                               DeviceService deviceService,
                               CommunicationTaskService communicationTaskService,
                               TopologyService topologyService,
                               Thesaurus thesaurus, EventService eventService,
                               TimeService timeService,
                               Clock clock,
                               Injector injector,
                               IssueService issueService) {
        this.issueDataCollectionService = issueDataCollectionService;
        this.meteringService = meteringService;
        this.communicationTaskService = communicationTaskService;
        this.deviceService = deviceService;
        this.topologyService = topologyService;
        this.thesaurus = thesaurus;
        this.timeService = timeService;
        this.eventService = eventService;
        this.clock = clock;
        this.injector = injector;
        this.issueService = issueService;
    }

    protected IssueDataCollectionService getIssueDataCollectionService() {
        return issueDataCollectionService;
    }

    protected MeteringService getMeteringService() {
        return meteringService;
    }

    protected CommunicationTaskService getCommunicationTaskService() {
        return communicationTaskService;
    }

    protected TopologyService getTopologyService() {
        return topologyService;
    }

    protected DeviceService getDeviceService() {
        return deviceService;
    }

    protected Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    protected TimeService getTimeService() {
        return timeService;
    }

    protected EventDescription getDescription() {
        if (eventDescription == null) {
            throw new IllegalStateException("You are trying to get event description for event that was not initialized yet");
        }
        return eventDescription;
    }

    protected void setDevice(Device device) {
        this.device = device;
    }

    protected void setEventDescription(EventDescription eventDescription) {
        this.eventDescription = eventDescription;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public void wrap(Map<?, ?> rawEvent, EventDescription eventDescription, Device device) {
        this.eventDescription = eventDescription;
        if (device != null) {
            this.device = device;
        } else {
            getEventDevice(rawEvent);
        }
        getEventTimestamp(rawEvent);
        wrapInternal(rawEvent, eventDescription);
    }

    protected abstract void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription);

    protected void getEventDevice(Map<?, ?> rawEvent) {
        Optional<Long> amrId = getLong(rawEvent, ModuleConstants.DEVICE_IDENTIFIER);
        device = getDeviceService().findDeviceById(amrId.orElse(0L)).orElseThrow(() -> new UnableToCreateIssueException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_DEVICE, amrId));
    }

    protected void getEventTimestamp(Map<?, ?> rawEvent) {
        Long timestampMilli = getLong(rawEvent, ModuleConstants.EVENT_TIMESTAMP).orElseThrow(() -> new UnableToCreateIssueException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_TIMESTAMP));
        timestamp = Instant.ofEpochMilli(timestampMilli);
    }

    private Optional<EndDevice> findEndDeviceByMdcDevice() {
        if (device == null) { //for unknown inbound device
            return Optional.empty();
        }
        Optional<AmrSystem> amrSystemRef = getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId());
        if (amrSystemRef.isPresent()) {
            Optional<Meter> meterRef = amrSystemRef.get().findMeter(String.valueOf(device.getId()));
            if (meterRef.isPresent()) {
                return Optional.of(meterRef.get());
            }
        }
        throw new UnableToCreateIssueException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_KORE_DEVICE, device.getId());
    }

    protected Instant getLastSuccessfulCommunicationEnd(Device concentrator) {
        Instant lastSuccessfulCommTask = Instant.EPOCH;
        for (ConnectionTask<?, ?> task : concentrator.getConnectionTasks()) {
            Instant taskEnd = task.getLastSuccessfulCommunicationEnd();
            if (taskEnd != null && lastSuccessfulCommTask.isBefore(taskEnd)) {
                lastSuccessfulCommTask = taskEnd;
            }
        }
        return lastSuccessfulCommTask;
    }

    protected int getNumberOfDevicesWithEvents(Device concentrator) {
        Instant start = getLastSuccessfulCommunicationEnd(concentrator);
        int numberOfDevicesWithEvents = 0;
        try {
            DataCollectionEventDescription description = DataCollectionEventDescription.valueOf(this.eventDescription.getUniqueKey());
            if (description != null && description.getErrorType() != null) {
                numberOfDevicesWithEvents = this.getTopologyService().
                        countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(
                                description.getErrorType(),
                                concentrator,
                                Interval.startAt(start));
            }
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "Incorrect communication type for concentrator[id={0}]", concentrator.getId());
        }
        return numberOfDevicesWithEvents;
    }

    // Used in rule engine
    public double computeCurrentThreshold() {
        Optional<Device> concentrator = this.topologyService.getPhysicalGateway(this.device);
        if (!concentrator.isPresent()) {
            LOG.log(Level.WARNING, "Concentrator for device[id={0}] is not found", device.getId());
            return -1;
        }
        int numberOfEvents = getNumberOfDevicesWithEvents(concentrator.get());
        int numberOfConnectedDevices = this.topologyService.findPhysicalConnectedDevices(concentrator.get()).size();
        if (numberOfConnectedDevices == 0) {
            LOG.log(Level.WARNING, "Number of connected devices for concentrator[id={0}] equals 0", concentrator.get().getId());
            return -1;
        }
        return (double) numberOfEvents / (double) numberOfConnectedDevices * 100.0;
    }

    // Used in rule engine
    //copied from com.energyict.mdc.device.alarms.event.DeviceAlarmEvent
    public boolean hasAssociatedDeviceLifecycleStatesInDeviceTypes(String statesInDeviceTypes) {
        return parseRawInputToList(statesInDeviceTypes, SEMI_COLON_SEPARATOR).stream().map(value -> parseRawInputToList(value, COLON_SEPARATOR))
                .anyMatch(valueSet -> valueSet.size() == 3 &&
                        this.getDevice().getDeviceType().getId() == Long.parseLong(valueSet.get(0)) &&
                        this.getDevice().getDeviceType().getDeviceLifeCycle().getId() == Long.parseLong(valueSet.get(1)) &&
                        parseRawInputToList(valueSet.get(2), COMMA_SEPARATOR).stream()
                                .map(String::trim)
                                .mapToLong(Long::parseLong).boxed().collect(Collectors.toList()).contains(this.getDevice().getState().getId()));
    }

    // Used in rule engine
    public boolean checkOccurrenceConditions(final String relativePeriodWithCount, final String triggeringEndDeviceEventTypes) {
        final List<String> relativePeriodWithCountValues = parseRawInputToList(relativePeriodWithCount, COLON_SEPARATOR);

        final int eventCountThreshold = Integer.parseInt(relativePeriodWithCountValues.get(0));
        final Optional<RelativePeriod> relativePeriod = timeService.findRelativePeriod(Long.parseLong(relativePeriodWithCountValues.get(1)));

        if (relativePeriodWithCountValues.size() != 2) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_ARGUMENTS,
                    "Relative period with occurrence count for device alarms",
                    String.valueOf(2),
                    String.valueOf(relativePeriodWithCountValues.size()));
        }

        if (!relativePeriod.isPresent()) {
            throw new LocalizedFieldValidationException(MessageSeeds.EVENT_BAD_DATA_NO_RELATIVE_PERIOD,
                    "Relative period can not be obtained! Relative Period: ",
                    relativePeriodWithCountValues.get(1));
        }

        final Range<ZonedDateTime> closedInterval = relativePeriod.get().getClosedZonedInterval(clock.instant().atZone(clock.getZone()).with(LocalTime.now()));
        final List<DataCollectionEventMetadata> dataCollectionEvents = issueDataCollectionService.getDataCollectionEventsForDeviceWithinTimePeriod(device, closedInterval);

        int counter = 0;
        for (DataCollectionEventMetadata event : dataCollectionEvents) {
            if (event.getEventType().equals(this.getIssueResolvingEvent().getName())) {
                return false;
            } else if (event.getEventType().equals(this.getIssueCausingEvent().getName())) {
                if (closedInterval.contains(event.getCreateDateTime().atZone(clock.getZone()))) {
                    counter++;
                }
            }

            if (counter >= eventCountThreshold) {
                return true;
            }
        }
        return false;
    }

    private List<String> parseRawInputToList(String rawInput, String delimiter) {
        return Arrays.stream(rawInput.split(delimiter)).map(String::trim).collect(Collectors.toList());
    }

    @Override
    public String getEventType() {
        return eventDescription.getUniqueKey();
    }

    @Override
    public Optional<EndDevice> getEndDevice() {
        return findEndDeviceByMdcDevice();
    }

    public Device getDevice() {
        return device;
    }

    @Override
    public Optional<? extends OpenIssue> findExistingIssue() {
        return getIssueDataCollectionService().stream(OpenIssueDataCollection.class, ConnectionTask.class, ComTaskExecution.class, ComSession.class, OpenIssue.class, CreationRule.class)
                .filter(getConditionForExistingIssue().and(where("baseIssue.rule.id").isEqualTo(ruleId)))
                .findAny();
    }

    protected abstract Condition getConditionForExistingIssue();

    protected Optional<Long> getLong(Map<?, ?> map, String key) {
        Object contents = map.get(key);
        if (contents == null) {
            return Optional.empty();
        }
        return Optional.of(((Number) contents).longValue());
    }

    @Override
    public DataCollectionEvent clone() {
        DataCollectionEvent clone = injector.getInstance(eventDescription.getEventClass());
        clone.eventDescription = eventDescription;
        clone.device = device;
        return clone;
    }

    public DataCollectionEvent cloneForAggregation() {
        DataCollectionEvent clone = this.clone();
        Optional<Device> physicalGateway = this.topologyService.getPhysicalGateway(this.device);
        if (physicalGateway.isPresent()) {
            clone.device = physicalGateway.get();
        } else {
            clone.device = null;
        }
        return clone;
    }

    public abstract EventDescription getIssueCausingEvent();

    public abstract EventDescription getIssueResolvingEvent();

    public boolean isResolveEvent() {
        return false;
    }

    /**
     * used by issue creation rule
     */
    public void setCreationRule(long ruleId) {
        this.ruleId = ruleId;
    }

    public long getCreationRule() {
        return ruleId;
    }
}
