/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import com.elster.jupiter.metering.ReadingType;

import java.util.Collection;
import java.util.Map;

public interface ValidationRuleBuilder {

    ValidationRuleBuilder active(boolean active);

    ValidationRuleBuilder withReadingType(ReadingType... readingType);

    ValidationRuleBuilder withReadingType(String... readingTypeMRID);

    ValidationRuleBuilder withReadingTypes(Collection<ReadingType> readingTypes);

    ValidationRuleBuilder withProperties(Map<String, Object> properties);

    ValidationRuleBuilder.PropertyBuilder havingProperty(String property);

    interface PropertyBuilder {
        ValidationRuleBuilder withValue(Object value);
    }

    ValidationRule create();
}
