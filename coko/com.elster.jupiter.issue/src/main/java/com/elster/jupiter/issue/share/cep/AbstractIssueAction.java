package com.elster.jupiter.issue.share.cep;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.ActionParameter;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.service.IssueService;

import java.util.*;
import java.util.logging.Logger;

public abstract class AbstractIssueAction implements IssueAction {
    protected Map<String, ParameterDefinition> parameterDefinitions = new LinkedHashMap<>();

    @Override
    public Map<String, ParameterDefinition> getParameterDefinitions() {
        return parameterDefinitions;
    }

    @Override
    public List<ParameterViolation> validate(Map<String, String> actionParameters) {
        List<ParameterViolation> errors = new ArrayList<>();
        for (ParameterDefinition definition : parameterDefinitions.values()) {
            errors.addAll(definition.validate(actionParameters.get(definition.getKey()), ParameterDefinitionContext.ACTION));
        }
        return errors;
    }

    @Override
    public List<ParameterViolation> validate(CreationRuleAction action) {
        List<ParameterViolation> errors = new ArrayList<>();
        validateParameters(action, errors);
        return errors;
    }

    protected void validateParameters(CreationRuleAction action, List<ParameterViolation> errors){
        if(action == null) {
            throw new IllegalArgumentException("action is missing");
        }

        Map<String, ParameterDefinition> parameterDefinitionsCopy = new HashMap<>(parameterDefinitions);
        for (ActionParameter parameter : action.getParameters()) {
            ParameterDefinition definition = parameterDefinitionsCopy.remove(parameter.getKey());
            errors.addAll(definition.validate(parameter.getValue(), ParameterDefinitionContext.ACTION));
        }
        for (ParameterDefinition definition : parameterDefinitionsCopy.values()){
            if (!definition.getConstraint().isOptional()) {
                errors.add(new ParameterViolation(ParameterDefinitionContext.ACTION.wrapKey(definition.getKey()), MessageSeeds.ISSUE_CREATION_RULE_PARAMETER_ABSENT.getKey(), IssueService.COMPONENT_NAME));
            }
        }
    }

}
