package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.AbstractEvent;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.UnableToCreateEventException;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;
import com.google.common.base.Optional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

public abstract class DataCollectionEvent implements IssueEvent {
    protected static final Logger LOG = Logger.getLogger(AbstractEvent.class.getName());

    private final IssueDataCollectionService issueDataCollectionService;
    private final IssueService issueService;
    private final MeteringService meteringService;
    private final CommunicationTaskService communicationTaskService;
    private final DeviceService deviceService;
    private final Thesaurus thesaurus;

    private Device device;
    private EndDevice koreDevice;
    private IssueStatus status;
    private DataCollectionEventDescription eventDescription;

    public DataCollectionEvent(IssueDataCollectionService issueDataCollectionService, IssueService issueService, MeteringService meteringService, DeviceService deviceService, CommunicationTaskService communicationTaskService, Thesaurus thesaurus) {
        this.issueDataCollectionService = issueDataCollectionService;
        this.issueService = issueService;
        this.meteringService = meteringService;
        this.communicationTaskService = communicationTaskService;
        this.deviceService = deviceService;
        this.thesaurus = thesaurus;
    }

    protected IssueService getIssueService() {
        return issueService;
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

    protected DataCollectionEventDescription getDescription() {
        if (eventDescription == null){
            throw new IllegalStateException("You are trying to get event description for not initialized event");
        }
        return eventDescription;
    }

    public void wrap(Map<?, ?> rawEvent, DataCollectionEventDescription eventDescription){
        this.eventDescription = eventDescription;
        setDefaultIssueStatus();
        getEventDevice(rawEvent);
    }

    private void setDefaultIssueStatus() {
        Optional<IssueStatus> statusRef = getIssueService().findStatus(IssueStatus.OPEN);
        if (!statusRef.isPresent()) {
            throw new UnableToCreateEventException(thesaurus, MessageSeeds.EVENT_BAD_DATA_NO_STATUS);
        } else {
            this.status = statusRef.get();
        }
    }

    protected void getEventDevice(Map<?, ?> rawEvent) {
        String amrId = String.class.cast(rawEvent.get(ModuleConstants.DEVICE_IDENTIFIER));
        device = findDeviceByAmrId(amrId);
        if (device != null) {
            koreDevice = findKoreDeviceByDevice();
        } else {
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_DEVICE, amrId);
        }
    }

    private Device findDeviceByAmrId(String amrId) {
        long id = 0;
        try {
            id = Long.parseLong(amrId);
        } catch (NumberFormatException e) {
        }
        return getDeviceService().findDeviceById(id);
    }

    public EndDevice findKoreDeviceByDevice() {
        Query<Meter> meterQuery = getMeteringService().getMeterQuery();
        List<Meter> meterList = meterQuery.select(where("amrId").isEqualTo(device.getId()));
        if (meterList.size() != 1) {
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_KORE_DEVICE, device.getId());
        }
        return meterList.get(0);
    }

    protected Date getLastSuccessfulCommunicationEnd(Device concentrator) {
        Date lastSuccessfulCommTask = new Date(0);

        for (ConnectionTask<?, ?> task : concentrator.getConnectionTasks()) {
            Date taskEnd = task.getLastSuccessfulCommunicationEnd();
            if (taskEnd != null && lastSuccessfulCommTask.before(taskEnd)) {
                lastSuccessfulCommTask = taskEnd;
            }
        }
        return lastSuccessfulCommTask;
    }

    protected int getNumberOfDevicesWithEvents(Device concentrator) {
        Date start = getLastSuccessfulCommunicationEnd(concentrator);
        int numberOfDevicesWithEvents = 0;
        try {
            numberOfDevicesWithEvents = this.getCommunicationTaskService().countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(getDescription().getErrorType(), concentrator, Interval.startAt(start));
        } catch (RuntimeException ex){
            LOG.log(Level.WARNING, "Incorrect communication type for concentrator[id={0}]", concentrator.getId());
        }
        return numberOfDevicesWithEvents;
    }

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
    public Optional<? extends Issue> findExistingIssue(Issue baseIssue) {
        Query<OpenIssueDataCollection> query = issueDataCollectionService.query(OpenIssueDataCollection.class, OpenIssue.class);
        List<OpenIssueDataCollection> issues = query.select(where("baseIssue.rule").isEqualTo(baseIssue.getRule()).and(where("baseIssue.device").isEqualTo(getKoreDevice())));
        if (issues != null && issues.size() > 0){
            return Optional.of(issues.get(0));
        }
        return Optional.absent();
    }

    @Override
    public String getEventType() {
        return this.getDescription().name();
    }

    @Override
    public IssueStatus getStatus() {
        return status;
    }

    @Override
    public EndDevice getKoreDevice() {
        return koreDevice;
    }
}
