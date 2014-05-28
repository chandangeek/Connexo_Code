package com.elster.jupiter.issue.share.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.cep.IssueAction;
import com.elster.jupiter.issue.share.cep.IssueActionFactory;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.google.common.base.Optional;

import java.util.Map;

public interface IssueActionService {
    IssueAction createIssueAction(String factoryUuid, String issueActionClassName);
    void executeAction(IssueActionType type, Issue issue, Map<String, String> actionParams);
    IssueActionType createActionType(String factoryId, String className, IssueType issueType);
    Optional<IssueActionType> findActionType(long id);
    Query<IssueActionType> getActionTypeQuery();
}
