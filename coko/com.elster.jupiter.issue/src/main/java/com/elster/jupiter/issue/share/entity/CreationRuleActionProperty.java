package com.elster.jupiter.issue.share.entity;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface CreationRuleActionProperty extends PersistentProperty {

    CreationRuleAction getAction();
    
}
