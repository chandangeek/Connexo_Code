/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.records;

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
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.event.DeviceAlarmRelatedEvent;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DeviceAlarmImpl implements DeviceAlarm {


    public enum Fields {
        BASE_ISSUE("baseIssue"),
        DEVICE_ALARM_RELATED_EVENTS("deviceAlarmRelatedEvents"),;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private Reference<Issue> baseIssue = ValueReference.absent();
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
    private final DeviceAlarmService deviceAlarmService;

    @Inject
    public DeviceAlarmImpl(DataModel dataModel, DeviceAlarmService deviceAlarmService) {
        this.dataModel = dataModel;
        this.deviceAlarmService = deviceAlarmService;
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
    public Boolean isStatusCleared() {
        return clearedStatus;
    }

    @Override
    public void setClearedStatus() {
        clearedStatus = !clearedStatus;
    }


    @Override
    public Optional<IssueComment> addComment(String body, User author) {
        return getBaseIssue().addComment(body, author);
    }

    @Override
    public void assignTo(String type, long userId) {
        assignTo(userId, null);
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
    public Instant getCreateDateTime() {
        return getBaseIssue().getCreateDateTime();
    }

    @Override
    public void setCreateDateTime(Instant dateTime) {
        getBaseIssue().setCreateDateTime(dateTime);
    }

    @Override
    public List<DeviceAlarmRelatedEvent> getDeviceAlarmRelatedEvents() {
        Optional<? extends DeviceAlarm> alarm;
        if (getStatus().isHistorical()) {
            alarm = deviceAlarmService.findHistoricalAlarm(getId());
        } else {
            alarm = deviceAlarmService.findAlarm(getId());
        }
        return alarm.map(DeviceAlarm::getDeviceAlarmRelatedEvents).orElse(Collections.emptyList());
    }

    public void save() {
        if (getBaseIssue() != null) {
            getBaseIssue().update();
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof DeviceAlarmImpl)) {
            return false;
        }

        DeviceAlarmImpl that = (DeviceAlarmImpl) o;

        return Objects.equals(this.getBaseIssue(), that.getBaseIssue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBaseIssue());
    }
}
