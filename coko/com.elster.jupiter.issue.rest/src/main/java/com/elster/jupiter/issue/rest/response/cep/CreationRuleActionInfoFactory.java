/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.rest.response.IssueActionInfoFactory;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;

import javax.inject.Inject;

public class CreationRuleActionInfoFactory {

    private final PropertyValueInfoService propertyValueInfoService;
    private final IssueActionInfoFactory issueActionInfoFactory;
    private final Thesaurus thesaurus;

    @Inject
    public CreationRuleActionInfoFactory(PropertyValueInfoService propertyValueInfoService, IssueActionInfoFactory issueActionInfoFactory, Thesaurus thesaurus) {
        this.propertyValueInfoService = propertyValueInfoService;
        this.issueActionInfoFactory = issueActionInfoFactory;
        this.thesaurus = thesaurus;
    }

    public CreationRuleActionInfo asInfo(CreationRuleAction action) {
        CreationRuleActionInfo info = new CreationRuleActionInfo();
        info.phase = new CreationRuleActionPhaseInfo(action.getPhase(), thesaurus);
        info.type = issueActionInfoFactory.asInfo(action.getAction());
        info.properties = propertyValueInfoService.getPropertyInfos(action.getPropertySpecs(), action.getProperties());
        return info;
    }
}
