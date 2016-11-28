package com.energyict.mdc.device.alarms.impl.records;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;

import javax.inject.Inject;

public class OpenDeviceAlarmImpl extends DeviceAlarmImpl implements OpenDeviceAlarm {

    @IsPresent
    private Reference<OpenIssue> baseAlarm = ValueReference.absent();

    @Inject
    public OpenDeviceAlarmImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    protected OpenIssue getBaseAlarm(){
        return baseAlarm.orNull();
    }

    public void setIssue(OpenIssue baseAlarm) {
        this.baseAlarm.set(baseAlarm);
    }

    public HistoricalDeviceAlarm close(IssueStatus status) {
        this.delete(); // Remove reference to baseAlarm
        HistoricalIssue historicalBaseAlarm = getBaseAlarm().closeInternal(status);
        HistoricalDeviceAlarmImpl historicalDeviceAlarm = getDataModel().getInstance(HistoricalDeviceAlarmImpl.class);
        historicalDeviceAlarm.setIssue(historicalBaseAlarm);
        historicalDeviceAlarm.copy(this);
        historicalDeviceAlarm.save();
        return historicalDeviceAlarm;
    }
}
