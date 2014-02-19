package com.elster.jupiter.issue.impl;

import com.elster.jupiter.issue.IssueAssignee;
import com.elster.jupiter.issue.IssueAssigneeType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.UtcInstant;

import javax.inject.Inject;

public class IssueAssigneeImpl implements IssueAssignee {
    private final DataModel dataModel;

    private long id;
    private String assigneeRef;
    private IssueAssigneeType assigneeType;

    // Audit fields
    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String userName;

    @Inject
    IssueAssigneeImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    IssueAssigneeImpl init(String assigneeRef, IssueAssigneeType assigneeType) {
        this.assigneeRef = assigneeRef;
        this.assigneeType = assigneeType;
        return this;
    }

    static IssueAssigneeImpl from(DataModel dataModel, String assigneeRef, IssueAssigneeType assigneeType) {

        return dataModel.getInstance(IssueAssigneeImpl.class).init(assigneeRef, assigneeType);
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getAssigneeRef() {
        return assigneeRef;
    }

    public void setAssigneeRef(String assigneeRef) {
        this.assigneeRef = assigneeRef;
    }

    @Override
    public IssueAssigneeType getAssigneeType() {
        return assigneeType;
    }

    public void setAssigneeType(IssueAssigneeType assigneeType) {
        this.assigneeType = assigneeType;
    }

    @Override
    public long getVersion() {
        return version;
    }
}
