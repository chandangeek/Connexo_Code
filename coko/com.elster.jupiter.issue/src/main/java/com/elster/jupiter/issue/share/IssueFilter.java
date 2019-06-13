/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share;

import com.elster.jupiter.issue.share.entity.DueDateRange;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface IssueFilter {

    Optional<String> getIssueId();

    void setIssueId(String issueId);

    void setUnassignedSelected();

    void setUnassignedWorkGroupSelected();

    void addDevice(EndDevice device);

    void addLocation(Location location);

    void addUsagePoint(UsagePoint usagePoint);

    void setIssueReason(IssueReason issueReason);

    void setPriority(Priority priority);

    void addStatus(IssueStatus status);

    void addAssignee(User assignee);

    void addWorkGroupAssignee(WorkGroup workGroup);

    List<User> getAssignees();

    List<WorkGroup> getWorkGroupAssignees();

    boolean isUnassignedSelected();

    boolean isUnassignedWorkGroupSelected();

    List<EndDevice> getDevices();

    List<Location> getLocations();

    List<UsagePoint> getUsagePoints();

    List<IssueReason> getIssueReasons();

    List<IssueStatus> getStatuses();

    List<DueDateRange> getDueDates();

    void addDueDate(long startTime, long endTime);

    List<IssueType> getIssueTypes();

    void addIssueType(IssueType issueType);

    List<Priority> getPriorities();

    Long getStartCreateTime();

    void setStartCreateTime(Long startCreateTime);

    Long getEndCreateTime();

    void setEndCreateTime(Long endCreateTime);

}