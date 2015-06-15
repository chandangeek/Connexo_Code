package com.energyict.mdc.issue.datacollection.impl.templates;

import javax.inject.Inject;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;

public abstract class AbstractDataCollectionTemplate implements CreationRuleTemplate {

    private Thesaurus thesaurus;
    private PropertySpecService propertySpecService;
    
    public AbstractDataCollectionTemplate() {
    }
    
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Inject
    protected AbstractDataCollectionTemplate(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    protected void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    protected void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }
}
