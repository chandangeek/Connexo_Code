package com.elster.jupiter.issue.share.entity;

import java.util.List;

import com.elster.jupiter.properties.HasDynamicPropertiesWithValues;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface CreationRuleAction extends HasDynamicPropertiesWithValues {

    IssueActionType getAction();

    CreationRuleActionPhase getPhase();

    CreationRule getRule();

    List<CreationRuleActionProperty> getCreationRuleActionProperties();
    
    void validate();
    
}
