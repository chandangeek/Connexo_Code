package com.elster.jupiter.issue.share.service;

import java.util.List;

public class GroupQueryBuilder {
    private long id;
    private long to;
    private long from;
    private boolean isAsc = true;
    private Class<?> sourceClass;
    private List<String> statuses;
    private String groupColumn;

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
        this.groupColumn = groupColumn;
        return this;
    }

    public List<String> getStatuses() {
        return statuses;
    }

    public GroupQueryBuilder setStatuses(List<String> statuses) {
        this.statuses = statuses;
        return this;
    }
}
