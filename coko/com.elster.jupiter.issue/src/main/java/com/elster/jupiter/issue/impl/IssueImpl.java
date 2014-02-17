package com.elster.jupiter.issue.impl;

import com.elster.jupiter.issue.Issue;
import com.elster.jupiter.issue.IssueAssignee;
import com.elster.jupiter.issue.IssueStatus;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.fields.impl.ReverseConstraintMapping;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.inject.Inject;

public class IssueImpl implements Issue {
    private final DataModel dataModel;

    protected long id;
    protected String reason;
    protected UtcInstant dueDate;
    protected IssueStatus status;

    protected Reference<IssueAssignee> assignee = ValueReference.absent();
    protected Reference<EndDevice> device = ValueReference.absent();

    // Audit fields
    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String userName;

    @Inject
    IssueImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    IssueImpl init(String status) {
        this.status = IssueStatus.valueOf(status);
        return this;
    }

    static IssueImpl from(DataModel dataModel, String status) {
        return dataModel.getInstance(IssueImpl.class).init(status);
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public UtcInstant getCreateTime() {
        return createTime;
    }

    public void setCreateTime(UtcInstant createTime) {
        this.createTime = createTime;
    }

    public UtcInstant getModTime() {
        return modTime;
    }

    public void setModTime(UtcInstant modTime) {
        this.modTime = modTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    @Override
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public UtcInstant getDueDate() {
        return dueDate;
    }

    public void setDueDate(UtcInstant dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public EndDevice getDevice() {
        return this.device.orNull();
    }

    public void setDevice(EndDevice device){
        this.device.set(device);
    }

    public IssueAssignee getAssignee() {
        return assignee.orNull();
    }

    public void setAssignee(IssueAssignee assignee) {
        this.assignee.set(assignee);
    }
}
