package com.elster.jupiter.issue.share.cep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.ActionParameter;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import javax.inject.Inject;

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
        return validate(actionParameters, ParameterDefinitionContext.NONE);
    }

    protected List<ParameterViolation> validate(Map<String, String> actionParameters, ParameterDefinitionContext context) {
        List<ParameterViolation> errors = new ArrayList<>();
        Map<String, ParameterDefinition> parameterDefinitionsCopy = new HashMap<>(getParameterDefinitions());
        // Validate the passed parameters
        for (Map.Entry<String, String> entry : actionParameters.entrySet()) {
            ParameterDefinition definition = parameterDefinitionsCopy.remove(entry.getKey());
            if (definition != null) {
                errors.addAll(definition.validate(entry.getValue(), context));
            }
        }
        // Validate missing parameters
        for (ParameterDefinition definition : parameterDefinitionsCopy.values()){
            if (!definition.getConstraint().isOptional()) {
                errors.add(new ParameterViolation(context.wrapKey(definition.getKey()), MessageSeeds.ISSUE_CREATION_RULE_PARAMETER_ABSENT.getKey(), IssueService.COMPONENT_NAME));
            }
        }
        return errors;
    }

    @Override
    public List<ParameterViolation> validate(CreationRuleAction action) {
        if(action == null) {
            throw new IllegalArgumentException("action is missing");
        }
        List<ParameterViolation> errors = new ArrayList<>();
        List<ActionParameter> parameters = action.getParameters();
        if (parameters != null && !parameters.isEmpty()){
            Map<String, String> actionParameters = new HashMap<>(parameters.size());
            for (ActionParameter parameter : parameters) {
                actionParameters.put(parameter.getKey(), parameter.getValue());
            }
            errors = validate(actionParameters, ParameterDefinitionContext.ACTION);
        }
        return errors;
    }
    
    @Override
    public boolean isApplicable(Issue issue) {
        return issue != null;
    }
}
