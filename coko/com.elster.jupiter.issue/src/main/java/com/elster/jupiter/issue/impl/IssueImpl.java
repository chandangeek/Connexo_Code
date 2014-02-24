package com.elster.jupiter.issue.impl;

import com.elster.jupiter.issue.Issue;
import com.elster.jupiter.issue.IssueAssignee;
import com.elster.jupiter.issue.IssueReason;
import com.elster.jupiter.issue.IssueStatus;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.util.Map;

public class IssueImpl implements Issue {
    // TODO apply general class of issue
    public final Map<String, Class<? extends IssueImpl>> IMPLEMENTERS = ImmutableMap.<String, Class<? extends IssueImpl>>of(Issue.TYPE_IDENTIFIER, IssueImpl.class);

    private final DataModel dataModel;

    protected long id;
    protected UtcInstant dueDate;
    protected Reference<IssueReason> reason = ValueReference.absent();
    protected Reference<IssueStatus> status = ValueReference.absent();

    protected IssueAssigneeImpl assignee;
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
    public String getTitle() {
        String title = getReason().getName();
        if (getDevice() != null){
            StringBuilder titleWithDevice = new StringBuilder(title);
            titleWithDevice.append(" to ");
            titleWithDevice.append(getDevice().getSerialNumber());
            title = titleWithDevice.toString();
        }
        return title;
    }

    @Override
    public IssueReason getReason() {
        return this.reason.orNull();
    }

    public void setReason(IssueReason reason) {
        this.reason.set(reason);
    }

    public IssueStatus getStatus() {
        return this.status.get();
    }

    public void setStatus(IssueStatus status) {
        this.status.set(status);
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
        return assignee;
    }
}
