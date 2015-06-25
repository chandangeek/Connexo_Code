package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.rest.response.IssueActionInfoFactory;
import com.elster.jupiter.issue.rest.response.PropertyUtils;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;

public class CreationRuleActionInfoFactory {

    private final PropertyUtils propertyUtils;
    private final IssueActionInfoFactory issueActionInfoFactory;
    private final Thesaurus thesaurus;

    @Inject
    public CreationRuleActionInfoFactory(PropertyUtils propertyUtils, IssueActionInfoFactory issueActionInfoFactory, Thesaurus thesaurus) {
        this.propertyUtils = propertyUtils;
        this.issueActionInfoFactory = issueActionInfoFactory;
        this.thesaurus = thesaurus;
    }

    public CreationRuleActionInfo asInfo(CreationRuleAction action) {
        CreationRuleActionInfo info = new CreationRuleActionInfo();
        info.phase = new CreationRuleActionPhaseInfo(action.getPhase(), thesaurus);
        info.type = issueActionInfoFactory.asInfo(action.getAction());
        info.properties = propertyUtils.convertPropertySpecsToPropertyInfos(action.getPropertySpecs(), action.getProperties());
        return info;
    }
}
