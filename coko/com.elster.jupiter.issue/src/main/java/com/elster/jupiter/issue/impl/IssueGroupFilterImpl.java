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
    private String id;
    private boolean isAsc = true;
    private Class<?> sourceClass;
    private Set<String> statuses;
    private Set<String> reasons;
    private Set<String> clearedStatuses;
    private String groupBy;
    private List<Long> userAssignees;
    private String meterName;
    private List<Long> workGroupAssignees;
    private Set<String> issueTypes;
    private List<DueDateRange> dueDates;

    public IssueGroupFilterImpl() {
        this.statuses = new HashSet<>();
        this.clearedStatuses = new HashSet<>();
        this.userAssignees = new ArrayList<>();
        this.workGroupAssignees = new ArrayList<>();
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

    @Override
    public Collection<String> getReasons() {
        return Collections.unmodifiableSet(reasons);
    }

    @Override
    public Collection<String> getClearedStatuses() {
        return Collections.unmodifiableSet(clearedStatuses);
    }

    /**
     * Only issues which have one of passed statuses will be used for grouping and counting
     * @param statuses list which contains keys of allowed statuses
     * @return the same instanse of filter
     */
    @Override
    public IssueGroupFilterImpl withStatuses(Collection<String> statuses) {
        if (statuses != null && !statuses.isEmpty()) {
            this.statuses = statuses.stream().map(this::getSafeString).collect(Collectors.toSet());
        }
        return this;
    }

    @Override
    public IssueGroupFilterImpl withReasons(Collection<String> reasons) {
        if (reasons != null) {
            this.reasons = reasons.stream().map(this::getSafeString).collect(Collectors.toSet());
        }
        return this;
    }

    @Override
    public IssueGroupFilterImpl withClearedStatuses(Collection<String> clearedStatuses) {
        if (clearedStatuses != null) {
            this.clearedStatuses = clearedStatuses.stream().map(this::getSafeString).collect(Collectors.toSet());
        }
        return this;
    }

    @Override
    public IssueGroupFilter withUserAssignee(long id) {
        this.userAssignees.add(id);
        return this;
    }

    @Override
    public IssueGroupFilter withId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getId(){
        return this.id;
    }

    @Override
    public IssueGroupFilter withWorkGroupAssignee(long id) {
        this.workGroupAssignees.add(id);
        return this;
    }

    @Override
    public IssueGroupFilter withAssignee(long id, String type){
        return this.withUserAssignee(id);
    }

    @Override
    public List<Long> getUserAssignees() {
        return this.userAssignees;
    }

    @Override
    public List<Long> getWorkGroupAssignees() {
        return this.workGroupAssignees;
    }

    @Override
    public List<DueDateRange> getDueDates() {
        return this.dueDates;
    }

    @Override
    public List<AssigneeDetails> getAssignees() {
        return userAssignees.stream().map(userId -> new AssigneeDetails(userId, "USER")).collect(Collectors.toList());
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