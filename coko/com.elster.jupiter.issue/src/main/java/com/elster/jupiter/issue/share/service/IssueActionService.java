package com.elster.jupiter.issue.share.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import aQute.bnd.annotation.ProviderType;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionFactory;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;

@ProviderType
public interface IssueActionService {
    
    Optional<IssueAction> createIssueAction(String factoryId, String issueActionImpl);
    
    List<IssueActionFactory> getRegisteredFactories();
    
    IssueActionResult executeAction(IssueActionType type, Issue issue, Map<String, Object> props);
    
    IssueActionType createActionType(String factoryId, String className, IssueType issueType);
    
    IssueActionType createActionType(String factoryId, String className, IssueType issueType, CreationRuleActionPhase phase);
    
    IssueActionType createActionType(String factoryId, String className, IssueReason issueReason);
    
    IssueActionType createActionType(String factoryId, String className, IssueReason issueReason, CreationRuleActionPhase phase);
    
    Optional<IssueActionType> findActionType(long id);
    
    Query<IssueActionType> getActionTypeQuery();
    
}
