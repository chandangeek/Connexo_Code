package com.energyict.mdc.device.alarms.impl.records;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.elster.jupiter.util.Checks.is;

public class DeviceAlarmImpl implements DeviceAlarm {

    private Reference<Issue> baseAlarm = ValueReference.absent();
    private String deviceMRID;
    private List<EndDeviceEventRecord> relatedEvents;
    private Boolean clearedStatus = Boolean.FALSE;


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
    public DeviceAlarmImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    protected Issue getBaseAlarm() {
        return baseAlarm.orNull();
    }

    @Override
    public String getIssueId() {
        return getBaseAlarm().getIssueId();
    }

    @Override
    public String getTitle() {
        return getBaseAlarm().getTitle();
    }

    @Override
    public IssueReason getReason() {
        return getBaseAlarm().getReason();
    }

    @Override
    public IssueStatus getStatus() {
        return getBaseAlarm().getStatus();
    }

    @Override
    public IssueAssignee getAssignee() {
        return getBaseAlarm().getAssignee();
    }

    @Override
    public EndDevice getDevice() {
        return getBaseAlarm().getDevice();
    }

    @Override
    public Optional<UsagePoint> getUsagePoint() {
        return getBaseAlarm().getUsagePoint();
    }

    @Override
    public Instant getDueDate() {
        return getBaseAlarm().getDueDate();
    }

    @Override
    public boolean isOverdue() {
        return getBaseAlarm().isOverdue();
    }

    @Override
    public CreationRule getRule() {
        return getBaseAlarm().getRule();
    }

    @Override
    public void setReason(IssueReason reason) {
        getBaseAlarm().setReason(reason);
    }

    @Override
    public void setStatus(IssueStatus status) {
        getBaseAlarm().setStatus(status);
    }

    @Override
    public void setDevice(EndDevice device) {
        getBaseAlarm().setDevice(device);
    }

    @Override
    public void setDueDate(Instant dueDate) {
        getBaseAlarm().setDueDate(dueDate);
    }

    @Override
    public void setOverdue(boolean overdue) {
        getBaseAlarm().setOverdue(overdue);
    }

    @Override
    public void setRule(CreationRule rule) {
        getBaseAlarm().setRule(rule);
    }

    @Override
    public List<EndDeviceEventRecord> getRelatedEventRecords() {
        return relatedEvents;
    }

    @Override
    public EndDeviceEventRecord getCurrentEventRecord() {
        return Collections.max(relatedEvents, Comparator.comparing(EndDeviceEventRecord::getCreateTime));
    }

    @Override
    public Boolean getClearedStatus() {
        return clearedStatus;
    }

    @Override
    public void setClearedStatus() {
        clearedStatus = Boolean.TRUE;
    }


    @Override
    public Optional<IssueComment> addComment(String body, User author) {
        return getBaseAlarm().addComment(body, author);
    }

    @Override
    public void assignTo(Long userId, Long workGroupId) {
        getBaseAlarm().assignTo(userId, workGroupId);
    }

    @Override
    public void assignTo(IssueAssignee assignee) {
        getBaseAlarm().assignTo(assignee);
    }

    @Override
    public void autoAssign() {
        getBaseAlarm().autoAssign();
    }

    @Override
    public String getDeviceMRID() {
        if (!is(deviceMRID).emptyOrOnlyWhiteSpace()) {
            return deviceMRID;
        } else if (getBaseAlarm() != null && getBaseAlarm().getDevice() != null) {
            return getBaseAlarm().getDevice().getMRID();
        }
        return "";
    }

    @Override
    public void setDeviceMRID(String deviceMRID) {
        this.deviceMRID = deviceMRID;
    }


    public void save() {
        if (getBaseAlarm() != null) {
            this.setId(getBaseAlarm().getId());
        }
        Save.CREATE.save(dataModel, this);
    }

    @Override
    public void update() {
        getBaseAlarm().update();
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
        return getBaseAlarm().getVersion();
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
        if (o == null || !(o instanceof DeviceAlarmImpl)) {
            return false;
        }

        DeviceAlarmImpl that = (DeviceAlarmImpl) o;

        return this.id == that.id;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}
