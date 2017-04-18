/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.aggregation.ReadingQualityComment;

import aQute.bnd.annotation.ProviderType;

import java.util.Collection;
import java.util.Map;

@ProviderType
public interface EstimationRuleBuilder {

    EstimationRuleBuilder active(boolean active);

    EstimationRuleBuilder withReadingType(ReadingType... readingType);

    EstimationRuleBuilder withReadingType(String... readingTypeMRID);

    EstimationRuleBuilder withReadingTypes(Collection<ReadingType> readingTypes);

    EstimationRuleBuilder withProperties(Map<String, Object> properties);

    EstimationRuleBuilder withEstimationComment(ReadingQualityComment estimationComment);

    EstimationRuleBuilder.PropertyBuilder havingProperty(String property);

    interface PropertyBuilder {
        EstimationRuleBuilder withValue(Object value);
    }

    EstimationRule create();
}
