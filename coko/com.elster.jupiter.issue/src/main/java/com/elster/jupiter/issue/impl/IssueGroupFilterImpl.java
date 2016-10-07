package com.elster.jupiter.issue.impl;

import com.elster.jupiter.issue.share.IssueGroupFilter;
import com.elster.jupiter.issue.share.entity.AssigneeDetails;
import com.elster.jupiter.issue.share.entity.DueDateRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class IssueGroupFilterImpl implements IssueGroupFilter {
    private Object key;
    private long to;
    private long from;
    private boolean isAsc = true;
    private Class<?> sourceClass;
    private Set<String> statuses;
    private String groupBy;
    private List<AssigneeDetails> assignees;
    private String meterName;
    private Set<String> issueTypes;
    private List<DueDateRange> dueDates;

    public IssueGroupFilterImpl() {
        this.statuses = new HashSet<>();
        this.assignees = new ArrayList<>();
        this.issueTypes = new HashSet<>();
        this.dueDates = new ArrayList<>();
    }

    @Override
    public Object getGroupKey() {
        return key;
    }

    /**
     * Resulting set will contain only one group with the passed key
     * @param key
     * @return the same instanse of filter
     */
    @Override
    public IssueGroupFilterImpl onlyGroupWithKey(Object key) {
        if (key != null) {
            this.key = getSafeString(key.toString());
        }
        return this;
    }

    @Override
    public long getTo() {
        return to;
    }

    @Override
    public IssueGroupFilter to(long to) {
        this.to = to;
        return this;
    }

    @Override
    public long getFrom() {
        return from;
    }

    @Override
    public IssueGroupFilter from(long from) {
        this.from = from;
        return this;
    }

    @Override
    public boolean isAscOrder() {
        return isAsc;
    }

    @Override
    public IssueGroupFilterImpl setAscOrder(boolean isAsc) {
        this.isAsc = isAsc;
        return this;
    }

    @Override
    public Class<?> getSourceClass() {
        return sourceClass;
    }

    @Override
    public IssueGroupFilterImpl using(Class<?> sourceClass) {
        this.sourceClass = sourceClass;
        return this;
    }

    @Override
    public String getGroupBy() {
        return groupBy;
    }

    /**
     * All issues which are matched other filter parameters will be grouped by this attribute
     * @param groupColumn attribute for grouping
     * @return the same instanse of filter
     */
    @Override
    public IssueGroupFilterImpl groupBy(String groupColumn) {
        this.groupBy = getSafeString(groupColumn);
        return this;
    }

    @Override
    public Collection<String> getStatuses() {
        return Collections.unmodifiableSet(statuses);
    }

    /**
     * Only issues which have one of passed statuses will be used for grouping and counting
     * @param statuses list which contains keys of allowed statuses
     * @return the same instanse of filter
     */
    @Override
    public IssueGroupFilterImpl withStatuses(Collection<String> statuses) {
        if (statuses != null) {
            this.statuses = statuses.stream().map(this::getSafeString).collect(Collectors.toSet());
        }
        return this;
    }

    @Override
    public IssueGroupFilter withAssignee(long id, String type) {
        this.assignees.add(new AssigneeDetails(id, type));
        return this;
    }

    @Override
    public List<AssigneeDetails> getAssignees() {
        return this.assignees;
    }

    @Override
    public List<DueDateRange> getDueDates() {
        return this.dueDates;
    }

    @Override
    public String getMeterName() {
        return meterName;
    }

    @Override
    public IssueGroupFilterImpl withMeterName(String name) {
        this.meterName = name;
        return this;
    }

    @Override
    public IssueGroupFilterImpl withIssueTypes(Collection<String> issueTypes) {
        if (issueTypes != null) {
            this.issueTypes = issueTypes.stream().map(this::getSafeString).collect(Collectors.toSet());
        }
        return this;
    }

    @Override
    public IssueGroupFilter withDueDate(long startTime, long endTime) {
        this.dueDates.add(new DueDateRange(startTime, endTime));
        return this;
    }

    @Override
    public Collection<String> getIssueTypes() {
        return Collections.unmodifiableSet(issueTypes);
    }

    private String getSafeString(String in) {
        // TODO SQL Injection
        return in;
    }

}