package com.energyict.mdc.device.alarms.impl.records;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;

import javax.inject.Inject;

public class HistoricalDeviceAlarmImpl extends DeviceAlarmImpl implements HistoricalDeviceAlarm {

    @IsPresent
    private Reference<HistoricalIssue> baseAlarm = ValueReference.absent();

    @Inject
    public HistoricalDeviceAlarmImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    protected HistoricalIssue getBaseAlarm(){
        return baseAlarm.orNull();
    }

    void setIssue(HistoricalIssue issue) {
        this.baseAlarm.set(issue);
    }

    void copy(OpenDeviceAlarm source) {
        this.setId(source.getId());
        this.setDeviceMRID(source.getDeviceMRID());
    }
}
