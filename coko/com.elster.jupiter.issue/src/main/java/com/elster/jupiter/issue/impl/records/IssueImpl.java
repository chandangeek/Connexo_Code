package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;

import javax.inject.Inject;
import java.util.Collections;

import static com.elster.jupiter.util.Checks.is;

public class IssueImpl extends BaseIssueImpl implements Issue{


    private volatile UserService userService;
    private volatile IssueService issueService;
    private volatile IssueAssignmentService issueAssignmentService;

    @Inject
    public IssueImpl(DataModel dataModel, UserService userService, IssueService issueService, IssueAssignmentService issueAssignmentService){
        super(dataModel);
        this.userService = userService;
        this.issueService = issueService;
        this.issueAssignmentService = issueAssignmentService;
    }

    @Override
    public void close(IssueStatus status){
        if (status == null || !status.isFinal()) {
            throw  new IllegalArgumentException("Incorrect status for closing issue");
        }
        setStatus(status);
        HistoricalIssue historicalIssue = getDataModel().getInstance(HistoricalIssueImpl.class);
        BaseIssueImpl.class.cast(historicalIssue).copy(this);
        historicalIssue.save();
        this.delete();
    }

    @Override
    public Optional<IssueComment> addComment(String body, User author){
        if (!is(body).emptyOrOnlyWhiteSpace() && author != null){
            IssueCommentImpl comment = getDataModel().getInstance(IssueCommentImpl.class);
            comment.init(getId(), body, author);
            comment.save();
            return Optional.of(IssueComment.class.cast(comment));
        }
        return Optional.absent();
    }

    @Override
    public void assignTo(String type, long id) {
        IssueAssignee assignee = issueService.findIssueAssignee(type, id);
        assignTo(assignee);
    }

    @Override
    public void assignTo(IssueAssignee assignee) {
        if (assignee != null) {
            IssueAssigneeImpl assgneeImpl = IssueAssigneeImpl.class.cast(assignee);
            assignTo(assgneeImpl);
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
            Optional<MeterActivation> meterActivation = meter.getCurrentMeterActivation();
            if (meterActivation.isPresent()) {
                return meterActivation.get().getUsagePoint();
            }
        }
        return Optional.absent();
    }
}
