/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.energyict.mdc.device.alarms.DeviceAlarmFilter;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.impl.ModuleConstants;
import com.energyict.mdc.device.alarms.impl.event.EventDescription;
import com.energyict.mdc.device.alarms.impl.i18n.MessageSeeds;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import com.energyict.cim.EndDeviceEventTypeMapping;
import com.google.inject.Injector;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class DeviceAlarmEvent implements IssueEvent, Cloneable {
    protected static final Logger LOGGER = Logger.getLogger(DeviceAlarmEvent.class.getName());

    private final DeviceAlarmService deviceAlarmService;
    private final IssueService issueService;
    private final MeteringService meteringService;
    private final DeviceService deviceService;
    private final Thesaurus thesaurus;
    private final TimeService timeService;
    private final Clock clock;


    private Device device;
    private Instant timestamp;
    private EventDescription eventDescription;
    private Injector injector;
    private int ruleId;

    private static final String WILDCARD = "*";
    private static final String ALL_EVENT_TYPES = "*.*.*.*";
    private static final String COLON_SEPARATOR = ":";
    private static final String COMMA_SEPARATOR = ",";
    private static final String SEMI_COLON_SEPARATOR = ";";

    public DeviceAlarmEvent(DeviceAlarmService deviceAlarmService, IssueService issueService, MeteringService meteringService, DeviceService deviceService, Thesaurus thesaurus, TimeService timeService, Clock clock, Injector injector) {
        this.deviceAlarmService = deviceAlarmService;
        this.issueService = issueService;
        this.meteringService = meteringService;
        this.deviceService = deviceService;
        this.thesaurus = thesaurus;
        this.timeService = timeService;
        this.clock = clock;
        this.injector = injector;
    }

    public abstract void init(Map<?, ?> jsonPayload);

    protected Thesaurus getThesaurus() {
        return this.thesaurus;
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

    public Clock getClock() {
        return clock;
    }

    public void wrap(Map<?, ?> rawEvent, EventDescription eventDescription, Device device) {
        this.eventDescription = eventDescription;
        if (device != null) {
            this.device = device;
        } else {
            getEventDevice(rawEvent);
        }
        getEventTimestamp(rawEvent);
    }

    public boolean checkOccurrenceConditions(int ruleId, String relativePeriodWithCount, String triggeringEndDeviceEventTypes) {
        setCreationRule(ruleId);
        List<String> relativePeriodWithCountValues = parseRawInputToList(relativePeriodWithCount, COLON_SEPARATOR);
        if (relativePeriodWithCountValues.size() != 2) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_ARGUMENTS, "Relative period with occurrence count for device alarms");
        }

        int eventCountThreshold = Integer.parseInt(relativePeriodWithCountValues.get(0));
        Optional<RelativePeriod> relativePeriod = timeService.findRelativePeriod(Long.parseLong(relativePeriodWithCountValues.get(1)));

        if (!relativePeriod.isPresent()) {
            return false;
        }
        List<String> inputTriggeringEventTypeList = getEndDeviceEventTypes(triggeringEndDeviceEventTypes);
        if (isAllEventTypesList(inputTriggeringEventTypeList)) {
            return getLoggedEvents(relativePeriod.get()).size() >= eventCountThreshold;
        } else if (getMatchingEventOccurenceCount(inputTriggeringEventTypeList, Collections.singletonList(this.getEventTypeMrid())) == 0) {
            return false;
        }
        return getTotalOccurenceCount(getLoggedEvents(relativePeriod.get()), getEndDeviceEventTypes(triggeringEndDeviceEventTypes), getDeviceCodes(triggeringEndDeviceEventTypes)) >= eventCountThreshold;
    }

    public boolean logOnSameAlarm(String raiseEventProps) {
        List<String> values = parseRawInputToList(raiseEventProps, COLON_SEPARATOR);
        if (values.size() != 3) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_ARGUMENTS, "Device Life Cycle in Device Type");
        }
        return Integer.parseInt(values.get(0)) == 1;
    }

    public boolean isClearing(List<String> endDeviceEventTypes) {
        return endDeviceEventTypes.contains(this.getEventTypeMrid())
                || endDeviceEventTypes
                .stream()
                .anyMatch(event -> checkMatchingEvent(event, this.getEventTypeMrid()));
    }

    public boolean isClearing(int ruleId, String endDeviceEventTypes) {
        setCreationRule(ruleId);
        return (isClearing(getEndDeviceEventTypes(endDeviceEventTypes)) || getEndDeviceEventTypes(endDeviceEventTypes).stream()
                .anyMatch(event -> checkMatchingEvent(event, this.getEventTypeMrid()))) && issueService.findOpenIssuesForDevice(getDevice().getName())
                .find()
                .stream()
                .anyMatch(issue -> issue.getRule().getId() == ruleId);
    }

    public boolean hasAssociatedDeviceLifecycleStatesInDeviceTypes(String statesInDeviceTypes) {
        return parseRawInputToList(statesInDeviceTypes, SEMI_COLON_SEPARATOR).stream().map(value -> parseRawInputToList(value, COLON_SEPARATOR))
                .anyMatch(valueSet -> valueSet.size() == 3 &&
                        this.getDevice().getDeviceType().getId() == Long.parseLong(valueSet.get(0)) &&
                        this.getDevice().getDeviceType().getDeviceLifeCycle().getId() == Long.parseLong(valueSet.get(1)) &&
                        parseRawInputToList(valueSet.get(2), COMMA_SEPARATOR).stream()
                                .map(String::trim)
                                .mapToLong(Long::parseLong).boxed().collect(Collectors.toList()).contains(this.getDevice().getState().getId()));
    }

    private void setCreationRule(int ruleId){
        this.ruleId = ruleId;
    }

    private List<String> parseRawInputToList(String rawInput, String delimiter) {
        return Arrays.stream(rawInput.split(delimiter)).map(String::trim).collect(Collectors.toList());
    }

    private List<String> getEndDeviceEventTypes(String endDeviceEventTypes) {
        return parseRawInputToList(endDeviceEventTypes, COMMA_SEPARATOR).stream()
                .map(type -> parseRawInputToList(type, COLON_SEPARATOR).get(0))
                .collect(Collectors.toList());
    }

    private List<String> getDeviceCodes(String endDeviceEventTypes) {
        return parseRawInputToList(endDeviceEventTypes, COMMA_SEPARATOR).stream()
                .map(type -> parseRawInputToList(type, COLON_SEPARATOR).get(1))
                .filter(code -> !code.equals(WILDCARD))
                .collect(Collectors.toList());
    }

    private List<EndDeviceEventRecord> getLoggedEvents(RelativePeriod relativePeriod) {
        return getDevice().getLogBooks().stream()
                .map(logBook -> logBook.getEndDeviceEvents(relativePeriod.getClosedInterval(clock.instant().atZone(clock.getZone()).with(LocalTime.MIDNIGHT))))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    private int getTotalOccurenceCount(List<EndDeviceEventRecord> loggedEvents, List<String> triggeringEvents, List<String> deviceCodes) {
        return getMatchingEventOccurenceCount(triggeringEvents, loggedEvents.stream()
                .map(EndDeviceEventRecord::getEventTypeCode)
                .filter(eventTypeMrid -> !eventTypeMrid.equals(EndDeviceEventTypeMapping.OTHER.getEventType().getCode()))
                .collect(Collectors.toList())) +
                loggedEvents.stream()
                        .filter(event -> event.getEventType().getMRID().equals(EndDeviceEventTypeMapping.OTHER.getEventType().getCode()))
                        .map(EndDeviceEventRecord::getDeviceEventType)
                        .collect(Collectors.collectingAndThen(Collectors.toList(), Collection::stream)).filter(deviceCodes::contains)
                        .collect(Collectors.toList()).size();

    }

    private int getMatchingEventOccurenceCount(List<String> rawTriggeringEventTypeList, List<String> loggedEventTypeList) {
        List<String> starredList = rawTriggeringEventTypeList.stream().filter(type -> type.contains(WILDCARD)).collect(Collectors.toList());
        int initCount = loggedEventTypeList.stream()
                .filter(rawTriggeringEventTypeList::contains)
                .collect(Collectors.toList()).size();
        if (starredList.isEmpty()) {
            return initCount;
        } else {
            AtomicInteger count = new AtomicInteger(0);
            starredList.forEach(starredEvent -> loggedEventTypeList.forEach(loggedEvent -> {
                if (checkMatchingEvent(starredEvent, loggedEvent)) {
                    count.incrementAndGet();
                }
            }));
            return count.intValue() + initCount;
        }
    }


    private boolean checkMatchingEvent(String inputEvent, String loggedEvent) {
        String testVal;
        String regexVal;
        if (inputEvent.contains(WILDCARD)) {
            testVal = loggedEvent;
            regexVal = escape(inputEvent).replaceAll("\\*", "\\\\d+");     //Replace the * wildcards with proper regex wildcard
        } else {
            return inputEvent.equals(loggedEvent);
        }
        return testVal.matches(regexVal);
    }

    private String escape(String cimCode) {
        return cimCode.replaceAll("\\.", "\\\\.");
    }


    private boolean isAllEventTypesList(List<String> eventTypeist) {
        return eventTypeist.contains(ALL_EVENT_TYPES);
    }


    private void getEventDevice(Map<?, ?> rawEvent) {
        Optional<Long> endDeviceId = getLong(rawEvent, ModuleConstants.DEVICE_IDENTIFIER);
        EndDevice endDevice = meteringService.findEndDeviceById(endDeviceId.orElse(0L))
                .orElseThrow(() -> new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_KORE_DEVICE, endDeviceId));
        long amrId = Long.parseLong(endDevice.getAmrId());
        device = deviceService.findDeviceById(amrId).orElseThrow(() -> new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_DEVICE, amrId));
    }

    private void getEventTimestamp(Map<?, ?> rawEvent) {
        Long eventTimeStamp = getLong(rawEvent, ModuleConstants.EVENT_TIMESTAMP).orElseThrow(() -> new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_TIMESTAMP));
        timestamp = Instant.ofEpochSecond(eventTimeStamp);
    }

    private Optional<EndDevice> findEndDeviceByMdcDevice() {
        if (device == null) { //for unknown inbound device
            return Optional.empty();
        }
        Optional<AmrSystem> amrSystemRef = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
        if (amrSystemRef.isPresent()) {
            Optional<Meter> meterRef = amrSystemRef.get().findMeter(String.valueOf(device.getId()));
            if (meterRef.isPresent()) {
                return Optional.of(meterRef.get());
            }
        }
        throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_KORE_DEVICE, device.getId());
    }

    @Override
    public String getEventType() {
        return eventDescription.getUniqueKey();
    }


    public abstract String getEventTypeMrid();


    @Override
    public Optional<EndDevice> getEndDevice() {
        return findEndDeviceByMdcDevice();
    }

    public Device getDevice() {
        return device;
    }

    @Override
    public Optional<? extends OpenIssue> findExistingIssue() {
        DeviceAlarmFilter filter = new DeviceAlarmFilter();
        Optional<CreationRule> rule = issueService.getIssueCreationService().findCreationRuleById(ruleId);
        if(rule.isPresent()){
            filter.setRule(rule.get());
            getEndDevice().ifPresent(filter::setDevice);
            new ArrayList<String>(){{
                add(IssueStatus.OPEN);
                add(IssueStatus.IN_PROGRESS);
                add(IssueStatus.SNOOZED);
            }}.forEach(is -> filter.setStatus(issueService.findStatus(is).get()));
            filter.setAlarmReason(rule.get().getReason());
            Optional<? extends DeviceAlarm> foundAlarm = deviceAlarmService.findAlarms(filter).find()
                    .stream().max(Comparator.comparing(Issue::getCreateDateTime));//It is going to be only zero or one open alarm per device
            if (foundAlarm.isPresent()) {
                return Optional.of((OpenIssue) foundAlarm.get());
            }
        }
        return Optional.empty();

    }

    private Optional<Long> getLong(Map<?, ?> map, String key) {
        Object contents = map.get(key);
        if (contents == null) {
            return Optional.empty();
        }
        return Optional.of(((Number) contents).longValue());
    }

    @Override
    public DeviceAlarmEvent clone() {
        DeviceAlarmEvent clone = injector.getInstance(eventDescription.getEventClass());
        clone.eventDescription = eventDescription;
        clone.device = device;
        return clone;
    }
}