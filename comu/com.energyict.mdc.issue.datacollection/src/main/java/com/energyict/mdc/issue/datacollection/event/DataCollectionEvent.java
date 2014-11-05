package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.metering.AmrSystem;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.UnableToCreateEventException;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.Interval;
import com.google.inject.Injector;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DataCollectionEvent implements IssueEvent, Cloneable {
    protected static final Logger LOG = Logger.getLogger(DataCollectionEvent.class.getName());
    private static final int MDC_AMR_ID = 1;

    private final IssueDataCollectionService issueDataCollectionService;
    private final IssueService issueService;
    private final MeteringService meteringService;
    private final CommunicationTaskService communicationTaskService;
    private final DeviceService deviceService;
    private final Thesaurus thesaurus;

    private Device device;
    private EndDevice endDevice;
    private IssueStatus status;
    private EventDescription eventDescription;
    private Optional<? extends Issue> existingIssue;
    private Injector injector;

    public DataCollectionEvent(IssueDataCollectionService issueDataCollectionService, IssueService issueService, MeteringService meteringService, DeviceService deviceService, CommunicationTaskService communicationTaskService, Thesaurus thesaurus, Injector injector) {
        this.issueDataCollectionService = issueDataCollectionService;
        this.issueService = issueService;
        this.meteringService = meteringService;
        this.communicationTaskService = communicationTaskService;
        this.deviceService = deviceService;
        this.thesaurus = thesaurus;
        this.injector = injector;
    }

    protected IssueService getIssueService() {
        return issueService;
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

    protected void setEndDevice(EndDevice endDevice) {
        this.endDevice = endDevice;
    }

    protected void setEventDescription(EventDescription eventDescription) {
        this.eventDescription = eventDescription;
    }

    public void wrap(Map<?, ?> rawEvent, EventDescription eventDescription){
        this.eventDescription = eventDescription;
        setDefaultIssueStatus();
        getEventDevice(rawEvent);
        wrapInternal(rawEvent, eventDescription);
    }

    protected abstract void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription);

    protected void setDefaultIssueStatus() {
        Optional<IssueStatus> statusRef = getIssueService().findStatus(IssueStatus.OPEN);
        if (!statusRef.isPresent()) {
            throw new UnableToCreateEventException(thesaurus, MessageSeeds.EVENT_BAD_DATA_NO_STATUS);
        } else {
            this.status = statusRef.get();
        }
    }

    protected void getEventDevice(Map<?, ?> rawEvent) {
        Optional<Long> amrId = getLong(rawEvent, ModuleConstants.DEVICE_IDENTIFIER);
        device = getDeviceService().findDeviceById(amrId.orElse(0L));
        if (device != null) {
            endDevice = findEndDeviceByMdcDevice();
        } else {
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_DEVICE, amrId);
        }
    }

    public EndDevice findEndDeviceByMdcDevice() {
        Optional<AmrSystem> amrSystemRef = getMeteringService().findAmrSystem(MDC_AMR_ID);
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
                numberOfDevicesWithEvents = this.getCommunicationTaskService().
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

    @SuppressWarnings("unused")
    // Used in rule engine
    public double computeCurrentThreshold() {
        Device concentrator = device.getPhysicalGateway();
        if (concentrator == null) {
            LOG.log(Level.WARNING, "Concentrator for device[id={0}] is not found", device.getId());
            return -1;
        }
        int numberOfEvents = getNumberOfDevicesWithEvents(concentrator);
        int numberOfConnectedDevices = concentrator.getPhysicalConnectedDevices().size();
        if (numberOfConnectedDevices == 0) {
            LOG.log(Level.WARNING, "Number of connected devices for concentrator[id={0}] equals 0", concentrator.getId());
            return -1;
        }
        return (double) numberOfEvents / (double) numberOfConnectedDevices * 100.0;
    }

    @Override
    public String getEventType() {
        return eventDescription.getUniqueKey();
    }

    @Override
    public IssueStatus getStatus() {
        return status;
    }

    @Override
    public EndDevice getKoreDevice() {
        return endDevice;
    }

    public Device getDevice() {
        return device;
    }

    @Override
    public Optional<? extends Issue> findExistingIssue() {
        if (existingIssue == null) {
            Query<OpenIssueDataCollection> query = getIssueDataCollectionService().query(OpenIssueDataCollection.class, ComSession.class);
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
        clone.endDevice = endDevice;
        clone.device = device;
        return clone;
    }

    @SuppressWarnings("unused")
    public DataCollectionEvent cloneForAggregation(){
        DataCollectionEvent clone = this.clone();
        clone.device = device.getPhysicalGateway();
        if (clone.device != null) {
            clone.endDevice = findEndDeviceByMdcDevice();
        } else {
            clone.endDevice = null;
        }
        return clone;
    }

    public boolean isResolveEvent(){
        return false;
    }
}
