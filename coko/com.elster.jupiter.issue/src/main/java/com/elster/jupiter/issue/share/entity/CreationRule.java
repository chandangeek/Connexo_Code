package com.elster.jupiter.issue.share.entity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import aQute.bnd.annotation.ProviderType;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleUpdater;
import com.elster.jupiter.properties.PropertySpec;

@ProviderType
public interface CreationRule extends Entity {

    String getName();

    String getComment();

    String getContent();

    byte[] getData();

    IssueReason getReason();

    long getDueInValue();

    DueInType getDueInType();

    String getTemplateImpl();

    CreationRuleTemplate getTemplate();

    List<PropertySpec> getPropertySpecs();
    
    PropertySpec getPropertySpec(String propertyName);
    
    String getDisplayName(String propertyName);

    List<CreationRuleProperty> getProperties();

    List<CreationRuleAction> getActions();

    Instant getObsoleteTime();

    CreationRuleUpdater startUpdate();

    Map<String, Object> getProps();
}
