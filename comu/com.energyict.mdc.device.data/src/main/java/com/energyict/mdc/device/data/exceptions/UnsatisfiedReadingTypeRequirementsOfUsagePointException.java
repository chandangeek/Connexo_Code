package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UnsatisfiedReadingTypeRequirementsOfUsagePointException extends LocalizedException {

    public UnsatisfiedReadingTypeRequirementsOfUsagePointException(Thesaurus thesaurus, Map<MetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements) {
        super(thesaurus, MessageSeeds.UNSATISFIED_READING_TYPE_REQUIREMENTS_OF_USAGE_POINT, buildRequirementsString(unsatisfiedRequirements));
    }

    private static String buildRequirementsString(Map<MetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements) {
        return unsatisfiedRequirements.entrySet().stream()
                .map(entry -> buildDetailsForMetrologyConfiguration(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));
    }

    private static String buildDetailsForMetrologyConfiguration(MetrologyConfiguration metrologyConfiguration, List<ReadingTypeRequirement> requirements) {
        StringBuilder builder = new StringBuilder();
        builder.append("'").append(metrologyConfiguration.getName()).append("' (");
        builder.append(requirements.stream().map(ReadingTypeRequirement::getName).collect(Collectors.joining(", ")));
        builder.append(")");
        return builder.toString();
    }
}
