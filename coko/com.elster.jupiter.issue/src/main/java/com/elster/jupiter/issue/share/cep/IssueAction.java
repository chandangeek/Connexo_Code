package com.elster.jupiter.issue.share.cep;

import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.Issue;

import java.util.List;
import java.util.Map;

public interface IssueAction {

    public void execute(Issue issue, Map<String, String> actionParameters);
    
    public Map<String, ParameterDefinition> getParameterDefinitions();
    
    public String getLocalizedName();

    public List<ParameterViolation> validate(Map<String, String> actionParameters);

    public List<ParameterViolation> validate(CreationRuleAction action);
}
