package com.energyict.mdc.issue.datacollection.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;

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
    private final DeviceService deviceService;
    private final Thesaurus thesaurus;

    private Device device;
    private EndDevice koreDevice;
    private IssueStatus status;
    private DataCollectionEventDescription eventDescription;

    protected AbstractEvent(IssueService issueService, MeteringService meteringService, DeviceService deviceService, Thesaurus thesaurus) {
        this.issueService = issueService;
        this.meteringService = meteringService;
        this.deviceService = deviceService;
        this.thesaurus = thesaurus;
    }

    public void init(Map<?, ?> rawEvent, DataCollectionEventDescription eventDescription) {
        this.eventDescription = eventDescription;
        getDefaultIssueStatus();
        getEventDevice(rawEvent);
    }

    private void getDefaultIssueStatus() {
        Query<IssueStatus> statusQuery = issueService.query(IssueStatus.class);
        List<IssueStatus> statusList = statusQuery.select(where("isHistorical").isEqualTo(Boolean.FALSE));
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
            koreDevice = findKoreDeviceByDevice();
        } else {
            throw new UnableToCreateEventException(thesaurus, MessageSeeds.EVENT_BAD_DATA_NO_DEVICE, amrId);
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
        Query<Meter> meterQuery = meteringService.getMeterQuery();
        List<Meter> meterList = meterQuery.select(where("amrId").isEqualTo(device.getId()));
        if (meterList.size() != 1) {
            throw new UnableToCreateEventException(thesaurus, MessageSeeds.EVENT_BAD_DATA_NO_KORE_DEVICE, device.getId());
        }
        return meterList.get(0);
    }

    protected IssueService getIssueService() {
        return issueService;
    }

    protected MeteringService getMeteringService() {
        return meteringService;
    }

    protected DeviceService getDeviceService() {
        return deviceService;
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

    protected abstract int getNumberOfDevicesWithEvents(Device concentrator);

    @Override
    public String getEventType() {
        return eventDescription.getTopic();
    }

    @Override
    public IssueStatus getStatus() {
        return status;
    }

    @Override
    public EndDevice getKoreDevice() {
        return koreDevice;
    }

    protected void setKoreDevice(EndDevice koreDevice) {
        this.koreDevice = koreDevice;
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
        int numberOfEvents = getNumberOfDevicesWithEvents(concentrator);
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

    @Override
    protected AbstractEvent clone() {
        AbstractEvent clone = cloneInternal();
        clone.device = device;
        clone.koreDevice = koreDevice;
        clone.status = status;
        clone.eventDescription = eventDescription;
        return clone;
    }

    public AbstractEvent cloneForAggregation() {
        AbstractEvent clone = clone();
        clone.device = device.getPhysicalGateway();
        clone.koreDevice = clone.findKoreDeviceByDevice();
        return clone;
    }

    protected abstract AbstractEvent cloneInternal();
}
