package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;
import com.google.inject.Injector;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DataCollectionEvent implements IssueEvent, Cloneable {
    protected static final Logger LOG = Logger.getLogger(DataCollectionEvent.class.getName());

    private final IssueDataCollectionService issueDataCollectionService;
    private final MeteringService meteringService;
    private final CommunicationTaskService communicationTaskService;
    private final DeviceService deviceService;
    private final TopologyService topologyService;
    private final Thesaurus thesaurus;

    private Device device;
    private EventDescription eventDescription;
    private Optional<? extends Issue> existingIssue;
    private Injector injector;

    public DataCollectionEvent(IssueDataCollectionService issueDataCollectionService, MeteringService meteringService, DeviceService deviceService, CommunicationTaskService communicationTaskService, TopologyService topologyService, Thesaurus thesaurus, Injector injector) {
        this.issueDataCollectionService = issueDataCollectionService;
        this.meteringService = meteringService;
        this.communicationTaskService = communicationTaskService;
        this.deviceService = deviceService;
        this.topologyService = topologyService;
        this.thesaurus = thesaurus;
        this.injector = injector;
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

    public void wrap(Map<?, ?> rawEvent, EventDescription eventDescription){
        this.eventDescription = eventDescription;
        getEventDevice(rawEvent);
        wrapInternal(rawEvent, eventDescription);
    }

    protected abstract void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription);

    protected void getEventDevice(Map<?, ?> rawEvent) {
        Optional<Long> amrId = getLong(rawEvent, ModuleConstants.DEVICE_IDENTIFIER);
        device = getDeviceService().findDeviceById(amrId.orElse(0L)).orElseThrow(() -> new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_DEVICE, amrId));
    }

    private EndDevice findEndDeviceByMdcDevice() {
        Optional<AmrSystem> amrSystemRef = getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId());
        if (amrSystemRef.isPresent()){
            Optional<Meter> meterRef = amrSystemRef.get().findMeter(String.valueOf(device.getId()));
            if (meterRef.isPresent()){
                return meterRef.get();
            }
        }
        throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_KORE_DEVICE, device.getId());
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
        } catch (RuntimeException ex){
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

    @Override
    public String getEventType() {
        return eventDescription.getUniqueKey();
    }

    @Override
    public EndDevice getEndDevice() {
        return findEndDeviceByMdcDevice();
    }

    public Device getDevice() {
        return device;
    }

    @Override
    public Optional<? extends Issue> findExistingIssue() {
        if (existingIssue == null) {
            Query<OpenIssueDataCollection> query = getIssueDataCollectionService().query(OpenIssueDataCollection.class, ConnectionTask.class, ComTaskExecution.class, ComSession.class);
            List<OpenIssueDataCollection> theSameIssues = query.select(getConditionForExistingIssue());
            if (!theSameIssues.isEmpty()) {
                existingIssue = Optional.of(theSameIssues.get(0));
            } else {
                existingIssue = Optional.empty();
            }
        }
        return existingIssue;
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
    public DataCollectionEvent clone() {
        DataCollectionEvent clone = injector.getInstance(eventDescription.getEventClass());
        clone.eventDescription = eventDescription;
        clone.device = device;
        return clone;
    }

    public DataCollectionEvent cloneForAggregation(){
        DataCollectionEvent clone = this.clone();
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