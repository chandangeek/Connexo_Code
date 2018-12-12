/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;

import javax.inject.Inject;

public class CreationRuleTemplateInfoFactory {

    private final PropertyValueInfoService propertyValueInfoService;

    @Inject
    public CreationRuleTemplateInfoFactory(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    public CreationRuleTemplateInfo asInfo(CreationRuleTemplate template) {
        CreationRuleTemplateInfo info = new CreationRuleTemplateInfo();
        info.name = template.getName();
        info.displayName = template.getDisplayName();
        info.description = template.getDescription();
        info.properties = propertyValueInfoService.getPropertyInfos(template.getPropertySpecs());
        return info;
    }
}
