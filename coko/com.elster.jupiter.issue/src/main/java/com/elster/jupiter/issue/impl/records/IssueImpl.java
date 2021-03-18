/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueForAssign;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.entity.UnsupportedStatusChangeException;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static com.elster.jupiter.util.Checks.is;

//public class IssueImpl<T extends HasId & IdentifiedObject> extends EntityImpl implements Issue {
@NotManualIssueRuleIsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
public class IssueImpl extends EntityImpl implements Issue {
    private Instant dueDate;
    private Reference<IssueReason> reason = ValueReference.absent();
    private Reference<IssueStatus> status = ValueReference.absent();

    private Reference<IssueType> type = ValueReference.absent();
    private Priority priority;

    private boolean overdue;

    //work around
    private Reference<User> user = ValueReference.absent();
    private Reference<WorkGroup> workGroup = ValueReference.absent();

    private Reference<EndDevice> device = ValueReference.absent();
    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    //private Reference<T> member = ValueReference.absent(); // TODO - make IssueImpl abstract and implement DeviceIssueImpl and UsagePointIssueImpl and make getDevice and getUsagePoint Deprecated
    private Reference<CreationRule> rule = ValueReference.absent();

    private final IssueService issueService;
    private final IssueAssignmentService issueAssignmentService;
    final Clock clock;
    private Instant createDateTime;
    private Instant snoozeDateTime;
    final Thesaurus thesaurus;

    private static final String DEFAULT_ISSUE_PREFIX = "ISU";

    @Inject
    public IssueImpl(DataModel dataModel, IssueService issueService, Clock clock, Thesaurus thesaurus) {
        super(dataModel);
        this.issueService = issueService;
        this.clock = clock;
        this.issueAssignmentService = issueService.getIssueAssignmentService();
        this.thesaurus = thesaurus;
        setCreateDateTime(Instant.now(clock));
    }

    @Override
    public String getIssueId() {
        String prefix = DEFAULT_ISSUE_PREFIX;
        if (this.reason.isPresent()) {
            prefix = this.reason.get().getIssueType().getPrefix();
        }
        return prefix + "-" + this.getId();
    }

    @Override
    public String getTitle() {
        String title = getReason().getName();
        EndDevice endDevice = getDevice();
        Optional<UsagePoint> usagePoint = getUsagePoint();
        if (endDevice != null) {
            title = ((IssueReasonImpl) getReason()).getDescriptionFor(endDevice.getName());
        } else if (usagePoint != null && usagePoint.isPresent()) {
            title = ((IssueReasonImpl) getReason()).getDescriptionFor(usagePoint.get().getName());
        }
        return title;
    }

    @Override
    public IssueReason getReason() {
        return this.reason.orNull();
    }

    @Override
    public void setReason(IssueReason reason) {
        this.reason.set(reason);
    }

    @Override
    public IssueStatus getStatus() {
        return this.status.get();
    }

    @Override
    public void setStatus(IssueStatus status) {
        this.status.set(status);
    }

    @Override
    public Instant getDueDate() {
        return dueDate;
    }

