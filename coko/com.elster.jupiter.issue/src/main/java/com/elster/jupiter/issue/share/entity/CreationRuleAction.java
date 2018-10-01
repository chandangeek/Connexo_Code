/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.properties.HasDynamicPropertiesWithValues;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface CreationRuleAction extends HasDynamicPropertiesWithValues {

    IssueActionType getAction();

    CreationRuleActionPhase getPhase();

    CreationRule getRule();

    List<CreationRuleActionProperty> getCreationRuleActionProperties();
    
    void validate();

    String getFormattedProperties();
    
}
