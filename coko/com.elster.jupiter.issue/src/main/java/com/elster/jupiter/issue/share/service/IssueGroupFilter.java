package com.elster.jupiter.issue.share.service;

import java.util.*;

public final class IssueGroupFilter {
    private Object key;
    private long to;
    private long from;
    private boolean isAsc = true;
    private Class<?> sourceClass;
    private Set<String> statuses;
    private String groupBy;
    private List<AssigneeDetails> assignees;
    private String mrid;
    private Set<String> issueTypes;
    private List<DueDateRange> dueDates;
    private List<Long> deviceGroups;

    public class AssigneeDetails {
        private long assigneeId;
        private String assigneeType;

        public AssigneeDetails(long assigneeId, String assigneeType) {
            this.assigneeId = assigneeId;
            this.assigneeType = assigneeType;
        }

        public String getAssigneeType() {
            return assigneeType;
        }

        public void setAssigneeType(String assigneeType) {
            this.assigneeType = assigneeType;
        }

        public long getAssigneeId() {
            return assigneeId;
        }

        public void setAssigneeId(long assigneeId) {
            this.assigneeId = assigneeId;
        }
    }

    public class DueDateRange {
        private long startTime;
        private long endTime;

        public DueDateRange(long startTime, long endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }
    }

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
            statuses.stream().forEach(st -> this.statuses.add(getSafeString(st)));
        }
        return this;
    }

    public IssueGroupFilter withAssignee(long id, String type) {
        if (this.assignees == null) {
            this.assignees = new ArrayList<>();
        }
        this.assignees.add(new IssueGroupFilter.AssigneeDetails(id, type));
        return this;
    }

    public List<AssigneeDetails> getAssignees() {
        return this.assignees;
    }

    public List<DueDateRange> getDueDates() {
        return this.dueDates;
    }

    public List<Long> getDeviceGroups() {
        return deviceGroups;
    }

    public IssueGroupFilter withDeviceGroups(List<Long> deviceGroups) {
        if (deviceGroups != null) {
            this.deviceGroups = new ArrayList<>();
        }
        this.deviceGroups = deviceGroups;
        return this;
    }

    public String getMeterMrid() {
        return mrid;
    }

    public IssueGroupFilter withMeterMrid(String mrid) {
        this.mrid = mrid;
        return this;
    }

    public IssueGroupFilter withIssueTypes(Collection<String> issueTypes) {
        if (issueTypes != null) {
            this.issueTypes = new HashSet<>();
            issueTypes.stream().forEach(it -> this.issueTypes.add(getSafeString(it)));
        }
        return this;
    }

    public IssueGroupFilter withDueDate(long startTime, long endTime) {
        if (this.dueDates == null) {
            this.dueDates = new ArrayList<>();
        }
        this.dueDates.add(new IssueGroupFilter.DueDateRange(startTime, endTime));
        return this;
    }

    public Collection<String> getIssueTypes() {
        return issueTypes;
    }

    private String getSafeString(String in){
        // TODO SQL Injection
        return in;
    }
}
