package com.elster.jupiter.issue.impl.drools;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssigneeType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.parties.Party;
import com.google.common.base.Optional;

import java.util.Date;

public class IssueForAssign {
    public static final byte PROCESSED = 1;

    private long id;
    private long version;

    // Fields for check
    private byte status = 0;
    private String outageRegion;
    private String customer;
    private String reason;

    // Fields which will be modified by drools engine
    private IssueAssigneeType assigneeType;
    private long assigneeId;

    private Issue issue;

    public IssueForAssign(Issue issue) {
        if (issue == null) {
            throw new IllegalArgumentException("Issue for wrapping can't be null!");
        }
        this.issue = issue;
        setId(issue.getId());
        setVersion(issue.getVersion());
        setReason(issue.getReason().getName());
        Optional<UsagePoint> usagePointRef = issue.getUsagePoint();
        if (usagePointRef.isPresent()){
            UsagePoint usagePoint = usagePointRef.get();
            setOutageRegion(usagePoint.getOutageRegion());
            Optional<Party> customerRef = usagePoint.getCustomer(new Date());
            if (customerRef.isPresent()){
                setCustomer(customerRef.get().getName());
            }
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public String getOutageRegion() {
        return outageRegion;
    }

    public void setOutageRegion(String outageRegion) {
        this.outageRegion = outageRegion;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public IssueAssigneeType getAssigneeType() {
        return assigneeType;
    }

    public void setAssigneeType(IssueAssigneeType assigneeType) {
        this.assigneeType = assigneeType;
    }

    public long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public void process(){
        this.status = PROCESSED;
        issue.assignTo(getAssigneeType(), getAssigneeId());
        issue.update();
    }
}
