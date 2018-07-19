/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation;

import com.elster.jupiter.issue.share.entity.Issue;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface UsagePointIssueDataValidation extends Issue {

    List<UsagePointNotEstimatedBlock> getNotEstimatedBlocks();
    
}
