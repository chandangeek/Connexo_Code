package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.UtcInstant;

import javax.inject.Inject;

public class BaseIssue extends Entity{
    protected UtcInstant dueDate;
    protected Reference<IssueReason> reason = ValueReference.absent();
    protected Reference<IssueStatus> status = ValueReference.absent();

    protected IssueAssignee assignee;
    protected String issueType = "C";

    //work around
    private IssueAssigneeType type;
    private Reference<User> user = ValueReference.absent();
    private Reference<AssigneeTeam> team = ValueReference.absent();
    private Reference<AssigneeRole> role = ValueReference.absent();

    protected Reference<EndDevice> device = ValueReference.absent();

    @Inject
    public BaseIssue(DataModel dataModel) {
        super(dataModel);
    }

    public String getTitle() {
        String title = getReason().getName();
        EndDevice device = getDevice();
        if (device != null){
            StringBuilder titleWithDevice = new StringBuilder(title);
            titleWithDevice.append(" to ");
            titleWithDevice.append(device.getName()).append(" ");
            titleWithDevice.append(device.getSerialNumber());
            title = titleWithDevice.toString();
        }
        return title;
    }

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


    public UtcInstant getDueDate() {
        return dueDate;
    }

    public void setDueDate(UtcInstant dueDate) {
        this.dueDate = dueDate;
    }


    public EndDevice getDevice() {
        return this.device.orNull();
    }

    public void setDevice(EndDevice device){
        this.device.set(device);
    }

    public IssueAssignee getAssignee() {
        if (assignee == null && type != null){
            assignee = new IssueAssignee();
            assignee.setType(type);
            switch (type){
                case USER:
                    assignee.setUser(user.orNull());
                    break;
                case TEAM:
                    assignee.setTeam(team.orNull());
                    break;
                case ROLE:
                    assignee.setRole(role.orNull());
                    break;
            }
        }
        return assignee;
    }

    public void setAssignee(IssueAssignee assignee){
        this.assignee = null;
        if (assignee != null){
            type = assignee.getType();
            user.set(assignee.getUser());
            role.set(assignee.getRole());
            team.set(assignee.getTeam());
        }
    }

    public String getIssueType() {
        return issueType;
    }
}
