/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface EstimationRuleProperties {
    String getName();

    String getDisplayName();

    String getDescription();

    Object getValue();

    void setValue(Object value);

    EstimationRule getRule();
}
