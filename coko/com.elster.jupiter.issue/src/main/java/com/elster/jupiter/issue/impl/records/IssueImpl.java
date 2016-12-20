package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueForAssign;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.Priority;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
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

public class IssueImpl extends EntityImpl implements Issue {
    private Instant dueDate;
    private Reference<IssueReason> reason = ValueReference.absent();
    private Reference<IssueStatus> status = ValueReference.absent();
    private long urgency;
    private long impact;

    private boolean overdue;

    //work around
    private Reference<User> user = ValueReference.absent();
    private Reference<WorkGroup> workGroup = ValueReference.absent();

    private Reference<EndDevice> device = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<CreationRule> rule = ValueReference.absent();

    private final IssueService issueService;
    private final IssueAssignmentService issueAssignmentService;
    private final Clock clock;

    private static final String DEFAULT_ISSUE_PREFIX = "ISU";

    @Inject
    public IssueImpl(DataModel dataModel, IssueService issueService, Clock clock) {
        super(dataModel);
        this.issueService = issueService;
        this.clock = clock;
        this.issueAssignmentService = issueService.getIssueAssignmentService();
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
        if (endDevice != null) {
            title = ((IssueReasonImpl) getReason()).getDescriptionFor(endDevice.getName());
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
    public CreationRule getRule() {
        return rule.orNull();
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
        return Priority.get(urgency, impact);
    }

    @Override
    public void setPriority(long urgency, long impact) {
        this.urgency = urgency;
        this.impact = impact;
    }

    @Override
    public Optional<UsagePoint> getUsagePoint() {
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

    protected IssueService getIssueService() {
        return issueService;
    }
}

