package com.elster.jupiter.issue.datacollection.templates;

import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.datacollection.impl.i18n.MessageSeeds;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.ParameterDefinition;
import com.elster.jupiter.issue.share.cep.ParameterDefinitionContext;
import com.elster.jupiter.issue.share.cep.ParameterViolation;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleParameter;
import com.elster.jupiter.nls.Thesaurus;

import java.util.*;

public abstract class AbstractTemplate implements CreationRuleTemplate {
    private final Map<String, ParameterDefinition> parameterDefinitions;
    private volatile Thesaurus thesaurus;

    protected AbstractTemplate() {
        parameterDefinitions = new LinkedHashMap<>();
    }

    @Override
    public Map<String, ParameterDefinition> getParameterDefinitions() {
        return parameterDefinitions;
    }

    protected void addParameterDefinition(ParameterDefinition definition){
        if (definition != null) {
            getParameterDefinitions().put(definition.getKey(), definition);
        }
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected String getString(MessageSeeds seed){
        if (seed != null) {
            return getThesaurus().getString(seed.getKey(), seed.getDefaultFormat());
        }
        return "";
    }

    protected void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getIssueType() {
        return ModuleConstants.ISSUE_TYPE_UUID;
    }

    @Override
    public List<ParameterViolation> validate(CreationRule rule) {
        List<ParameterViolation> errors = new ArrayList<>();
        if(rule == null){
            throw new IllegalArgumentException("Rule is missing");
        }

        Map<String, ParameterDefinition> parameterDefinitionsCopy = getParameterDefinitionsForValidation();
        for (CreationRuleParameter parameter : rule.getParameters()) {
            ParameterDefinition definition = parameterDefinitionsCopy.remove(parameter.getKey());
            errors.addAll(definition.validate(parameter.getValue(), ParameterDefinitionContext.RULE));
        }
        for (ParameterDefinition definition : parameterDefinitionsCopy.values()) {
            if (!definition.getConstraint().isOptional()) {
                errors.add(new ParameterViolation(ParameterDefinitionContext.RULE.wrapKey(definition.getKey()), MessageSeeds.ISSUE_CREATION_RULE_PARAMETER_ABSENT.getKey(), ModuleConstants.COMPONENT_NAME));
            }
        }
        return errors;
    }

    protected Map<String, ParameterDefinition> getParameterDefinitionsForValidation(){
        return new HashMap<>(parameterDefinitions);
    }
}
