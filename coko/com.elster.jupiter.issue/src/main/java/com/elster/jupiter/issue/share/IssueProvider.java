/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share;

import java.util.Optional;

import aQute.bnd.annotation.ConsumerType;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.OpenIssue;

@ConsumerType
public interface IssueProvider {
    
    Optional<? extends OpenIssue> getOpenIssue(OpenIssue issue);
    
    Optional<? extends HistoricalIssue> getHistoricalIssue(HistoricalIssue issue);

    Optional<? extends Issue> findIssue(long id);
    
}
