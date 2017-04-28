/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-23 (11:42)
 */
public class MeterHasUnsatisfiedRequirements extends LocalizedException {
    private final Map<UsagePointMetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements;

    public MeterHasUnsatisfiedRequirements(Thesaurus thesaurus, MessageSeed messageSeed, Map<UsagePointMetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements) {
        super(thesaurus, messageSeed, descriptions(unsatisfiedRequirements));
        this.unsatisfiedRequirements = Collections.unmodifiableMap(unsatisfiedRequirements);
    }

    private static String descriptions(Map<UsagePointMetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements) {
        return unsatisfiedRequirements
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(ReadingTypeRequirement::getDescription)
                .collect(Collectors.joining(", "));
    }

    public Map<UsagePointMetrologyConfiguration, List<ReadingTypeRequirement>> getUnsatisfiedRequirements() {
        return this.unsatisfiedRequirements; // already unmodifiable
    }
}