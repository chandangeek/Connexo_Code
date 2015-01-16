package com.elster.jupiter.issue.share.cep;

import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.Issue;

import java.util.List;
import java.util.Map;

public interface IssueAction {

    String getLocalizedName();
    
    boolean isApplicable(Issue issue);
    
    IssueActionResult execute(Issue issue, Map<String, String> actionParameters);

    Map<String, ParameterDefinition> getParameterDefinitions();

    List<ParameterViolation> validate(Map<String, String> actionParameters);

    List<ParameterViolation> validate(CreationRuleAction action);
}
