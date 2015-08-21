package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.*;
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

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static com.elster.jupiter.util.Checks.is;

public class IssueImpl extends EntityImpl implements Issue {
    private Instant dueDate;
    private Reference<IssueReason> reason = ValueReference.absent();
    private Reference<IssueStatus> status = ValueReference.absent();

    private IssueAssigneeImpl assignee;
    private boolean overdue;

    //work around
    private AssigneeType assigneeType;
    private Reference<User> user = ValueReference.absent();

    private Reference<EndDevice> device = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<CreationRule> rule = ValueReference.absent();

    private volatile IssueService issueService;
    private volatile IssueAssignmentService issueAssignmentService;

    @Inject
    public IssueImpl(DataModel dataModel, IssueService issueService){
        super(dataModel);
        this.issueService = issueService;
        this.issueAssignmentService = issueService.getIssueAssignmentService();
    }

    @Override
    public String getTitle() {
        String title = getReason().getName();
        EndDevice endDevice = getDevice();
        if (endDevice != null) {
            StringBuilder titleWithDevice = new StringBuilder(title);
            titleWithDevice.append(" ");
            titleWithDevice.append(endDevice.getMRID());
            title = titleWithDevice.toString();
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
        if (assignee == null && assigneeType != null) {
            assignee = assigneeType.getAssignee(this);
        }
        return assignee;
    }

    public User getUser() {
        return user.orNull();
    }

    public void setAssigneeType(AssigneeType assigneeType) {
        this.assigneeType = assigneeType;
    }

    public void setUser(User user) {
        this.user.set(user);
    }

    protected void resetAssignee(){
        assigneeType = null;
        setUser(null);
    }

    @Override
    public Optional<IssueComment> addComment(String body, User author){
        if (!is(body).emptyOrOnlyWhiteSpace() && author != null){
            IssueCommentImpl comment = getDataModel().getInstance(IssueCommentImpl.class);
            comment.init(getId(), body, author);
            comment.save();
            return Optional.of(IssueComment.class.cast(comment));
        }
        return Optional.empty();
    }

    @Override
    public void assignTo(String type, long id) {
        Optional<IssueAssignee> assignee = issueService.findIssueAssignee(AssigneeType.fromString(type), id);
        if (assignee.isPresent()) {
            assignTo(assignee.get());
        }
    }

    @Override
    public void assignTo(IssueAssignee assignee) {
        if (assignee != null) {
            resetAssignee();
            IssueAssigneeImpl assigneeImpl = IssueAssigneeImpl.class.cast(assignee);
            assigneeImpl.applyAssigneeToIssue(this);
        }
    }

    @Override
    public void autoAssign(){
        IssueForAssign wrapper = new IssueForAssignImpl(this);
        issueAssignmentService.assignIssue(Collections.singletonList(wrapper));
    }

    @Override
    public Optional<UsagePoint> getUsagePoint(){
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
