/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.IssueTypes;
import com.elster.jupiter.metering.groups.EndDeviceGroup;

import aQute.bnd.annotation.ProviderType;

import javax.naming.OperationNotSupportedException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface IssueCreationService {
    
    CreationRuleBuilder newCreationRule();

    Optional<CreationRule> findCreationRuleById(long id);
    
    Optional<CreationRule> findAndLockCreationRuleByIdAndVersion(long id, long version);

    Query<CreationRule> getCreationRuleQuery(Class<?>... eagers);

    List<CreationRuleAction> findActionsByMultiValueProperty(List<IssueTypes> issueTypes, String propertyKey, List<String> groupIdsList);

    Optional<CreationRuleTemplate> findCreationRuleTemplate(String name);

    List<CreationRuleTemplate> getCreationRuleTemplates();

    void dispatchCreationEvent(List<IssueEvent> events);

    void processIssueCreationEvent(long ruleId, IssueEvent event);

    void processAlarmCreationEvent(int ruleId, IssueEvent event, boolean logOnSameAlarm);

    void processIssueResolutionEvent(long ruleId, IssueEvent event);

    void closeAllOpenIssuesResolutionEvent(long ruleId, IssueEvent event) throws OperationNotSupportedException;

    boolean reReadRules();

    @ProviderType
    interface CreationRuleBuilder {
        
        CreationRuleBuilder setName(String name);
        
        CreationRuleBuilder setComment(String comment);

        CreationRuleBuilder setIssueType(IssueType issueType);

        CreationRuleBuilder setReason(IssueReason reason);
        
        CreationRuleBuilder setDueInTime(DueInType dueInType, long dueInValue);
        
        CreationRuleBuilder setTemplate(String name);

        CreationRuleBuilder setPriority(Priority priority);

        CreationRuleBuilder activate();

        CreationRuleBuilder deactivate();
        
        CreationRuleBuilder setProperties(Map<String, Object> props);
        
        CreationRuleActionBuilder newCreationRuleAction();
        
        CreationRuleBuilder setExcludedDeviceGroups(List<EndDeviceGroup> deviceGroupsList);

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
