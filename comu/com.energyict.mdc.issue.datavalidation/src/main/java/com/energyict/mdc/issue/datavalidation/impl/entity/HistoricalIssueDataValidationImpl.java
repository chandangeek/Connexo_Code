/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation.impl.entity;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.issue.datavalidation.HistoricalIssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.NotEstimatedBlock;
import com.energyict.mdc.issue.datavalidation.OpenIssueDataValidation;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HistoricalIssueDataValidationImpl extends IssueDataValidationImpl implements HistoricalIssueDataValidation {

    @IsPresent
    private Reference<HistoricalIssue> baseIssue = ValueReference.absent();
    
    @Valid
    private List<HistoricalIssueNotEstimatedBlockImpl> notEstimatedBlocks = new ArrayList<>();  

    @Inject
    public HistoricalIssueDataValidationImpl(DataModel dataModel, IssueDataValidationService issueDataValidationService) {
        super(dataModel, issueDataValidationService);
    }

    @Override
    HistoricalIssue getBaseIssue() {
        return baseIssue.orNull();
    }

    public void setIssue(HistoricalIssue issue) {
        this.baseIssue.set(issue);
    }

    void copy(OpenIssueDataValidation openIssueDataValidation) {
        for(NotEstimatedBlock block : openIssueDataValidation.getNotEstimatedBlocks()) {
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
