package com.elster.jupiter.issue.share.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class IssueGroupFilter {
    private Object key;
    private long to;
    private long from;
    private boolean isAsc = true;
    private Class<?> sourceClass;
    private Set<String> statuses;
    private String groupBy;
    private String assigneeType;
    private long assigneeId;
    private String mrid;
    private String issueType;

    public Object getGroupKey() {
        return key;
    }

    /**
     * Resulting set will contain only one group with the passed key
     * @param key
     * @return the same instanse of filter
     */
    public IssueGroupFilter onlyGroupWithKey(Object key) {
        if (key != null) {
            this.key = getSafeString(key.toString());
        }
        return this;
    }

    public long getTo() {
        return to;
    }

    public IssueGroupFilter to(long to) {
        this.to = to;
        return this;
    }

    public long getFrom() {
        return from;
    }

    public IssueGroupFilter from(long from) {
        this.from = from;
        return this;
    }

    public boolean isAscOrder() {
        return isAsc;
    }

    public IssueGroupFilter setAscOrder(boolean isAsc) {
        this.isAsc = isAsc;
        return this;
    }

    public Class<?> getSourceClass() {
        return sourceClass;
    }

    public IssueGroupFilter using(Class<?> sourceClass) {
        this.sourceClass = sourceClass;
        return this;
    }

    public String getGroupBy() {
        return groupBy;
    }

    /**
     * All issues which are matched other filter parameters will be grouped by this attribute
     * @param groupColumn attribute for grouping
     * @return the same instanse of filter
     */
    public IssueGroupFilter groupBy(String groupColumn) {
        this.groupBy = getSafeString(groupColumn);
        return this;
    }

    public Collection<String> getStatuses() {
        return statuses;
    }

    /**
     * Only issues which have one of passed statuses will be used for grouping and counting
     * @param statuses list which contains keys of allowed statuses
     * @return the same instanse of filter
     */
    public IssueGroupFilter withStatuses(Collection<String> statuses) {
        if (statuses != null) {
            this.statuses = new HashSet<>();
            for (String status : statuses) {
                this.statuses.add(getSafeString(status));
            }
        }
        return this;
    }

    public String getAssigneeType() {
        return assigneeType;
    }

    public IssueGroupFilter withAssignee(long id, String type) {
       return withAssigneeId(id).withAssigneeType(type);
    }

    public IssueGroupFilter withAssigneeType(String type) {
        this.assigneeType = getSafeString(type);
        return this;
    }

    public long getAssigneeId() {
        return assigneeId;
    }

    public IssueGroupFilter withAssigneeId(long id) {
        this.assigneeId = id;
        return this;
    }

    public String getMeterMrid() {
        return mrid;
    }

    public IssueGroupFilter withMeterMrid(String mrid) {
        this.mrid = mrid;
        return this;
    }

    public IssueGroupFilter withIssueType(String issueType) {
        this.issueType = getSafeString(issueType);
        return this;
    }

    public String getIssueType() {
        return issueType;
    }

    private String getSafeString(String in){
        // TODO SQL Injection
        return in;
    }
}
