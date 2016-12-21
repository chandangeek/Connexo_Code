package com.elster.jupiter.issue.share.entity;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.issue.impl.records.UniqueNamed;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleUpdater;
import com.elster.jupiter.properties.HasDynamicPropertiesWithValues;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface CreationRule extends Entity, UniqueNamed, HasDynamicPropertiesWithValues {

    String getName();

    String getComment();

    String getContent();

    byte[] getData();

    IssueReason getReason();

    IssueType getIssueType();

    long getDueInValue();

    DueInType getDueInType();

    String getTemplateImpl();

    CreationRuleTemplate getTemplate();

    List<CreationRuleProperty> getCreationRuleProperties();

    List<CreationRuleAction> getActions();

    Instant getObsoleteTime();

    CreationRuleUpdater startUpdate();

}
