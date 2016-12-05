package com.energyict.mdc.device.alarms.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;

public class DeviceAlarmInfo<T extends DeviceInfo> extends IssueInfo<T, DeviceAlarm> {

    public String deviceMRID;
    public String usagePointMRID;
    public String location;
    public boolean clearedStatus;
    public IdWithNameInfo logBook;

    public DeviceAlarmInfo(DeviceAlarm deviceAlarm, Class<T> deviceInfoClass){
        super(deviceAlarm, deviceInfoClass);
    }

}
