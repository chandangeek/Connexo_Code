package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.ReadingType;

import java.util.Arrays;
import java.util.List;

public class ReadingTypeDeliverableFilter {

    private List<ReadingType> readingTypes;
    private List<MetrologyConfiguration> metrologyConfigurations;
    private List<MetrologyContract> metrologyContracts;

    public ReadingTypeDeliverableFilter withReadingTypes(ReadingType... readingTypes) {
        if (readingTypes != null && readingTypes.length > 0) {
            this.readingTypes = Arrays.asList(readingTypes);
        }
        return this;
    }

    public ReadingTypeDeliverableFilter withMetrologyConfigurations(MetrologyConfiguration... metrologyConfigurations) {
        if (metrologyConfigurations != null && metrologyConfigurations.length > 0) {
            this.metrologyConfigurations = Arrays.asList(metrologyConfigurations);
        }
        return this;
    }

    public ReadingTypeDeliverableFilter withMetrologyContracts(MetrologyContract... metrologyContracts) {
        if (metrologyContracts != null && metrologyContracts.length > 0) {
            this.metrologyContracts = Arrays.asList(metrologyContracts);
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
