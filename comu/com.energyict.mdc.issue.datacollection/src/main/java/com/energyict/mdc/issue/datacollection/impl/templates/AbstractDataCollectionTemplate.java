package com.energyict.mdc.issue.datacollection.impl.templates;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.HasTranslatableNameAndPropertiesImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;

public abstract class AbstractDataCollectionTemplate extends HasTranslatableNameAndPropertiesImpl implements CreationRuleTemplate {

    public AbstractDataCollectionTemplate() {
    }
    
    protected AbstractDataCollectionTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

}
