/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface CreationRuleActionProperty extends PersistentProperty {

    CreationRuleAction getAction();
    
}
