/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.ReadingType;

import java.util.Collection;
import java.util.Map;

public interface EstimationRuleBuilder {

    EstimationRuleBuilder active(boolean active);

    EstimationRuleBuilder withReadingType(ReadingType... readingType);

    EstimationRuleBuilder withReadingType(String... readingTypeMRID);

    EstimationRuleBuilder withReadingTypes(Collection<ReadingType> readingTypes);

    EstimationRuleBuilder withProperties(Map<String, Object> properties);

    EstimationRuleBuilder.PropertyBuilder havingProperty(String property);

    interface PropertyBuilder {
        EstimationRuleBuilder withValue(Object value);
    }

    EstimationRule create();
}
