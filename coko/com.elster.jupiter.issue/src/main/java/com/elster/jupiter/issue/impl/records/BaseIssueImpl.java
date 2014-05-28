package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.impl.records.assignee.AssigneeRoleImpl;
import com.elster.jupiter.issue.impl.records.assignee.AssigneeTeamImpl;
import com.elster.jupiter.issue.impl.records.assignee.types.AssigneeTypes;
import com.elster.jupiter.issue.share.entity.BaseIssue;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.UtcInstant;

import javax.inject.Inject;

public class BaseIssueImpl extends EntityImpl implements BaseIssue {
    private UtcInstant dueDate;
    private Reference<IssueReason> reason = ValueReference.absent();
    private Reference<IssueStatus> status = ValueReference.absent();

    private IssueAssigneeImpl assignee;
    private boolean overdue;

    //work around
    private AssigneeTypes assigneeType;
    private Reference<User> user = ValueReference.absent();
    private Reference<AssigneeTeamImpl> group = ValueReference.absent();
    private Reference<AssigneeRoleImpl> role = ValueReference.absent();

    private Reference<EndDevice> device = ValueReference.absent();
    private Reference<CreationRule> rule = ValueReference.absent();

    @Inject
    public BaseIssueImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    public String getTitle() {
        String title = getReason().getName();
        EndDevice endDevice = getDevice();
        if (endDevice != null) {
            StringBuilder titleWithDevice = new StringBuilder(title);
            titleWithDevice.append(" to ");
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
    public UtcInstant getDueDate() {
        return dueDate;
    }

    @Override
    public void setDueDate(UtcInstant dueDate) {
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

    BaseIssueImpl copy(BaseIssueImpl issue) {
        this.setId(issue.getId());
        if (issue.getDueDate() != null) {
            this.setDueDate(new UtcInstant(issue.getDueDate().getTime()));
        }
        this.setReason(issue.getReason());
        this.setStatus(issue.getStatus());
        this.setDevice(issue.getDevice());
        this.setDevice(issue.getDevice());
        this.setRule(issue.getRule());
        this.assignTo(issue.getAssignee());
        return this;
    }

    public User getUser() {
        return user.orNull();
    }

    public AssigneeTeamImpl getGroup(){
        return group.orNull();
    }

    public AssigneeRoleImpl getRole(){
        return role.orNull();
    }

    public void setAssigneeType(AssigneeTypes assigneeType) {
        this.assigneeType = assigneeType;
    }

    public void setUser(User user) {
        this.user.set(user);
    }

    public void setGroup(AssigneeTeamImpl team) {
        this.group.set(team);
    }

    public void setRole(AssigneeRoleImpl role) {
        this.role.set(role);
    }

    protected void resetAssignee(){
        assigneeType = null;
        setUser(null);
        setGroup(null);
        setRole(null);
    }

    protected void assignTo(IssueAssigneeImpl assigneeImpl){
        resetAssignee();
        if (assigneeImpl != null) {
            assigneeImpl.applyAssigneeToIssue(this);
        }
    }
}
