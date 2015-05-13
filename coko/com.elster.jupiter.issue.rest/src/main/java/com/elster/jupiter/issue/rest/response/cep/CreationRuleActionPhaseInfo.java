package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.rest.MessageSeeds;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.nls.Thesaurus;

public class CreationRuleActionPhaseInfo {
    private String uuid;
    private String title;
    private String description;

    public CreationRuleActionPhaseInfo() {}

    public CreationRuleActionPhaseInfo(CreationRuleActionPhaseInfo phase) {
        if (phase == null) {
            throw new IllegalArgumentException("CreationRuleActionPhaseInfo is initialized with the null CreationRuleActionPhaseInfo value");
        }
        this.uuid = phase.uuid;
        this.title = phase.title;
        this.description = phase.description;
    }

    public CreationRuleActionPhaseInfo(CreationRuleActionPhase phase){
        if (phase == null) {
            throw new IllegalArgumentException("CreationRuleActionPhaseInfo is initialized with the null CreationRuleActionPhase value");
        }
        this.uuid = phase.name();
    }
    
    public CreationRuleActionPhaseInfo(CreationRuleActionPhase phase, Thesaurus thesaurus){
        if (phase == null || thesaurus == null) {
            throw new IllegalArgumentException("CreationRuleActionPhaseInfo is initialized with the null CreationRuleActionPhase or Thesaurus value");
        }
        MessageSeeds phaseSeed = MessageSeeds.getByKey(phase.getTitleId());
        this.uuid = phase.name();
        this.title = thesaurus.getString(phaseSeed.getKey(), phaseSeed.getDefaultFormat());
        phaseSeed = MessageSeeds.getByKey(phase.getDescriptionId());
        this.description = thesaurus.getString(phaseSeed.getKey(), phaseSeed.getDefaultFormat());
    }

    public String getUuid() {
        return uuid;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
