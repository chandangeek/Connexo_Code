/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnsatisfiedReadingTypeRequirements extends LocalizedException {
    private Map<MeterRole, List<ReadingTypeRequirement>> unsatisfiedRequirements;

    @Deprecated
    public UnsatisfiedReadingTypeRequirements(Thesaurus thesaurus, UsagePointMetrologyConfiguration metrologyConfiguration) {
        super(thesaurus, MessageSeeds.UNSATISFIED_READING_TYPE_REQUIREMENTS);
    }

    public UnsatisfiedReadingTypeRequirements(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.UNSATISFIED_READING_TYPE_REQUIREMENTS);
    }

    public UnsatisfiedReadingTypeRequirements addUnsatisfiedReadingTypeRequirements(MeterRole meterRole, List<ReadingTypeRequirement> readingTypeRequirements) {
        if (unsatisfiedRequirements == null) {
            unsatisfiedRequirements = new HashMap<>();
        }
        this.unsatisfiedRequirements.put(meterRole, readingTypeRequirements);
        return this;
    }

    public Map<MeterRole, List<ReadingTypeRequirement>> getFailedRequirements() {
        return Collections.unmodifiableMap(unsatisfiedRequirements);
    }
}