package com.elster.jupiter.issue.share.cep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.ActionParameter;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.google.inject.Inject;

public abstract class AbstractIssueAction implements IssueAction {
    
    private NlsService nlsService;
    private Thesaurus thesaurus;
    protected Map<String, ParameterDefinition> parameterDefinitions = new LinkedHashMap<>();
    
    @Inject
    public AbstractIssueAction(NlsService nlsService, Thesaurus thesaurus) {
        this.nlsService = nlsService;
        this.thesaurus = thesaurus;
    }
    
    public Thesaurus getThesaurus() {
        return thesaurus;
    }
    
    public void validateParametersOrThrowException(Map<String, String> actionParameters) {
        List<ParameterViolation> violations = validate(actionParameters);
        if (!violations.isEmpty()) {
            CreationRuleOrActionValidationException exception = new CreationRuleOrActionValidationException(nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.DOMAIN), MessageSeeds.ACTION_INCORRECT_PARAMETERS);
            exception.addErrors(violations);
            throw exception;
        }
    }
    
    @Override
    public Map<String, ParameterDefinition> getParameterDefinitions() {
        return parameterDefinitions;
    }

    @Override
    public List<ParameterViolation> validate(Map<String, String> actionParameters) {
        List<ParameterViolation> errors = new ArrayList<>();
        for (ParameterDefinition definition : getParameterDefinitions().values()) {
            errors.addAll(definition.validate(actionParameters.get(definition.getKey()), ParameterDefinitionContext.NONE));
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

        Map<String, ParameterDefinition> parameterDefinitionsCopy = new HashMap<>(getParameterDefinitions());
        for (ActionParameter parameter : action.getParameters()) {
            ParameterDefinition definition = parameterDefinitionsCopy.remove(parameter.getKey());
            if (definition != null) {
                errors.addAll(definition.validate(parameter.getValue(), ParameterDefinitionContext.ACTION));
            }
        }
        for (ParameterDefinition definition : parameterDefinitionsCopy.values()){
            if (!definition.getConstraint().isOptional()) {
                errors.add(new ParameterViolation(ParameterDefinitionContext.ACTION.wrapKey(definition.getKey()), MessageSeeds.ISSUE_CREATION_RULE_PARAMETER_ABSENT.getKey(), IssueService.COMPONENT_NAME));
            }
        }
    }

}
