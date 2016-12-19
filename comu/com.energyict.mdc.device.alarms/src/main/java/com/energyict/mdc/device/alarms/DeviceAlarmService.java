package com.energyict.mdc.device.alarms;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;

import java.util.Optional;

public interface DeviceAlarmService {

    String COMPONENT_NAME = "DAL";
    String DEVICE_ALARM = "devicealarm";
    String DEVICE_ALARM_PREFIX = "ALM";

    Optional<? extends DeviceAlarm> findAlarm(long id);

    Optional<? extends  DeviceAlarm> findAndLockDeviceAlarmByIdAndVersion(long id, long version);

    Optional<OpenDeviceAlarm> findOpenAlarm(long id);

    Optional<HistoricalDeviceAlarm> findHistoricalAlarm(long id);

    <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers);

    Finder<? extends DeviceAlarm> findAlarms(DeviceAlarmFilter filter, Class<?>... eagers);

    OpenDeviceAlarm createAlarm(OpenIssue baseIssue, IssueEvent issueEvent);
}