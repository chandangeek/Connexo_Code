package com.energyict.mdc.issue.datacollection.impl.records;

import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;

import static com.elster.jupiter.util.Checks.is;

public class IssueDataCollectionImpl extends EntityImpl implements IssueDataCollection {

    private Reference<Issue> baseIssue = ValueReference.absent();
    private Reference<ComTaskExecution> comTask = ValueReference.absent();
    private Reference<ConnectionTask> connectionTask = ValueReference.absent();
    private Reference<ComSession> comSession = ValueReference.absent();
    private String deviceMRID;

    @Inject
    public IssueDataCollectionImpl(DataModel dataModel) {
        super(dataModel);
    }

    protected Issue getBaseIssue() {
        return baseIssue.orNull();
    }

    @Override
    public String getTitle() {
        return getBaseIssue().getTitle();
    }

    @Override
    public IssueReason getReason() {
        return getBaseIssue().getReason();
    }

    @Override
    public IssueStatus getStatus() {
        return getBaseIssue().getStatus();
    }

    @Override
    public IssueAssignee getAssignee() {
        return getBaseIssue().getAssignee();
    }

    @Override
    public EndDevice getDevice() {
        return getBaseIssue().getDevice();
    }

    @Override
    public Optional<UsagePoint> getUsagePoint() {
        return getBaseIssue().getUsagePoint();
    }

    @Override
    public Instant getDueDate() {
        return getBaseIssue().getDueDate();
    }

    @Override
    public boolean isOverdue() {
        return getBaseIssue().isOverdue();
    }

    @Override
    public CreationRule getRule() {
        return getBaseIssue().getRule();
    }

    @Override
    public void setReason(IssueReason reason) {
        getBaseIssue().setReason(reason);
    }

    @Override
    public void setStatus(IssueStatus status) {
        getBaseIssue().setStatus(status);
    }

    @Override
    public void setDevice(EndDevice device) {
        getBaseIssue().setDevice(device);
    }

    @Override
    public void setDueDate(Instant dueDate) {
        getBaseIssue().setDueDate(dueDate);
    }

    @Override
    public void setOverdue(boolean overdue) {
        getBaseIssue().setOverdue(overdue);
    }

    @Override
    public void setRule(CreationRule rule) {
        getBaseIssue().setRule(rule);
    }

    @Override
    public Optional<IssueComment> addComment(String body, User author) {
        return getBaseIssue().addComment(body, author);
    }

    @Override
    public void assignTo(String type, long id) {
        getBaseIssue().assignTo(type, id);
    }

    @Override
    public void assignTo(IssueAssignee assignee) {
        getBaseIssue().assignTo(assignee);
    }

    @Override
    public void autoAssign() {
        getBaseIssue().autoAssign();
    }

    @Override
    public Optional<ConnectionTask> getConnectionTask() {
        return connectionTask.getOptional();
    }

    @Override
    public void setConnectionTask(ConnectionTask task) {
        connectionTask.set(task);
    }

    @Override
    public Optional<ComTaskExecution> getCommunicationTask() {
        return comTask.getOptional();
    }

    @Override
    public void setCommunicationTask(ComTaskExecution task) {
        comTask.set(task);
    }

    @Override
    public String getDeviceMRID() {
        if (!is(deviceMRID).emptyOrOnlyWhiteSpace()) {
            return deviceMRID;
        } else if (getBaseIssue() != null && getBaseIssue().getDevice() != null) {
            return getBaseIssue().getDevice().getMRID();
        }
        return "";
    }

    @Override
    public void setDeviceMRID(String deviceMRID) {
        this.deviceMRID = deviceMRID;
    }
    
    @Override
    public Optional<ComSession> getComSession() {
        return comSession.getOptional();
    }
    
    @Override
    public void setComSession(ComSession comSession) {
        this.comSession.set(comSession);
    }

    @Override
    public void save() {
        if (getBaseIssue() != null) {
            getBaseIssue().save();
            this.setId(getBaseIssue().getId());
        }
        super.save();
    }
}
