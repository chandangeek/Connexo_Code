package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.Store;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;

import javax.inject.Inject;
import java.util.Optional;

public class IssueReasonFactory implements Factory<IssueReason> {
    private final IssueService issueService;
    private final Store store;

    @Inject
    public IssueReasonFactory(IssueService issueService, Store store) {
        this.issueService = issueService;
        this.store = store;
    }

    public IssueReason get(){
        Log.write(this);
        Optional<IssueType> issueTypeRef = issueService.findIssueType(IssueDataCollectionService.ISSUE_TYPE_UUID);
        if (!issueTypeRef.isPresent()){
            throw new UnableToCreate("Unable to find the data collection issue type");
        }
        IssueReason reason = issueService.createReason(Constants.IssueReason.DAILY_BILLING_READ_FAILED.getKey(), issueTypeRef.get(), Constants.IssueReason.DAILY_BILLING_READ_FAILED);
        store.add(IssueReason.class, reason);
        reason = issueService.createReason(Constants.IssueReason.SUSPECT_VALUES.getKey(), issueTypeRef.get(), Constants.IssueReason.SUSPECT_VALUES);
        store.add(IssueReason.class, reason);
        return reason;
    }

}
