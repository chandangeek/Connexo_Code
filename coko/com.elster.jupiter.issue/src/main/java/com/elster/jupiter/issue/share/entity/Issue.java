package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.issue.impl.drools.IssueForAssign;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;

import javax.inject.Inject;
import java.util.Collections;

import static com.elster.jupiter.util.Checks.is;
public class Issue extends BaseIssue {
    private volatile UserService userService;
    private volatile IssueService issueService;
    private volatile IssueAssignmentService issueAssignmentService;

    @Inject
    public Issue(DataModel dataModel, UserService userService, IssueService issueService, IssueAssignmentService issueAssignmentService){
        super(dataModel);
        this.userService = userService;
        this.issueService = issueService;
        this.issueAssignmentService = issueAssignmentService;
    }

    Issue copy(Issue issue){
        this.setId(issue.getId());
        if (issue.getDueDate() != null) {
            this.setDueDate(new UtcInstant(issue.getDueDate().getTime()));
        }
        this.setReason(issue.getReason());
        this.setStatus(issue.getStatus());
        this.setAssignee(issue.getAssignee());
        this.setDevice(issue.getDevice());
        this.setDevice(issue.getDevice());
        return this;
    }

    public void close(IssueStatus status){
        if (status == null || !status.isFinal()) {
            throw  new IllegalArgumentException("Incorrect status for closing issue");
        }
        setStatus(status);
        HistoricalIssue historicalIssue = new HistoricalIssue(getDataModel(), userService, issueService, issueAssignmentService);
        historicalIssue.copy(this);
        historicalIssue.save();
        this.delete();
    }

    public Optional<IssueComment> addComment(String body, User author){
        if (!is(body).emptyOrOnlyWhiteSpace() && author != null){
            IssueComment comment = getDataModel().getInstance(IssueComment.class);
            comment.init(getId(), body, author);
            comment.save();
            return Optional.of(comment);
        }
        return Optional.absent();
    }

    public void assignTo(IssueAssigneeType type, long id){
        if (type != null) {
            switch (type) {
                case USER:
                    assignTo(userService.getUser(id).orNull());
                    break;
                case ROLE:
                    assignTo(issueService.findAssigneeRole(id).orNull());
                    break;
                case TEAM:
                    assignTo(issueService.findAssigneeTeam(id).orNull());
                    break;
            }
        }
    }

    public void assignTo(User user){
        setAssignee(IssueAssignee.fromUser(user));
    }

    public void assignTo(AssigneeRole role){
        setAssignee(IssueAssignee.fromRole(role));
    }

    public void assignTo(AssigneeTeam team){
        setAssignee(IssueAssignee.fromTeam(team));
    }

    public void autoAssign(){
        issueAssignmentService.assignIssue(Collections.singletonList(new IssueForAssign(this)));
    }

    public Optional<UsagePoint> getUsagePoint(){
        EndDevice endDevice = getDevice();
        if (endDevice != null && Meter.class.isInstance(endDevice)) {
            Meter meter = Meter.class.cast(endDevice);
            Optional<MeterActivation> meterActivation = meter.getCurrentMeterActivation();
            if (meterActivation.isPresent()) {
                return meterActivation.get().getUsagePoint();
            }
        }
        return Optional.absent();
    }
}
