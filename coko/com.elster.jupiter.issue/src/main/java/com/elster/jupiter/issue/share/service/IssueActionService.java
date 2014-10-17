package com.elster.jupiter.issue.share.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.cep.IssueAction;
import com.elster.jupiter.issue.share.cep.IssueActionFactory;
import com.elster.jupiter.issue.share.cep.IssueActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import java.util.Optional;

import java.util.List;
import java.util.Map;

public interface IssueActionService {
    IssueAction createIssueAction(String factoryUuid, String issueActionClassName);
    IssueActionResult executeAction(IssueActionType type, Issue issue, Map<String, String> actionParams);
    IssueActionType createActionType(String factoryId, String className, IssueType issueType);
    IssueActionType createActionType(String factoryId, String className, IssueReason issueReason);
    Optional<IssueActionType> findActionType(long id);
    Query<IssueActionType> getActionTypeQuery();
    List<IssueActionFactory> getRegisteredFactories();
}
