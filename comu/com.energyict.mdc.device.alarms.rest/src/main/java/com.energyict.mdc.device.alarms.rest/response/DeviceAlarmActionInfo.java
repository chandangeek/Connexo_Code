package com.energyict.mdc.device.alarms.rest.response;


import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;

import java.util.List;

public class DeviceAlarmActionInfo {

    public long id;
    public String name;
    public List<PropertyInfo> properties;

    public DeviceAlarmActionInfo() {

    }

    public DeviceAlarmActionInfo(DeviceAlarm deviceAlarm, IssueActionType actionType, PropertyValueInfoService propertyValueInfoService) {
        this.id = actionType.getId();
        IssueAction action = actionType.createIssueAction().get();
        this.name = action.getDisplayName();
        action.setIssue(deviceAlarm);
        List<PropertySpec> propertySpecs = action.getPropertySpecs();
        this.properties = propertySpecs != null && !propertySpecs.isEmpty() ? propertyValueInfoService.getPropertyInfos(propertySpecs) : null;
    }

}
