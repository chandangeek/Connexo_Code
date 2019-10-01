package com.elster.jupiter.issue.impl.actions.webelements;

import com.elster.jupiter.issue.share.PropertyFactory;
import com.elster.jupiter.issue.share.entity.PropertyType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

public abstract class PropertyAbstractFactory implements PropertyFactory {

    protected final PropertySpecService propertySpecService;
    protected final Thesaurus thesaurus;

    protected PropertyAbstractFactory(final PropertySpecService propertySpecService, final Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public abstract PropertySpec getElement(String name, TranslationKey displayName, TranslationKey description);

    public abstract PropertyType getType();

}