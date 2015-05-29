package com.elster.jupiter.issue.impl.records;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;

public class IssueActionPropertiesConstraintValidator extends AbstractPropertiesConstraintValidator<Annotation, AbstractIssueAction> {

    @Inject
    public IssueActionPropertiesConstraintValidator(Thesaurus thesaurus) {
        super(thesaurus);
    }
    
    @Override
    protected List<PropertySpec> getPropertySpecs(AbstractIssueAction issueAction) {
        return issueAction.getPropertySpecs();
    }

    @Override
    protected Map<String, Object> getProps(AbstractIssueAction issueAction) {
        return issueAction.getProps();
    }
}
