package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.rest.i18n.MessageSeeds;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.nls.Thesaurus;

public class CreationRuleActionPhaseInfo {
    private String uuid;
    private String title;

    public CreationRuleActionPhaseInfo(String phase) {
        this(CreationRuleActionPhase.fromString(phase));
    }

    public CreationRuleActionPhaseInfo(CreationRuleActionPhaseInfo phase) {
        if (phase != null) {
            this.uuid = phase.uuid;
            this.title = phase.title;
        }
    }

    public CreationRuleActionPhaseInfo(CreationRuleActionPhase phase){
        if (phase != null) {
            this.uuid = phase.name();
        }
    }
    
    public CreationRuleActionPhaseInfo(CreationRuleActionPhase phase, Thesaurus thesaurus){
        if (phase != null) {
            MessageSeeds phaseSeed = MessageSeeds.getByKey(phase.getTitleId());
            this.uuid = phase.name();
            this.title = thesaurus.getString(phaseSeed.getKey(), phaseSeed.getDefaultFormat());
        }
    }

    public String getUuid() {
        return uuid;
    }

    public String getTitle() {
        return title;
    }
}
