package com.elster.jupiter.issue.share.service;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface IssueCreationService {
    
    CreationRuleBuilder newCreationRule();

    Optional<CreationRule> findCreationRuleById(long id);
    
    Optional<CreationRule> findAndLockCreationRuleByIdAndVersion(long id, long version);

    Query<CreationRule> getCreationRuleQuery(Class<?>... eagers);

    Optional<CreationRuleTemplate> findCreationRuleTemplate(String name);

    List<CreationRuleTemplate> getCreationRuleTemplates();

    void dispatchCreationEvent(List<IssueEvent> events);

    void processIssueCreationEvent(long ruleId, IssueEvent event);

    void processIssueResolutionEvent(long ruleId, IssueEvent event);

    boolean reReadRules();
    
    @ProviderType
    interface CreationRuleBuilder {
        
        CreationRuleBuilder setName(String name);
        
        CreationRuleBuilder setComment(String comment);

        CreationRuleBuilder setIssueType(IssueType issueType);

        CreationRuleBuilder setReason(IssueReason reason);
        
        CreationRuleBuilder setDueInTime(DueInType dueInType, long dueInValue);
        
        CreationRuleBuilder setTemplate(String name);
        
        CreationRuleBuilder setProperties(Map<String, Object> props);
        
        CreationRuleActionBuilder newCreationRuleAction();
        
        CreationRule complete();
        
    }
    
    @ProviderType
    interface CreationRuleUpdater extends CreationRuleBuilder {
        
        CreationRuleUpdater removeActions();
        
    }
    
    @ProviderType
    interface CreationRuleActionBuilder {
        
        CreationRuleActionBuilder setActionType(IssueActionType issueActionType);
        
        CreationRuleActionBuilder setPhase(CreationRuleActionPhase phase);
        
        CreationRuleActionBuilder addProperty(String name, Object value);
        
        CreationRuleAction complete();
        
    }
}
