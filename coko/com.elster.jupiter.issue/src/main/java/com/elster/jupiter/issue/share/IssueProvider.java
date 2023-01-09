/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.OpenIssue;

import aQute.bnd.annotation.ConsumerType;

import java.util.Optional;
import java.util.Set;

@ConsumerType
public interface IssueProvider {

    Optional<? extends OpenIssue> getOpenIssue(OpenIssue issue);

    Optional<? extends HistoricalIssue> getHistoricalIssue(HistoricalIssue issue);

    Optional<? extends Issue> findIssue(long id);

    Set<String> getIssueTypeIdentifiers();

}
