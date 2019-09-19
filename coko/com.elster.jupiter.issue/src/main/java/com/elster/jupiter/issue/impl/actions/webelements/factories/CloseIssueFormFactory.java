package com.elster.jupiter.issue.impl.actions.webelements.factories;

import com.elster.jupiter.issue.impl.actions.webelements.PropertyAbstractFactory;
import com.elster.jupiter.issue.share.entity.values.CloseIssueFormValue;
import com.elster.jupiter.issue.share.entity.values.factories.CloseIssueFormValueFactory;
import com.elster.jupiter.issue.share.entity.PropertyType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import javax.inject.Inject;

public class CloseIssueFormFactory extends PropertyAbstractFactory {

    private final IssueService issueService;

    @Inject
    public CloseIssueFormFactory(final PropertySpecService propertySpecService, final Thesaurus thesaurus, final IssueService issueService) {
        super(propertySpecService, thesaurus);
        this.issueService = issueService;
    }

    @Override
    public PropertySpec getElement(final String name, final TranslationKey displayName, final TranslationKey description) {
        return propertySpecService
                .specForValuesOf(new CloseIssueFormValueFactory(issueService))
                .named(name, displayName)
                .fromThesaurus(thesaurus)
                .setDefaultValue(new CloseIssueFormValue(null, null, null))
                .markRequired()
                .finish();
    }

    @Override
    public PropertyType getType() {
        return PropertyType.CLOSE_ISSUE_FORM;
    }
}
