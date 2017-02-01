/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ValidationRuleProperties {
    String getName();

    String getDisplayName();

    Object getValue();

    void setValue(Object value);

    ValidationRule getRule();
}
