package com.elster.jupiter.issue.share;

import com.elster.jupiter.issue.share.entity.DueDateRange;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.users.User;

import java.util.List;

public interface IssueFilter {

    void setUnassignedSelected();

    void addDevice(EndDevice device);

    void setIssueReason(IssueReason issueReason);

    void addStatus(IssueStatus status);

    void addAssignee(User assignee);

    List<User> getAssignees();

    boolean isUnassignedSelected();

    List<EndDevice> getDevices();

    List<IssueReason> getIssueReasons();

    List<IssueStatus> getStatuses();

    List<DueDateRange> getDueDates();

    void addDueDate(long startTime, long endTime);

    List<IssueType> getIssueTypes();

    void addIssueType(IssueType issueType);
}
