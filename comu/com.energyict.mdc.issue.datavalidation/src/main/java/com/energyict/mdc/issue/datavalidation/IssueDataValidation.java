package com.energyict.mdc.issue.datavalidation;

import java.util.List;

import aQute.bnd.annotation.ProviderType;

import com.elster.jupiter.issue.share.entity.Issue;

@ProviderType
public interface IssueDataValidation extends Issue {

    List<NotEstimatedBlock> getNotEstimatedBlocks();
    
}
