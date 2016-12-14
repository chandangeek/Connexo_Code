package com.energyict.mdc.device.alarms.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.alarms.DeviceAlarmFilter;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.impl.ModuleConstants;
import com.energyict.mdc.device.alarms.impl.event.EventDescription;
import com.energyict.mdc.device.alarms.impl.i18n.MessageSeeds;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;

import com.google.inject.Injector;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class DeviceAlarmEvent implements IssueEvent, Cloneable {
    protected static final Logger LOGGER = Logger.getLogger(DeviceAlarmEvent.class.getName());

    private final DeviceAlarmService deviceAlarmService;
    private final IssueService issueService;
    private final MeteringService meteringService;
    private final DeviceService deviceService;
    private final TopologyService topologyService;
    private final Thesaurus thesaurus;

    private Device device;
    private Instant timestamp;
    private EventDescription eventDescription;
    private Optional<? extends OpenIssue> existingIssue;
    private Injector injector;

    public DeviceAlarmEvent(DeviceAlarmService deviceAlarmService, IssueService issueService, MeteringService meteringService, DeviceService deviceService, TopologyService topologyService, Thesaurus thesaurus, Injector injector) {
        this.deviceAlarmService = deviceAlarmService;
        this.issueService = issueService;
        this.meteringService = meteringService;
        this.deviceService = deviceService;
        this.topologyService = topologyService;
        this.thesaurus = thesaurus;
        this.injector = injector;
    }

    public abstract void init(Map<?, ?> jsonPayload);

    protected TopologyService getTopologyService() {
        return topologyService;
    }

    protected Thesaurus getThesaurus(){
        return this.thesaurus;
    }

    protected EventDescription getDescription() {
        if (eventDescription == null){
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

    public void wrap(Map<?, ?> rawEvent, EventDescription eventDescription, Device device){
        this.eventDescription = eventDescription;
        if(device != null){
            this.device = device;
        } else {
            getEventDevice(rawEvent);
        }
        getEventTimestamp(rawEvent);
        // wrapInternal(rawEvent, eventDescription);
    }

   // protected abstract void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription);

    protected void getEventDevice(Map<?, ?> rawEvent) {
        Optional<Long> amrId = getLong(rawEvent, ModuleConstants.DEVICE_IDENTIFIER);
        device = deviceService.findDeviceById(amrId.orElse(0L)).orElseThrow(() -> new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_DEVICE, amrId));
    }

    protected void getEventTimestamp(Map<?, ?> rawEvent) {
        Long eventTimeSTamp = getLong(rawEvent, ModuleConstants.EVENT_TIMESTAMP).orElseThrow(() -> new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_TIMESTAMP));
        timestamp = Instant.ofEpochSecond(eventTimeSTamp);
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

 /*   protected Instant getLastSuccessfulEndDeviceCreationEnd(Device concentrator) {
        Instant lastSuccessfulEndDeviceCreationTask = Instant.EPOCH;
        for (ConnectionTask<?, ?> task : concentrator.getConnectionTasks()) {
            Instant taskEnd = task.getLastSuccessfulEndDeviceCreationEnd();
            if (taskEnd != null && lastSuccessfulEndDeviceCreationTask.isBefore(taskEnd)) {
                lastSuccessfulEndDeviceCreationTask = taskEnd;
            }
        }
        return lastSuccessfulEndDeviceCreationTask;
    } */

 /*   protected int getNumberOfDevicesWithEvents(Device concentrator) {
        Instant start = getLastSuccessfulEndDeviceCreationEnd(concentrator);
        int numberOfDevicesWithEvents = 0;
        try {
            DeviceAlarmEventDescription description = DeviceAlarmEventDescription.valueOf(this.eventDescription.getUniqueKey());
            if (description != null && description.getErrorType() != null) {
                numberOfDevicesWithEvents = this.getTopologyService().
                        countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(
                                description.getErrorType(),
                                concentrator,
                                Interval.startAt(start));
            }
        } catch (RuntimeException ex){
            LOGGER.log(Level.WARNING, "Incorrect communication type for concentrator[id={0}]", concentrator.getId());
        }
        return numberOfDevicesWithEvents;
    }
*/
    // Used in rule engine
    /*public double computeCurrentThreshold() {
        Optional<Device> concentrator = this.topologyService.getPhysicalGateway(this.device);
        if (!concentrator.isPresent()) {
            LOGGER.log(Level.WARNING, "Concentrator for device[id={0}] is not found", device.getId());
            return -1;
        }
        int numberOfEvents = getNumberOfDevicesWithEvents(concentrator.get());
        int numberOfConnectedDevices = this.topologyService.findPhysicalConnectedDevices(concentrator.get()).size();
        if (numberOfConnectedDevices == 0) {
            LOGGER.log(Level.WARNING, "Number of connected devices for concentrator[id={0}] equals 0", concentrator.get().getId());
            return -1;
        }
        return (double) numberOfEvents / (double) numberOfConnectedDevices * 100.0;
    } */

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
        Optional<? extends DeviceAlarm> foundIssue = deviceAlarmService.findAlarms(filter).find().stream().findFirst();//It is going to be only zero or one open issue per device
        if (foundIssue.isPresent()) {
            return Optional.of((OpenIssue)foundIssue.get());
        }
        return Optional.empty();
    }

    protected abstract Condition getConditionForExistingIssue();

    protected Optional<Long> getLong(Map<?, ?> map, String key) {
        Object contents = map.get(key);
        if (contents == null){
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

    public DeviceAlarmEvent cloneForAggregation(){
        DeviceAlarmEvent clone = this.clone();
        Optional<Device> physicalGateway = this.topologyService.getPhysicalGateway(this.device);
        if (physicalGateway.isPresent()) {
            clone.device = physicalGateway.get();
        }
        else {
            clone.device = null;
        }
        return clone;
    }

    public boolean isResolveEvent(){
        return false;
    }

}