    @Override
    public void setDueDate(Instant dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public EndDevice getDevice() {
        return this.device.orNull();
    }

    @Override
    public void setDevice(EndDevice device) {
        this.device.set(device);
    }

    @Override
    public Optional<CreationRule> getRule() {
        return rule.getOptional();
    }

    @Override
    public void setRule(CreationRule rule) {
        this.rule.set(rule);
    }

    @Override
    public boolean isOverdue() {
        return overdue;
    }

    @Override
    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    @Override
    public IssueAssigneeImpl getAssignee() {
        return new IssueAssigneeImpl(getUser(), getWorkGroup());
    }

    public User getUser() {
        return user.orNull();
    }

    public WorkGroup getWorkGroup() {
        return workGroup.orNull();
    }

    public void setUser(User user) {
        this.user.set(user);
    }

    public void setWorkGroup(WorkGroup workGroup) {
        this.workGroup.set(workGroup);
    }

    protected void resetAssignee() {
        setUser(null);
    }

    @Override
    public Optional<IssueComment> addComment(String body, User author) {
        if (!is(body).emptyOrOnlyWhiteSpace() && author != null) {
            IssueCommentImpl comment = getDataModel().getInstance(IssueCommentImpl.class);
            comment.init(getId(), body, author).save();
            return Optional.of(IssueComment.class.cast(comment));
        }
        return Optional.empty();
    }

    @Override
    public void removeComment(long id, User author) {
        if (author != null) {
            getDataModel().mapper(IssueCommentImpl.class).find("id", id).stream()
                    .filter(issueComment -> issueComment.getUser().getId() == author.getId())
                    .findFirst().ifPresent(
                    issueComment -> issueComment.delete()
            );
        }
    }

    @Override
    public Optional<IssueComment> editComment(long id, String body, User author) {
        if (!is(body).emptyOrOnlyWhiteSpace() && author != null) {
            return getDataModel().mapper(IssueCommentImpl.class).find("id", id).stream()
                    .findFirst()
                    .map(issueComment -> {
                        issueComment.setComment(body);
                        issueComment.update();
                        return Optional.of(IssueComment.class.cast(issueComment));
                    }).orElse(Optional.empty());
        }
        return Optional.empty();
    }

    @Override
    public void assignTo(Long userId, Long workGroupId) {
        assignTo(issueService.findIssueAssignee(userId, workGroupId));
    }

    @Override
    public void assignTo(IssueAssignee assignee) {
        if (assignee != null) {
            this.workGroup.set(assignee.getWorkGroup());
            this.user.set(assignee.getUser());
        }
    }

    @Override
    public void assignTo(String type, long id) {
        assignTo(id, null);
    }

    @Override
    public void autoAssign() {
        IssueForAssign wrapper = new IssueForAssignImpl(this, Instant.now(clock));
        issueAssignmentService.assignIssue(Collections.singletonList(wrapper));
    }


    @Override
    public Priority getPriority() {
        return priority == null || priority.isEmpty() ? null : priority.copy();
    }

    @Override
    public void setPriority(Priority priority) {
        this.priority = priority.copy();
    }

    @Override
    public Instant getCreateDateTime() {
        return createDateTime;
    }

    @Override
    public void setCreateDateTime(Instant dateTime) {
        createDateTime = dateTime;
    }

    @Override
    public Optional<Instant> getSnoozeDateTime() {
        return Optional.ofNullable(snoozeDateTime);
    }

    @Override
    public void snooze(Instant snoozeDateTime) {
        IssueStatus openIssueStatus = issueService.findStatus(IssueStatus.OPEN).orElse(null);
        IssueStatus snoozedIssueStaus = issueService.findStatus(IssueStatus.SNOOZED).orElse(null);
        IssueStatus ongoingIssueStatus = issueService.findStatus(IssueStatus.IN_PROGRESS).orElse(null);
        if (snoozeDateTime != null &&
                openIssueStatus != null &&
                snoozedIssueStaus != null &&
                (this.getStatus().equals(openIssueStatus) || this.getStatus().equals(ongoingIssueStatus) || this.getStatus().equals(snoozedIssueStaus))) {
            this.snoozeDateTime = snoozeDateTime;
            this.setStatus(snoozedIssueStaus);
        } else {
            throw new UnsupportedStatusChangeException(thesaurus, this.getStatus().getName());
        }

    }

    @Override
    public void clearSnooze() {
        this.setStatus(issueService.findStatus(IssueStatus.OPEN).orElse(null));
        this.snoozeDateTime = null;
    }

    @Override
    public Optional<UsagePoint> getUsagePoint() {
        if (usagePoint != null && usagePoint.isPresent()) {
            return Optional.of(usagePoint.get());
        } else {
            EndDevice endDevice = getDevice();
            if (endDevice != null && Meter.class.isInstance(endDevice)) {
                Meter meter = Meter.class.cast(endDevice);
                Optional<? extends MeterActivation> meterActivation = meter.getCurrentMeterActivation();
                if (meterActivation.isPresent()) {
                    return meterActivation.get().getUsagePoint();
                }
            }
            return Optional.empty();
        }

    }

    @Override
    public void setUsagePoint(UsagePoint usagePoint) {
        this.usagePoint.set(usagePoint);
    }

    @Override
    public IssueType getType() {
        return this.type.orNull();
    }

    @Override
    public void setType(IssueType type) {
        this.type.set(type);
    }

    public static Issue wrapOpenOrHistorical(Issue issue) {
        if (issue instanceof OpenIssue || issue instanceof HistoricalIssue) {
            return issue;
        } else if (issue instanceof IssueImpl) {
            IssueImpl issueImpl = (IssueImpl) issue;
            IssueImpl wrappedIssue = issue.getStatus().isHistorical() ?
                    new HistoricalIssueImpl(issueImpl.getDataModel(), issueImpl.getIssueService(), issueImpl.clock, issueImpl.thesaurus) :
                    new OpenIssueImpl(issueImpl.getDataModel(), issueImpl.getIssueService(), issueImpl.clock, issueImpl.thesaurus);
            wrappedIssue.copy(issue);
            return wrappedIssue;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    void copy(Issue issue) {
        this.setId(issue.getId());
        if (issue.getDueDate() != null) {
            this.setDueDate(issue.getDueDate());
        }
        this.setReason(issue.getReason());
        this.setStatus(issue.getStatus());
        this.setDevice(issue.getDevice());
        this.setUsagePoint(issue.getUsagePoint().orElse(null));
        this.setRule(issue.getRule().orElse(null));
        this.setCreateDateTime(issue.getCreateDateTime());
        this.setPriority(issue.getPriority());
        this.assignTo(issue.getAssignee());
        this.setType(issue.getType());
    }

    protected IssueService getIssueService() {
        return issueService;
    }

    @Override
    public String toString() {
        return "IssueImpl{" +
                "dueDate=" + dueDate +
                ", reason=" + reason +
                ", status=" + status +
                ", type=" + type +
                ", priority=" + priority +
                ", overdue=" + overdue +
                ", user=" + user +
                ", workGroup=" + workGroup +
                ", device=" + device +
                ", usagePoint=" + usagePoint +
                ", rule=" + rule +
                '}';
    }
}

