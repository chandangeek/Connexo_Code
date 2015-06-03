package com.elster.jupiter.issue.impl.records.validator;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.elster.jupiter.issue.impl.records.CreationRuleImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;

public class CreationRulePropertiesValidator extends AbstractPropertiesConstraintValidator<HasValidProperties, CreationRuleImpl> {

    @Inject
    public CreationRulePropertiesValidator(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    protected List<PropertySpec> getPropertySpecs(CreationRuleImpl creationRule) {
        return creationRule.getPropertySpecs();
    }
    
    @Override
    protected Map<String, Object> getProps(CreationRuleImpl creationRule) {
        return creationRule.getProps();
    }
}