package com.energyict.mdc.device.alarms.impl.records;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;

import javax.inject.Inject;
import javax.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class OpenDeviceAlarmImpl extends DeviceAlarmImpl implements OpenDeviceAlarm {

    @IsPresent
    private Reference<OpenIssue> baseIssue = ValueReference.absent();

    @Inject
    public OpenDeviceAlarmImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Valid
    private List<OpenDeviceAlarmRelatedEventImpl> deviceAlarmRelatedEvents = new ArrayList<>();

    protected OpenIssue getBaseIssue(){
        return baseIssue.orNull();
    }

    public void setIssue(OpenIssue baseAlarm) {
        this.baseIssue.set(baseAlarm);
    }

    //TODO add and remove event

    public HistoricalDeviceAlarm close(IssueStatus status) {
        this.delete(); // Remove reference to baseIssue
        HistoricalIssue historicalBaseAlarm = getBaseIssue().closeInternal(status);
        HistoricalDeviceAlarmImpl historicalDeviceAlarm = getDataModel().getInstance(HistoricalDeviceAlarmImpl.class);
        historicalDeviceAlarm.setIssue(historicalBaseAlarm);
        historicalDeviceAlarm.copy(this);
        historicalDeviceAlarm.save();
        return historicalDeviceAlarm;
    }

    @Override
    public void addRelatedAlarmEvent(long endDeviceEventTrecordId){

    }

    @Override
    public void removeRelatedAlarmEvent(EndDeviceEvent event, Instant timeStamp) {

    }
}
