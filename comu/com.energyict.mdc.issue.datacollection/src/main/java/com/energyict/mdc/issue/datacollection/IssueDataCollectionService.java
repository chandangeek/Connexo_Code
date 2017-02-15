/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.energyict.mdc.issue.datacollection.entity.HistoricalIssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;

import java.util.Optional;

public interface IssueDataCollectionService {

    String COMPONENT_NAME = "IDC";
    String DATA_COLLECTION_ISSUE = "datacollection";
    String DATA_COLLECTION_ISSUE_PREFIX = "DCI";

    Optional<? extends IssueDataCollection> findIssue(long id);

    Optional<? extends  IssueDataCollection> findAndLockIssueDataCollectionByIdAndVersion(long id, long version);

    Optional<OpenIssueDataCollection> findOpenIssue(long id);

    Optional<HistoricalIssueDataCollection> findHistoricalIssue(long id);

    OpenIssueDataCollection createIssue(OpenIssue baseIssue, IssueEvent issueEvent);

    <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers);

    Finder<? extends IssueDataCollection> findIssues(IssueDataCollectionFilter filter, Class<?>... eagers);

}