package com.elster.jupiter.issue.share;

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

    IssueGroupFilter withUserAssignee(long id);

    IssueGroupFilter withWorkGroupAssignee(long id);

    List<Long> getUserAssignees();

    List<Long> getWorkGroupAssignees();

    List<DueDateRange> getDueDates();

    String getMeterMrid();

    IssueGroupFilter withMeterMrid(String mrid);

    IssueGroupFilter withIssueTypes(Collection<String> issueTypes);

    IssueGroupFilter withDueDate(long startTime, long endTime);

    Collection<String> getIssueTypes();

}
