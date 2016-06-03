package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnsatisfiedReadingTypeRequirements extends LocalizedException {
    private UsagePointMetrologyConfiguration metrologyConfiguration;
    private Map<MeterRole, List<ReadingTypeRequirement>> failedRequirementsMap;

    public UnsatisfiedReadingTypeRequirements(Thesaurus thesaurus,
                                              UsagePointMetrologyConfiguration metrologyConfiguration) {

        super(thesaurus, MessageSeeds.UNSATISFIED_READING_TYPE_REQUIREMENTS);
        this.metrologyConfiguration = metrologyConfiguration;
    }

    public UnsatisfiedReadingTypeRequirements addUnsatisfiedReadingTypeRequirements(MeterRole meterRole, List<ReadingTypeRequirement> readingTypeRequirements) {
        if (failedRequirementsMap == null) {
            failedRequirementsMap = new HashMap<>();
        }
        this.failedRequirementsMap.put(meterRole, readingTypeRequirements);
        return this;
    }

    public UsagePointMetrologyConfiguration getMetrologyConfiguration() {
        return metrologyConfiguration;
    }

    public Map<MeterRole, List<ReadingTypeRequirement>> getFailedRequirements() {
        return Collections.unmodifiableMap(failedRequirementsMap);
    }
}