package com.elster.jupiter.issue.share.cep;

import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.google.common.base.Optional;

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
    Optional<? extends Issue> createIssue(Issue issue, IssueEvent event);
}
