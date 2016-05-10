package com.energyict.mdc.issue.datacollection.impl.records;

import com.elster.jupiter.domain.util.Save;
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
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import static com.elster.jupiter.util.Checks.is;

public class IssueDataCollectionImpl implements IssueDataCollection {

    private final DataModel dataModel;
    private Reference<Issue> baseIssue = ValueReference.absent();
    private Reference<ComTaskExecution> comTask = ValueReference.absent();
    private Reference<ConnectionTask> connectionTask = ValueReference.absent();
    private Reference<ComSession> comSession = ValueReference.absent();
    private String deviceMRID;
    private Instant firstConnectionAttemptTimestamp;
    private Instant lastConnectionAttemptTimestamp;
    private long connectionAttempt;
    private long id;//do we need this id ? we have a reference to base issue instead...
    // Audit fields
    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

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

    @Override
    public long getVersion() {
        return getBaseIssue().getVersion();
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
    public String getUserName() {
        return userName;
    }

    @Override
    public void update() {
        getBaseIssue().update();
        Save.UPDATE.save(dataModel, this);
    }

    public void delete(){
        dataModel.remove(this);
    }

    protected Issue getBaseIssue() {
        return baseIssue.orNull();
    }

    @Override
    public String getIssueId() {
        return this.getBaseIssue().getIssueId();
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
    public void setReason(IssueReason reason) {
        getBaseIssue().setReason(reason);
    }

    @Override
    public IssueStatus getStatus() {
        return getBaseIssue().getStatus();
    }

    @Override
    public void setStatus(IssueStatus status) {
        getBaseIssue().setStatus(status);
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
    public void setDevice(EndDevice device) {
        getBaseIssue().setDevice(device);
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
    public void setDueDate(Instant dueDate) {
        getBaseIssue().setDueDate(dueDate);
    }

    @Override
    public boolean isOverdue() {
        return getBaseIssue().isOverdue();
    }

    @Override
    public void setOverdue(boolean overdue) {
        getBaseIssue().setOverdue(overdue);
    }

    @Override
    public CreationRule getRule() {
        return getBaseIssue().getRule();
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
    public Instant getFirstConnectionAttemptTimestamp() {
        return firstConnectionAttemptTimestamp;
    }

    @Override
    public void setFirstConnectionAttemptTimestamp(Instant firstConnectionAttemptTimestamp) {
        this.firstConnectionAttemptTimestamp = firstConnectionAttemptTimestamp;
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

    protected DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
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
}
