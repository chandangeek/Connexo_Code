/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.records;

import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;

public class DeviceAlarmAssigneeImpl implements IssueAssignee {

    private User user;
    private WorkGroup workGroup;

    public DeviceAlarmAssigneeImpl() {
    }

    public DeviceAlarmAssigneeImpl(User user, WorkGroup workGroup) {
        this.user = user;
        this.workGroup = workGroup;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public WorkGroup getWorkGroup() {
        return workGroup;
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void setWorkGroup(WorkGroup workGroup) {
        this.workGroup = workGroup;
    }

    @Override
    public long getId() {
        return user.getId();
    }

    @Override
    public String getType() {
        return Types.USER;
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public long getVersion() {
        return user.getVersion();
    }
}