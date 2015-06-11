package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.issue.datavalidation.HistoricalIssueDataValidation;
import com.energyict.mdc.issue.datavalidation.HistoricalIssueNotEstimatedBlock;

public class HistoricalIssueNotEstimatedBlockImpl extends NotEstimatedBlockImpl implements HistoricalIssueNotEstimatedBlock {

    @IsPresent
    private Reference<HistoricalIssueDataValidation> issue = ValueReference.absent();

}
