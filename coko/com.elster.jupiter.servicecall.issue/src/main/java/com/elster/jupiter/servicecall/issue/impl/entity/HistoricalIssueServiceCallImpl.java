/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl.entity;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.servicecall.issue.HistoricalIssueServiceCall;
import com.elster.jupiter.servicecall.issue.IssueServiceCallService;
import com.elster.jupiter.servicecall.issue.NotEstimatedBlock;
import com.elster.jupiter.servicecall.issue.OpenIssueServiceCall;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HistoricalIssueServiceCallImpl extends IssueServiceCallImpl implements HistoricalIssueServiceCall {

    @IsPresent
    private Reference<HistoricalIssue> baseIssue = ValueReference.absent();
    
    @Valid
    private List<HistoricalIssueNotEstimatedBlockImpl> notEstimatedBlocks = new ArrayList<>();

    @Inject
    public HistoricalIssueServiceCallImpl(DataModel dataModel, IssueServiceCallService issueServiceCallService) {
        super(dataModel, issueServiceCallService);
    }

    @Override
    HistoricalIssue getBaseIssue() {
        return baseIssue.orNull();
    }

    public void setIssue(HistoricalIssue issue) {
        this.baseIssue.set(issue);
    }

    void copy(OpenIssueServiceCall openIssueServiceCall) {
        for(NotEstimatedBlock block : openIssueServiceCall.getNotEstimatedBlocks()) {
            HistoricalIssueNotEstimatedBlockImpl historicalBlock = getDataModel().getInstance(HistoricalIssueNotEstimatedBlockImpl.class);
            historicalBlock.init(this, block);
            notEstimatedBlocks.add(historicalBlock);
        }
    }
    
    @Override
    public List<NotEstimatedBlock> getNotEstimatedBlocks() {
        return Collections.unmodifiableList(notEstimatedBlocks);
    }
}
