/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.records;

import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.nls.Thesaurus;

public class DeviceAlarmGroupImpl implements IssueGroup {
    private Thesaurus thesaurus;

    private Object groupKey;
    private String groupName;
    private long count;

    public DeviceAlarmGroupImpl() {
    }

    public DeviceAlarmGroupImpl(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public DeviceAlarmGroupImpl init(Object key, String reason, long count) {
        this.groupKey = key;
        this.groupName = reason;
        this.count = count;
        return this;
    }

    public long getCount() {
        return count;
    }

    public String getGroupName() {
        return groupName;
    }

    public Object getGroupKey() {
        return groupKey;
    }
}
