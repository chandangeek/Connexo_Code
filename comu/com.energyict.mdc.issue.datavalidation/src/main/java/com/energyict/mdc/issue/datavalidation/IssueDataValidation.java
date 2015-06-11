package com.energyict.mdc.issue.datavalidation;

import java.util.List;

import com.elster.jupiter.issue.share.entity.Issue;

public interface IssueDataValidation extends Issue {

    List<NotEstimatedBlock> getNotEstimatedBlocks();
    
}
