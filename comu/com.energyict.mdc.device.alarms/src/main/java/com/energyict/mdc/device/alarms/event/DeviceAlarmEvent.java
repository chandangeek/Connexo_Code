/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateEventException;
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
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.energyict.mdc.device.alarms.DeviceAlarmFilter;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.impl.ModuleConstants;
import com.energyict.mdc.device.alarms.impl.event.EventDescription;
import com.energyict.mdc.device.alarms.impl.i18n.MessageSeeds;
import com.energyict.mdc.device.alarms.impl.templates.BasicDeviceAlarmRuleTemplate;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeMapping;

import com.google.inject.Injector;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
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

    private static final String ANY = "*";
    private static final String ALL_EVENT_TYPES = "*.*.*.*";
    private static final String SEPARATOR = ":";

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

    public int computeOccurenceCount(int ruleId, String relativePeriodId, String raiseEventProps, String triggeringEndDeviceEventTypes, String clearingEndDeviceEventTypes) {
        Optional<RelativePeriod> relativePeriod = timeService.findRelativePeriod(Long.parseLong(relativePeriodId));
        if (!relativePeriod.isPresent()) {
            return -1;
        }

        List<String> inputTriggeringEventTypeList = getEndDeviceEventTypes(triggeringEndDeviceEventTypes);
        if (isAllEventTypesList(inputTriggeringEventTypeList)) {
            return getLoggedEvents(relativePeriod.get()).size();
        } else if (eventTypeMridContainedInListCount(inputTriggeringEventTypeList, Collections.singletonList(this.getEventTypeMrid())) == 0) {
            return -1;
        }
        if (getEndDeviceEventTypes(clearingEndDeviceEventTypes).contains(this.getEventTypeMrid())) {
            if (raiseEventProps != null && !raiseEventProps.isEmpty() && Integer.parseInt(Arrays.asList(raiseEventProps.split("-")).get(0)) == 1 &&
                    // issueService.getIssueCreationService().findCreationRuleById(Long.parseLong(ruleId)).isPresent() &&
                    issueService.findOpenIssuesForDevice(getDevice().getName()).find().stream().filter(issue -> issue.getRule().getId() == ruleId).findAny().isPresent()) {
                return Integer.MAX_VALUE;
            } else {
                return -1;
            }
        }
        return getOccurenceCount(getLoggedEvents(relativePeriod.get()), getEndDeviceEventTypes(triggeringEndDeviceEventTypes), getDeviceCodes(triggeringEndDeviceEventTypes));
    }


    private List<String> parseCommaSeparatedStringToList(String rawInput) {
        return Arrays.asList(rawInput.split(",")).stream().collect(Collectors.toList());
    }

    private List<String> getEndDeviceEventTypes(String endDeviceEventTypes) {
        return parseCommaSeparatedStringToList(endDeviceEventTypes).stream().map(type -> type.substring(0,type.indexOf(SEPARATOR))).collect(Collectors.toList());
    }

    private List<String> getDeviceCodes(String endDeviceEventTypes) {
        return parseCommaSeparatedStringToList(endDeviceEventTypes).stream().map(type -> type.substring(type.indexOf(SEPARATOR)+1)).filter(code -> !code.equals(ANY)).collect(Collectors.toList());
    }

    private List<EndDeviceEventRecord> getLoggedEvents(RelativePeriod relativePeriod) {
        return getDevice().getLogBooks().stream()
                .map(logBook -> logBook.getEndDeviceEvents(relativePeriod.getClosedInterval(clock.instant().atZone(clock.getZone()).with(LocalTime.MIDNIGHT).plusDays(1))))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    private int getOccurenceCount(List<EndDeviceEventRecord> loggedEvents, List<String> triggeringEvents, List<String> deviceCodes) {
        return eventTypeMridContainedInListCount(triggeringEvents, loggedEvents.stream()
                .map(EndDeviceEventRecord::getEventTypeCode)
                .filter(eventTypeMrid -> !eventTypeMrid.equals(EndDeviceEventTypeMapping.OTHER.getEndDeviceEventTypeMRID()))
                .collect(Collectors.toList())) +
                loggedEvents.stream()
                        .filter(event -> event.getEventType().getMRID().equals(EndDeviceEventTypeMapping.OTHER.getEndDeviceEventTypeMRID()))
                        .map(EndDeviceEventRecord::getDeviceEventType)
                        .collect(Collectors.collectingAndThen(Collectors.toList(), Collection::stream)).filter(deviceCodes::contains)
                        .collect(Collectors.toList()).size();

    }

    private int eventTypeMridContainedInListCount(List<String> rawTriggeringEventTypesList, List<String> loggedEventTypeList) {
        List<String> starredList = rawTriggeringEventTypesList.stream().filter(type -> type.contains(ANY)).collect(Collectors.toList());
        int initCount = loggedEventTypeList.stream()
                .filter(rawTriggeringEventTypesList::contains)
                .collect(Collectors.toList()).size();
        if (starredList.isEmpty()) {
            return initCount;
        } else {
            int count = 0;
            for (String starredElement : starredList) {
                StringTokenizer starredElementTokenizer = getTokenized(starredElement);
                for (String loggedElement : loggedEventTypeList) {
                    StringTokenizer loggedElementTokenizer = getTokenized(loggedElement);
                    int internalCount = 0;
                    while (starredElementTokenizer.hasMoreTokens()) {
                        String starredNextToken = starredElementTokenizer.nextToken();
                        String loggedElementNextToken = loggedElementTokenizer.nextToken();
                        if (starredNextToken.equals(ANY) || starredNextToken.equals(loggedElementNextToken)) {
                            ++internalCount;
                        } else {
                            break;
                        }
                    }
                    if (internalCount == 4) {
                        ++count;
                    }
                    starredElementTokenizer = getTokenized(starredElement);
                }
            }
            return count + initCount;
        }
    }

    private StringTokenizer getTokenized(String string) {
        return new StringTokenizer(string, ".");
    }


    private boolean isAllEventTypesList(List<String> eventTypeist) {
        return eventTypeist.contains(ALL_EVENT_TYPES);
    }

    public boolean hasAssociatedDeviceLifecycleStatesInDeviceTypes(String statesInDeviceTypes) {
        String stateInDeviceType = getDevice().getDeviceType().getId() + ":" + getDevice().getState().getId();
        return parseCommaSeparatedStringToList(statesInDeviceTypes).contains(stateInDeviceType);
    }

    public boolean isClearing(List<String> endDeviceEventTypes) {
        return endDeviceEventTypes.contains(this.getEventTypeMrid());
    }


    protected void getEventDevice(Map<?, ?> rawEvent) {
        Optional<Long> endDeviceId = getLong(rawEvent, ModuleConstants.DEVICE_IDENTIFIER);
        EndDevice endDevice = meteringService.findEndDeviceById(endDeviceId.orElse(0L))
                .orElseThrow(() -> new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_KORE_DEVICE, endDeviceId));
        long amrId = Long.parseLong(endDevice.getAmrId());
        device = deviceService.findDeviceById(amrId).orElseThrow(() -> new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_DEVICE, amrId));
    }

    protected void getEventTimestamp(Map<?, ?> rawEvent) {
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
        getEndDevice().ifPresent(filter::setDevice);
        filter.setStatus(issueService.findStatus(IssueStatus.OPEN).get());
        filter.setStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
        Optional<? extends DeviceAlarm> foundIssue = deviceAlarmService.findAlarms(filter).find()
                .stream().max(Comparator.comparing(Issue::getCreateTime));//It is going to be only zero or one open alarm per device
        if (foundIssue.isPresent()) {
            return Optional.of((OpenIssue) foundIssue.get());
        }
        return Optional.empty();
    }

    protected Optional<Long> getLong(Map<?, ?> map, String key) {
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