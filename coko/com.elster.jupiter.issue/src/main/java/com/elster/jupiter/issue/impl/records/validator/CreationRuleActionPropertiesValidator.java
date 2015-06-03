package com.elster.jupiter.issue.impl.records.validator;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.elster.jupiter.issue.impl.records.CreationRuleActionImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;

public class CreationRuleActionPropertiesValidator extends AbstractPropertiesConstraintValidator<HasValidProperties, CreationRuleActionImpl> {

    @Inject
    public CreationRuleActionPropertiesValidator(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    protected List<PropertySpec> getPropertySpecs(CreationRuleActionImpl creationRuleAction) {
        return creationRuleAction.getPropertySpecs();
    }

    @Override
    protected Map<String, Object> getProps(CreationRuleActionImpl creationRuleAction) {
        return creationRuleAction.getProps();
    }

}