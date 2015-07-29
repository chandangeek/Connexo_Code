package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.rest.MessageSeeds;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.nls.Thesaurus;

public class CreationRuleActionPhaseInfo {
    public String uuid;
    public String title;
    public String description;

    public CreationRuleActionPhaseInfo() {}
    
    public CreationRuleActionPhaseInfo(CreationRuleActionPhase phase, Thesaurus thesaurus){
        MessageSeeds phaseSeed = MessageSeeds.getByKey(phase.getTitleId());
        this.uuid = phase.name();
        this.title = thesaurus.getString(phaseSeed.getKey(), phaseSeed.getDefaultFormat());
        phaseSeed = MessageSeeds.getByKey(phase.getDescriptionId());
        this.description = thesaurus.getString(phaseSeed.getKey(), phaseSeed.getDefaultFormat());
    }
}
