package com.energyict.mdc.issue.datacollection;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.Issue;
import com.energyict.mdc.issue.datacollection.entity.HistoricalIssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.google.common.base.Optional;


public interface IssueDataCollectionService {
    public static final String COMPONENT_NAME = "IDC";
    public static final String ISSUE_TYPE_UUID = "datacollection";

    Optional<IssueDataCollection> findIssue(long id);
    Optional<OpenIssueDataCollection> findOpenIssue(long id);
    Optional<HistoricalIssueDataCollection> findHistoricalIssue(long id);

    OpenIssueDataCollection createIssue(Issue baseIssue);

    <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers);
}
