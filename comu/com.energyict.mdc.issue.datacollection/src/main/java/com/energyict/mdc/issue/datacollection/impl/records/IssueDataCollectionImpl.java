/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.records;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.User;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import static com.elster.jupiter.util.Checks.is;

public class IssueDataCollectionImpl implements IssueDataCollection {

    private Reference<Issue> baseIssue = ValueReference.absent();
    private Reference<ComTaskExecution> comTask = ValueReference.absent();
    private Reference<ConnectionTask> connectionTask = ValueReference.absent();
    private Reference<ComSession> comSession = ValueReference.absent();
    private String deviceMRID;
    private String lastGatewayMRID;
    private Instant firstConnectionAttemptTimestamp;
    private Instant lastConnectionAttemptTimestamp;
    private long connectionAttempt;

    private long id;//do we need this id ? we have a reference to base issue instead...
    // Audit fields
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    private final DataModel dataModel;

    @Inject
    public IssueDataCollectionImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    protected Issue getBaseIssue() {
        return baseIssue.orNull();
    }

    @Override
    public String getIssueId() {
        return getBaseIssue().getIssueId();
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
    public Optional<CreationRule> getRule() {
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
    public void setUsagePoint(UsagePoint usagePoint){
        getBaseIssue().setUsagePoint(usagePoint);
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
    public void removeComment(long id, User author) {
        getBaseIssue().removeComment(id, author);
    }

    @Override
    public Optional<IssueComment> editComment(long id, String body, User author) {
        return getBaseIssue().editComment(id, body, author);
    }

    @Override
    public void assignTo(Long userId, Long workGroupId) {
        getBaseIssue().assignTo(userId, workGroupId);
    }

    @Override
    public void assignTo(IssueAssignee assignee) {
        getBaseIssue().assignTo(assignee);
    }

    @Override
    public void assignTo(String type, long id) {
        getBaseIssue().assignTo(type, id);
    }

    @Override
    public void autoAssign() {
        getBaseIssue().autoAssign();
    }

    @Override
    public Priority getPriority() {
        return getBaseIssue().getPriority();
    }

    @Override
    public void setPriority(Priority priority) {
        getBaseIssue().setPriority(priority);
    }

    @Override
    public Optional<Instant> getSnoozeDateTime() {
        return getBaseIssue().getSnoozeDateTime();
    }

    @Override
    public void snooze(Instant snoozeDateTime) {
        getBaseIssue().snooze(snoozeDateTime);

    }

    @Override
    public void clearSnooze() {
        getBaseIssue().clearSnooze();
    }

    @Override
    public Instant getCreateDateTime() {
        return getBaseIssue().getCreateDateTime();
    }

    @Override
    public void setCreateDateTime(Instant dateTime) {
        getBaseIssue().setCreateDateTime(dateTime);
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
    public String getDeviceIdentification() {
        if (!is(deviceMRID).emptyOrOnlyWhiteSpace()) {
            return deviceMRID;
        } else if (getBaseIssue() != null && getBaseIssue().getDevice() != null) {
            return getBaseIssue().getDevice().getMRID();
        }
        return "";
    }

    @Override
    public void setDeviceIdentification(String deviceIdentification) {
        this.deviceMRID = deviceIdentification;
    }

    @Override
    public String getLastGatewayIdentification() {
        return lastGatewayMRID;
    }

    @Override
    public void setLastGatewayIdentification(String gatewayIdentification) {
        this.lastGatewayMRID = gatewayIdentification;
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
    public Instant getLastConnectionAttemptTimestamp() {
        return lastConnectionAttemptTimestamp;
    }

    @Override
    public void setLastConnectionAttemptTimestamp(Instant lastConnectionAttemptTimestamp) {
        this.lastConnectionAttemptTimestamp = lastConnectionAttemptTimestamp;
    }

    @Override
    public Instant getFirstConnectionAttemptTimestamp() {
        return firstConnectionAttemptTimestamp;
    }

    @Override
    public void setFirstConnectionAttemptTimestamp(Instant firstConnectionAttemptTimestamp) {
        this.firstConnectionAttemptTimestamp = firstConnectionAttemptTimestamp;
    }

    @Override
    public long getConnectionAttempt() {
        return connectionAttempt;
    }

    @Override
    public void setConnectionAttempt(long connectionAttempt) {
        this.connectionAttempt = connectionAttempt;
    }

    @Override
    public void incrementConnectionAttempt() {
        this.connectionAttempt = this.connectionAttempt + 1;
    }


    public void save() {
        if (getBaseIssue() != null) {
            this.setId(getBaseIssue().getId());
        }
        Save.CREATE.save(dataModel, this);
    }

    @Override
    public void update() {
        getBaseIssue().update();
        Save.UPDATE.save(dataModel, this);
    }

    public void delete() {
        dataModel.remove(this);
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public long getVersion() {
        return getBaseIssue().getVersion();
    }

    @Override
    public String getUserName() {
        return userName;
    }

    protected DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof IssueDataCollectionImpl)) {
            return false;
        }

        IssueDataCollectionImpl that = (IssueDataCollectionImpl) o;

        return this.id == that.id;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "IssueDataCollectionImpl{" +
                "baseIssue=" + baseIssue +
                ", comTask=" + comTask +
                ", connectionTask=" + connectionTask +
                ", comSession=" + comSession +
                ", deviceMRID='" + deviceMRID + '\'' +
                ", lastGatewayMRID='" + lastGatewayMRID + '\'' +
                ", firstConnectionAttemptTimestamp=" + firstConnectionAttemptTimestamp +
                ", lastConnectionAttemptTimestamp=" + lastConnectionAttemptTimestamp +
                ", connectionAttempt=" + connectionAttempt +
                ", id=" + id +
                ", version=" + version +
                ", createTime=" + createTime +
                ", modTime=" + modTime +
                ", userName='" + userName + '\'' +
                ", dataModel=" + dataModel +
                '}';
    }
}
