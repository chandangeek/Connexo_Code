package com.elster.jupiter.issue.share.cep;

import com.elster.jupiter.issue.share.entity.CreationRule;

import java.util.List;
import java.util.Map;

public interface CreationRuleTemplate {
    String getUUID();
    String getName();
    String getDescription();
    String getContent();
    String getIssueType();
    Map<String, ParameterDefinition> getParameterDefinitions();
    List<ParameterViolation> validate(CreationRule rule);
}
