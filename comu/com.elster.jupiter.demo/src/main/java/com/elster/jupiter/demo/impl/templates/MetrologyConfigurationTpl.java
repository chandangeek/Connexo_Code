package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.MetrologyConfigurationBuilder;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

import java.util.Arrays;
import java.util.List;

public enum MetrologyConfigurationTpl implements Template<UsagePointMetrologyConfiguration, MetrologyConfigurationBuilder> {
    CONSUMER("Residential net metering (consumption)", "Residential consumer", ServiceKind.ELECTRICITY,
            Arrays.asList("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0",
                    "11.0.0.4.1.1.12.0.0.0.0.1.0.0.0.0.72.0",
                    "13.0.0.4.1.1.12.0.0.0.0.1.0.0.0.0.72.0")),
    PROSUMER("Residential prosumer with 1 meter", "Typical installation for residential prosumers with smart meter", ServiceKind.ELECTRICITY,
            Arrays.asList("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0",
                    "11.0.0.4.1.1.12.0.0.0.0.1.0.0.0.0.72.0",
                    "13.0.0.4.1.1.12.0.0.0.0.1.0.0.0.0.72.0",
                    "0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0",
                    "11.0.0.4.19.1.12.0.0.0.0.1.0.0.0.0.72.0",
                    "13.0.0.4.19.1.12.0.0.0.0.1.0.0.0.0.72.0"));

    private String name;
    private String description;
    private ServiceKind serviceKind;
    private List<String> readingTypes;

    MetrologyConfigurationTpl(String name,
                              String description,
                              ServiceKind serviceKind,
                              List<String> readingTypes) {
        this.name = name;
        this.description = description;
        this.serviceKind = serviceKind;
        this.readingTypes = readingTypes;
    }

    @Override
    public Class<MetrologyConfigurationBuilder> getBuilderClass() {
        return MetrologyConfigurationBuilder.class;
    }

    @Override
    public MetrologyConfigurationBuilder get(MetrologyConfigurationBuilder builder) {
        builder.withName(this.name)
                .withDescription(this.description)
                .withServiceKind(this.serviceKind)
                .withReadingTypes(this.readingTypes);

        return builder;
    }

    public String getName() {
        return name;
    }
}
