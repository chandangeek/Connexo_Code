package com.elster.jupiter.issue.share.service;

import java.util.ArrayList;
import java.util.List;

public class GroupQueryBuilder {
    private long id;
    private long to;
    private long from;
    private boolean isAsc = true;
    private Class<?> sourceClass;
    private List<String> statuses;
    private String groupColumn;
    private String assigneeType;
    private long assigneeId;
    private long meterId;
    private String issueType;

    public long getId() {
        return id;
    }

    public GroupQueryBuilder setId(long id) {
        this.id = id;
        return this;
    }

    public long getTo() {
        return to;
    }

    public GroupQueryBuilder setTo(long to) {
        this.to = to;
        return this;
    }

    public long getFrom() {
        return from;
    }

    public GroupQueryBuilder setFrom(long from) {
        this.from = from;
        return this;
    }

    public boolean isAsc() {
        return isAsc;
    }

    public GroupQueryBuilder setAsc(boolean isAsc) {
        this.isAsc = isAsc;
        return this;
    }

    public Class<?> getSourceClass() {
        return sourceClass;
    }

    public GroupQueryBuilder setSourceClass(Class<?> sourceClass) {
        this.sourceClass = sourceClass;
        return this;
    }

    public String getGroupColumn() {
        return groupColumn;
    }

    public GroupQueryBuilder setGroupColumn(String groupColumn) {
        this.groupColumn = getSafeString(groupColumn);
        return this;
    }

    public List<String> getStatuses() {
        return statuses;
    }

    public GroupQueryBuilder setStatuses(List<String> statuses) {
        if (statuses != null) {
            this.statuses = new ArrayList<>();
            for (String status : statuses) {
                this.statuses.add(getSafeString(status));
            }
        }
        return this;
    }

    public String getAssigneeType() {
        return assigneeType;
    }

    public GroupQueryBuilder setAssigneeType(String type) {
        this.assigneeType = getSafeString(type);
        return this;
    }

    public long getAssigneeId() {
        return assigneeId;
    }

    public GroupQueryBuilder setAssigneeId(long id) {
        this.assigneeId = id;
        return this;
    }

    public long getMeterId() {
        return meterId;
    }

    public GroupQueryBuilder setMeterId(long meterId) {
        this.meterId = meterId;
        return this;
    }

    public GroupQueryBuilder setIssueType(String issueType) {
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
