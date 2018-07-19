/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl.entity;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.insight.issue.datavalidation.UsagePointHistoricalIssueDataValidation;
import com.elster.insight.issue.datavalidation.UsagePointOpenIssueDataValidation;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidationService;
import com.elster.insight.issue.datavalidation.UsagePointNotEstimatedBlock;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UsagePointHistoricalIssueDataValidationImpl extends UsagePointIssueDataValidationImpl implements UsagePointHistoricalIssueDataValidation {

    @IsPresent
    private Reference<HistoricalIssue> baseIssue = ValueReference.absent();
    
    @Valid
    private List<UsagePointHistoricalIssueUsagePointUsagePointNotEstimatedBlockImpl> notEstimatedBlocks = new ArrayList<>();

    @Inject
    public UsagePointHistoricalIssueDataValidationImpl(DataModel dataModel, UsagePointIssueDataValidationService usagePointIssueDataValidationService) {
        super(dataModel, usagePointIssueDataValidationService);
    }

    @Override
    HistoricalIssue getBaseIssue() {
        return baseIssue.orNull();
    }

    public void setIssue(HistoricalIssue issue) {
        this.baseIssue.set(issue);
    }

    void copy(UsagePointOpenIssueDataValidation openIssueDataValidation) {
        for(UsagePointNotEstimatedBlock block : openIssueDataValidation.getNotEstimatedBlocks()) {
            UsagePointHistoricalIssueUsagePointUsagePointNotEstimatedBlockImpl historicalBlock = getDataModel().getInstance(UsagePointHistoricalIssueUsagePointUsagePointNotEstimatedBlockImpl.class);
            historicalBlock.init(this, block);
            notEstimatedBlocks.add(historicalBlock);
        }
    }
    
    @Override
    public List<UsagePointNotEstimatedBlock> getNotEstimatedBlocks() {
        return Collections.unmodifiableList(notEstimatedBlocks);
    }
}
