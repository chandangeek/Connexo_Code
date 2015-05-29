package com.elster.jupiter.issue.share.entity;

import java.util.List;
import java.util.Map;

import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface CreationRuleAction {

    IssueActionType getAction();

    CreationRuleActionPhase getPhase();

    List<CreationRuleActionProperty> getProperties();

    CreationRule getRule();

    List<PropertySpec> getPropertySpecs();

    PropertySpec getPropertySpec(String propertyName);

    String getDisplayName(String propertyName);

    Map<String, Object> getProps();

}
