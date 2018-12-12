/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UnsatisfiedReadingTypeRequirementsOfUsagePointException extends LocalizedException {

    private final Map<UsagePointMetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements;

    public UnsatisfiedReadingTypeRequirementsOfUsagePointException(Thesaurus thesaurus, Map<UsagePointMetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements) {
        super(thesaurus, MessageSeeds.UNSATISFIED_READING_TYPE_REQUIREMENTS_OF_USAGE_POINT, buildRequirementsString(unsatisfiedRequirements));
        this.unsatisfiedRequirements = unsatisfiedRequirements;
    }

    public Map<UsagePointMetrologyConfiguration, List<ReadingTypeRequirement>> getUnsatisfiedRequirements() {
        return unsatisfiedRequirements;
    }

    public static String buildRequirementsString(Map<UsagePointMetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements) {
        return unsatisfiedRequirements.entrySet().stream()
                .map(entry -> buildDetailsForMetrologyConfiguration(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));
    }

    private static String buildDetailsForMetrologyConfiguration(UsagePointMetrologyConfiguration metrologyConfiguration, List<ReadingTypeRequirement> requirements) {
        StringBuilder builder = new StringBuilder();
        builder.append("'").append(metrologyConfiguration.getName()).append("' (");
        builder.append(requirements.stream()
                .filter(readingTypeRequirement -> readingTypeRequirement instanceof FullySpecifiedReadingTypeRequirement)
                .map(FullySpecifiedReadingTypeRequirement.class::cast)//in MultiSense only fully specified requirements are expected
                .map(FullySpecifiedReadingTypeRequirement::getReadingType)
                .map(ReadingType::getName).collect(Collectors.joining(", ")));
        builder.append(")");
        return builder.toString();
    }
}
