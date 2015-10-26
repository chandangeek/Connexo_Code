package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.rest.TranslationKeys;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.nls.Thesaurus;

public class CreationRuleActionPhaseInfo {
    public String uuid;
    public String title;
    public String description;

    public CreationRuleActionPhaseInfo() {}

    public CreationRuleActionPhaseInfo(CreationRuleActionPhase phase, Thesaurus thesaurus) {
        this();
        this.uuid = phase.name();
        this.title = thesaurus.getFormat(TranslationKeys.from(phase.getTitleId())).format();
        this.description = thesaurus.getFormat(TranslationKeys.from(phase.getDescriptionId())).format();
    }

}