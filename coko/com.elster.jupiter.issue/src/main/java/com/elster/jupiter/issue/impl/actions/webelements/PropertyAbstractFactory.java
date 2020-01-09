package com.elster.jupiter.issue.impl.actions.webelements;

import com.elster.jupiter.issue.share.PropertyFactory;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.PropertyType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

public abstract class PropertyAbstractFactory implements PropertyFactory {

    protected final PropertySpecService propertySpecService;
    protected final Thesaurus thesaurus;

    protected IssueType issueType;
    protected IssueReason issueReason;

    protected PropertyAbstractFactory(final PropertySpecService propertySpecService, final Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public abstract PropertySpec getElement(final String name, final TranslationKey displayName, final TranslationKey description);

    @Override
    public PropertySpec getElement(final String name, final TranslationKey displayName, final TranslationKey description, final IssueType issueType, final IssueReason issueReason) {
        this.issueType = issueType;
        this.issueReason = issueReason;
        return getElement(name, displayName, description);
    }

    @Override
    public abstract PropertyType getType();

}