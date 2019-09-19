package com.elster.jupiter.issue.impl.actions.webelements.factories;

import com.elster.jupiter.issue.impl.actions.webelements.PropertyAbstractFactory;
import com.elster.jupiter.issue.share.entity.PropertyType;
import com.elster.jupiter.issue.share.entity.values.AssignIssueFormValue;
import com.elster.jupiter.issue.share.entity.values.factories.AssignIssueFormValueFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

public class AssigneeElementGroupFactory extends PropertyAbstractFactory {

    private final UserService userService;

    @Inject
    public AssigneeElementGroupFactory(final PropertySpecService propertySpecService, final Thesaurus thesaurus, final UserService userService) {
        super(propertySpecService, thesaurus);
        this.userService = userService;
    }

    @Override
    public PropertySpec getElement(final String name, final TranslationKey displayName, final TranslationKey description) {
        return propertySpecService
                .specForValuesOf(new AssignIssueFormValueFactory(userService))
                .named(name, displayName)
                .fromThesaurus(thesaurus)
                .setDefaultValue(new AssignIssueFormValue(Boolean.FALSE, null, null, ""))
                .finish();
    }

    @Override
    public PropertyType getType() {
        return PropertyType.ASSIGN_ISSUE_FORM;
    }
}
