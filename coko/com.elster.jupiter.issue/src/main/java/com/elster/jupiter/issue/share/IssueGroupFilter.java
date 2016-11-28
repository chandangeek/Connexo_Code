package com.elster.jupiter.issue.share;

import com.elster.jupiter.issue.share.entity.AssigneeDetails;
import com.elster.jupiter.issue.share.entity.DueDateRange;

import java.util.Collection;
import java.util.List;


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

    IssueGroupFilter withAssignee(long id, String type);

    List<AssigneeDetails> getAssignees();

    List<DueDateRange> getDueDates();

    String getMeterName();

    IssueGroupFilter withMeterName(String name);

    IssueGroupFilter withIssueTypes(Collection<String> issueTypes);

    IssueGroupFilter withDueDate(long startTime, long endTime);

    Collection<String> getIssueTypes();
}
