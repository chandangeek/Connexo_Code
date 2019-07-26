/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.IssueGroupFilter;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import org.osgi.framework.BundleContext;
import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface DeviceAlarmService {

    String COMPONENT_NAME = "DAL";
    String DEVICE_ALARM = "devicealarm";
    String DEVICE_ALARM_PREFIX = "ALM";

    Optional<? extends DeviceAlarm> findAlarm(long id);

    Optional<BundleContext> getBundleContext();

    Optional<? extends  DeviceAlarm> findAndLockDeviceAlarmByIdAndVersion(long id, long version);

    Optional<OpenDeviceAlarm> findOpenAlarm(long id);

    Optional<HistoricalDeviceAlarm> findHistoricalAlarm(long id);

    <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers);

    Finder<? extends DeviceAlarm> findAlarms(DeviceAlarmFilter filter, Class<?>... eagers);

    OpenDeviceAlarm createAlarm(OpenIssue baseIssue, IssueEvent issueEvent);

    List<IssueGroup> getDeviceAlarmGroupList(IssueGroupFilter builder);

    /**
     * Finds all open device alarms with the specific condition.
     *
     * @return a list of all open device alarms with the specific condition in the system
     */
    Finder<OpenDeviceAlarm> findOpenDeviceAlarms(Condition condition);

    Thesaurus thesaurus();

}