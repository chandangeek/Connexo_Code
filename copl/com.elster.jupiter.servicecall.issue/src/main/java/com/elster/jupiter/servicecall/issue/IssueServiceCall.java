/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue;

import com.elster.jupiter.issue.share.entity.Issue;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface IssueServiceCall extends Issue {

    List<NotEstimatedBlock> getNotEstimatedBlocks();
    
}
