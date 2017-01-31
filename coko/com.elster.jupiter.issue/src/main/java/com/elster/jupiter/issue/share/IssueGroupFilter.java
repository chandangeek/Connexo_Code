/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share;

import com.elster.jupiter.issue.share.entity.AssigneeDetails;
import com.elster.jupiter.issue.share.entity.DueDateRange;

import aQute.bnd.annotation.ProviderType;

import java.util.Collection;
import java.util.List;

@ProviderType
public interface IssueGroupFilter {

    Object getGroupKey();

    IssueGroupFilter onlyGroupWithKey(Object key);

    long getTo();

    IssueGroupFilter to(long to);

    long getFrom();

    IssueGroupFilter from(long from);

    boolean isAscOrder();

    IssueGroupFilter setAscOrder(boolean isAsc);

    Class<?> getSourceClass();

    IssueGroupFilter using(Class<?> sourceClass);

    String getGroupBy();

    IssueGroupFilter groupBy(String groupColumn);

    Collection<String> getStatuses();

    IssueGroupFilter withStatuses(Collection<String> statuses);

    IssueGroupFilter withUserAssignee(long id);

    IssueGroupFilter withAssignee(long id, String type);

    IssueGroupFilter withWorkGroupAssignee(long id);

    String getMeterName();

    List<Long> getUserAssignees();

    List<Long> getWorkGroupAssignees();

    List<DueDateRange> getDueDates();

    List<AssigneeDetails> getAssignees();

    IssueGroupFilter withMeterName(String name);

    IssueGroupFilter withIssueTypes(Collection<String> issueTypes);

    IssueGroupFilter withDueDate(long startTime, long endTime);

    Collection<String> getIssueTypes();

}
