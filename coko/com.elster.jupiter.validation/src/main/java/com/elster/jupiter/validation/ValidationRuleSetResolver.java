/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.RangeSet;

import java.time.Instant;
import java.util.Map;

@ProviderType
public interface ValidationRuleSetResolver {

    Map<ValidationRuleSet, RangeSet<Instant>> resolve(ValidationContext validationContext);

    boolean isValidationRuleSetInUse(ValidationRuleSet ruleset);

    default boolean isValidationRuleSetActiveOnDeviceConfig(long validationRuleSetId, long deviceId) {
        return false;
    }

    default boolean canHandleRuleSetStatus() {
        return false;
    }
}
