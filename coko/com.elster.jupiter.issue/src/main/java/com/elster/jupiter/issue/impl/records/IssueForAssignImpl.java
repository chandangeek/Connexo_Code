/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueForAssign;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.parties.Party;

import java.time.Instant;
import java.util.Optional;

public class IssueForAssignImpl implements IssueForAssign{
    private long id;
    private long version;

    // Fields for check
    private boolean isProcessed = false;
    private String outageRegion;
    private String customer;
    private String reason;

    private Issue issue;

    public IssueForAssignImpl(Issue issue, Instant now) {
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
            Optional<Party> customerRef = usagePoint.getCustomer(now);
            if (customerRef.isPresent()){
                setCustomer(customerRef.get().getName());
            }
        }
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String getOutageRegion() {
        return outageRegion;
    }

    public void setOutageRegion(String outageRegion) {
        this.outageRegion = outageRegion;
    }

    @Override
    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    @Override
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public boolean isProcessed(){
        return isProcessed;
    }

    @Override
    public void assignTo(Long userId, Long workGroup){
        isProcessed = true;
        issue.assignTo(userId, workGroup);
        issue.update();
    }

    @Override
    public void assignTo(String type, long assigneeId){
        isProcessed = true;
        issue.assignTo(assigneeId, null);
        issue.update();
    }
}
