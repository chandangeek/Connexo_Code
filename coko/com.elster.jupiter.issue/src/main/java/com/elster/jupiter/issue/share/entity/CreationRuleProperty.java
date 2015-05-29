package com.elster.jupiter.issue.share.entity;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface CreationRuleProperty extends PersistentProperty {

    CreationRule getRule();

}
