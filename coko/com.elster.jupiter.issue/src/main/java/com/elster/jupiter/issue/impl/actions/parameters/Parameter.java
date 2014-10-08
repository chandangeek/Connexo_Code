package com.elster.jupiter.issue.impl.actions.parameters;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.util.exception.MessageSeed;

public enum Parameter {

    CLOSE_STATUS("closeStatus", MessageSeeds.PARAMETER_CLOSE_STATUS),
    COMMENT("issueComment", MessageSeeds.PARAMETER_COMMENT),
    ASSIGNEE("issueAssignee", MessageSeeds.PARAMETER_ASSIGNEE),
    ASSIGNEE_USER("issueAssigneeUser", MessageSeeds.PARAMETER_ASSIGNEE_USER),
    ASSIGNEE_ROLE("issueAssigneeRole", MessageSeeds.PARAMETER_ASSIGNEE_ROLE),
    ASSIGNEE_GROUP("issueAssigneeGroup", MessageSeeds.PARAMETER_ASSIGNEE_GROUP),
    ;

    private String key;
    private MessageSeed seed;

    private Parameter(String key, MessageSeed seed) {
        this.key = key;
        this.seed = seed;
    }

    public String getKey() {
        return key;
    }
    
    public String getTranslationKey() {
        return seed.getKey();
    }
    
    public String getDefaultValue() {
        return seed.getDefaultFormat();
    }        
}