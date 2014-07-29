package com.elster.jupiter.issue.datacollection.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.elster.jupiter.issue.datacollection.impl.i18n.MessageSeeds;
import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import org.osgi.service.event.EventConstants;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

public abstract class AbstractEvent implements IssueEvent {
    protected static final Logger LOG = Logger.getLogger(AbstractEvent.class.getName());

    private final IssueService issueService;
    private final MeteringService meteringService;
    private final DeviceDataService deviceDataService;
    private final Thesaurus thesaurus;

    private Device device;
    private EndDevice endDevice;
    private IssueStatus status;
    private DataCollectionEventDescription eventDescription;

    public AbstractEvent(IssueService issueService, MeteringService meteringService, DeviceDataService deviceDataService, Thesaurus thesaurus, Map<?, ?> rawEvent) {
        this.issueService = issueService;
        this.meteringService = meteringService;
        this.deviceDataService = deviceDataService;
        this.thesaurus = thesaurus;
        init(rawEvent);
    }

    protected void init(Map<?, ?> rawEvent) {
        getEventDescriptionByTopic(rawEvent);
        getDefaultIssueStatus();
        getEventDevice(rawEvent);
    }

    private void getEventDescriptionByTopic(Map<?, ?> rawEvent) {
        String topic = String.class.cast(rawEvent.get(EventConstants.EVENT_TOPIC));
        this.eventDescription = DataCollectionEventDescription.getDescriptionByTopic(topic);
    }

    private void getDefaultIssueStatus() {
        Query<IssueStatus> statusQuery = issueService.query(IssueStatus.class);
        List<IssueStatus> statusList = statusQuery.select(where("isFinal").isEqualTo(Boolean.FALSE));
        if (statusList.isEmpty()) {
            LOG.severe("Issue creation failed, because no not-final statuses was found");
        } else {
            this.status = statusList.get(0);
        }
    }

    protected void getEventDevice(Map<?, ?> rawEvent) {
        String amrId = String.class.cast(rawEvent.get(ModuleConstants.DEVICE_IDENTIFIER));
        device = findDeviceByAmrId(amrId);
        if (device != null) {
            endDevice = findEndDeviceByDevice();
        } else {
            throw new UnableToCreateEventException(thesaurus, MessageSeeds.EVENT_BAD_DATA_NO_DEVICE, amrId);
        }
    }

    private Device findDeviceByAmrId(String amrId) {
        long id = 0;
        try {
            id = Long.valueOf(amrId);
        } catch (NumberFormatException e) {
        }
        return getDeviceDataService().findDeviceById(id);
    }

    public EndDevice findEndDeviceByDevice() {
        Query<Meter> meterQuery = meteringService.getMeterQuery();
        List<Meter> meterList = meterQuery.select(where("amrId").isEqualTo(device.getId()));
        if (meterList.size() != 1){
            throw new UnableToCreateEventException(thesaurus, MessageSeeds.EVENT_BAD_DATA_NO_END_DEVICE, device.getId());
        }
        return meterList.get(0);
    }

    protected IssueService getIssueService() {
        return issueService;
    }

    protected MeteringService getMeteringService() {
        return meteringService;
    }

    protected DeviceDataService getDeviceDataService() {
        return deviceDataService;
    }

    protected DataCollectionEventDescription getDescription() {
        return eventDescription;
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
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

    protected abstract int getNumberOfEvents(Device concentrator);

    @Override
    public String getEventType() {
        return eventDescription.getTopic();
    }

    @Override
    public IssueStatus getStatus() {
        return status;
    }

    @Override
    public EndDevice getDevice() {
        return endDevice;
    }

    protected void setEndDevice(EndDevice endDevice) {
        this.endDevice = endDevice;
    }

    protected void setDevice(Device device) {
        this.device = device;
    }

    public double computeCurrentThreshold() {
        Device concentrator = device.getPhysicalGateway();
        if (concentrator == null) {
            LOG.log(Level.WARNING, "Concentrator for device[id={0}] is not found", device.getId());
            return -1;
        }
        int numberOfEvents = getNumberOfEvents(concentrator);
        int numberOfConnectedDevices = concentrator.getPhysicalConnectedDevices().size();
        if (numberOfConnectedDevices == 0) {
            LOG.log(Level.WARNING, "Number of connected devices for concentrator[id={0}] equals 0", concentrator.getId());
            return -1;
        }
        return (double) numberOfEvents / (double) numberOfConnectedDevices * 100.0;
    }


    protected Long getLong(Map<?, ?> map, String key) {
        Object contents = map.get(key);
        return contents instanceof Long ? (Long) contents : ((Integer) contents).longValue();
    }
}
