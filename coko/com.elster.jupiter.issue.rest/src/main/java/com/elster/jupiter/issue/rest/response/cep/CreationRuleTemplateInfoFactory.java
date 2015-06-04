package com.elster.jupiter.issue.rest.response.cep;

import javax.inject.Inject;

import com.elster.jupiter.issue.rest.response.PropertyUtils;
import com.elster.jupiter.issue.share.CreationRuleTemplate;

public class CreationRuleTemplateInfoFactory {
    
    private final PropertyUtils propertyUtils;

    @Inject
    public CreationRuleTemplateInfoFactory(PropertyUtils propertyUtils) {
        this.propertyUtils = propertyUtils;
    }

    public CreationRuleTemplateInfo asInfo(CreationRuleTemplate template) {
        CreationRuleTemplateInfo info = new CreationRuleTemplateInfo();
        info.name = template.getName();
        info.displayName = template.getDisplayName();
        info.description = template.getDescription();
        info.properties = propertyUtils.convertPropertySpecsToPropertyInfos(template.getPropertySpecs());
        return info;
    }
}
