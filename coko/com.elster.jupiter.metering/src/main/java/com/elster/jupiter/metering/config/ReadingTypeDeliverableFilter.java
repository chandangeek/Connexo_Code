/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.ReadingType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadingTypeDeliverableFilter {

    private List<ReadingType> readingTypes = Collections.emptyList();
    private List<MetrologyConfiguration> metrologyConfigurations = Collections.emptyList();
    private List<MetrologyContract> metrologyContracts = Collections.emptyList();

    public ReadingTypeDeliverableFilter withReadingTypes(ReadingType... readingTypes) {
        if (readingTypes.length > 0) {
            this.readingTypes = Stream.of(readingTypes).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return this;
    }

    public ReadingTypeDeliverableFilter withMetrologyConfigurations(MetrologyConfiguration... metrologyConfigurations) {
        if (metrologyConfigurations != null && metrologyConfigurations.length > 0) {
            this.metrologyConfigurations = Stream.of(metrologyConfigurations).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return this;
    }

    public ReadingTypeDeliverableFilter withMetrologyContracts(MetrologyContract... metrologyContracts) {
        if (metrologyContracts != null && metrologyContracts.length > 0) {
            this.metrologyContracts = Stream.of(metrologyContracts).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return this;
    }

    public List<ReadingType> getReadingTypes() {
        return this.readingTypes;
    }

    public List<MetrologyConfiguration> getMetrologyConfigurations() {
        return this.metrologyConfigurations;
    }

    public List<MetrologyContract> getMetrologyContracts() {
        return this.metrologyContracts;
    }
}
