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
import com.energyict.mdc.device.alarms.DeviceAlarmFilter;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.impl.ModuleConstants;
import com.energyict.mdc.device.alarms.impl.event.EventDescription;
import com.energyict.mdc.device.alarms.impl.i18n.MessageSeeds;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeMapping;

import com.google.common.collect.Range;
import com.google.inject.Injector;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class DeviceAlarmEvent implements IssueEvent, Cloneable {
    protected static final Logger LOGGER = Logger.getLogger(DeviceAlarmEvent.class.getName());

    private final DeviceAlarmService deviceAlarmService;
    private final IssueService issueService;
    private final MeteringService meteringService;
    private final DeviceService deviceService;
    private final Thesaurus thesaurus;

    private Device device;
    private Instant timestamp;
    private EventDescription eventDescription;
    private Injector injector;

    public DeviceAlarmEvent(DeviceAlarmService deviceAlarmService, IssueService issueService, MeteringService meteringService, DeviceService deviceService, Thesaurus thesaurus, Injector injector) {
        this.deviceAlarmService = deviceAlarmService;
        this.issueService = issueService;
        this.meteringService = meteringService;
        this.deviceService = deviceService;
        this.thesaurus = thesaurus;
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

    public void wrap(Map<?, ?> rawEvent, EventDescription eventDescription, Device device) {
        this.eventDescription = eventDescription;
        if (device != null) {
            this.device = device;
        } else {
            getEventDevice(rawEvent);
        }
        getEventTimestamp(rawEvent);
    }

    //TODO- use clock, use lists for triggeringEndDeviceEventTypes and deviceTypes
    public int computeOccurenceCount(int ruleId, String range, String logOnSameAlarm, String triggeringEndDeviceEventTypes, String clearingEndDeviceEventTypes, String deviceTypes, String eisCodes) {
        List<String> inputTriggeringEventTypeList = getUpdatedEventTypeMrid(Arrays.asList(triggeringEndDeviceEventTypes.split(",")).stream().collect(Collectors.toList()),
                Arrays.asList(deviceTypes.split(",")).stream().collect(Collectors.toList()));
        if (!inputTriggeringEventTypeList.contains(this.getEventTypeMrid())) {
            return -1;
        }

        List<String> inputClearingEventTypeList = getUpdatedEventTypeMrid(Arrays.asList(clearingEndDeviceEventTypes.split(",")).stream().collect(Collectors.toList()),
                Arrays.asList(deviceTypes.split(",")).stream().collect(Collectors.toList()));
        if (inputClearingEventTypeList.contains(this.getEventTypeMrid())) {
            if (Integer.parseInt(logOnSameAlarm) == 1 &&
                    // issueService.getIssueCreationService().findCreationRuleById(Long.parseLong(ruleId)).isPresent() &&
                    issueService.findOpenIssuesForDevice(getDevice().getName()).find().stream().filter(issue -> issue.getRule().getId() == ruleId).findAny().isPresent()) {
                return Integer.MAX_VALUE;
            } else {
                return -1;
            }
        }

        List<EndDeviceEventRecord> loggedEvents = getDevice().getLogBooks().stream()
                .map(logBook -> logBook.getEndDeviceEvents(Range.closed(Instant.ofEpochMilli(Instant.now().toEpochMilli() - Long.valueOf(range)), Instant.now())))
                .flatMap(Collection::stream).collect(Collectors.toList());
        List<String> currentList = loggedEvents.stream().map(event -> event.getEventType().getMRID())
                .collect(Collectors.toList());
        List<String> currentEISCodeList = loggedEvents.stream()
                .filter(event -> event.getEventType().getMRID().equals(EndDeviceEventTypeMapping.OTHER.getEndDeviceEventTypeMRID()))
                .map(EndDeviceEventRecord::getType)
                .collect(Collectors.toList());
        List<String> eisCodesList = Arrays.asList(eisCodes.split(",")).stream().collect(Collectors.toList());
        return currentList.stream()
                .filter(inputTriggeringEventTypeList::contains)
                .filter(eventTypes -> !eventTypes.equals(EndDeviceEventTypeMapping.OTHER.getEndDeviceEventTypeMRID()))
                .collect(Collectors.toList()).size() +
                currentEISCodeList.stream()
                        .filter(eisCodesList::contains)
                        .collect(Collectors.toList()).size();
    }

    private List<String> getUpdatedEventTypeMrid(List<String> endDeviceEventTypes, List<String> deviceTypes) {
        List<String> inputEventTypeList = new ArrayList<>();
        if (deviceTypes != null && !deviceTypes.isEmpty()) {
            for (String devType : deviceTypes) {
                StringBuilder sb = new StringBuilder();
                sb.append(devType).append(".");
                for (String inputEventType : endDeviceEventTypes) {
                    inputEventTypeList.add(sb.toString() + inputEventType.substring(inputEventType.indexOf(".") + 1));
                }
            }
        } else {
            inputEventTypeList = endDeviceEventTypes.stream().collect(Collectors.toList());
        }
        return inputEventTypeList;
    }

    public boolean hasAssociatedDeviceLifecycleState(String avaliableDeviceLifecycleState) {
        long deviceStateId = getDevice().getState().getId();
        Optional<String> foundAssociatedState = Arrays.asList(avaliableDeviceLifecycleState.split(","))
                .stream().collect(Collectors.collectingAndThen(Collectors.toList(), Collection::stream))
                .filter(state -> Long.parseLong(state) == deviceStateId).findFirst();
        if(foundAssociatedState.isPresent()){
            return true;
        }else{
            return false;
        }
    }

    public String getEISCode() {
        //TODO - update this to code to retrieve EIS code
        return this.getEventType().contains(".") ? "-1" : this.getEventType();
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