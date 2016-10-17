package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.MetrologyConfigurationBuilder;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

import java.util.Arrays;
import java.util.List;

public enum MetrologyConfigurationTpl implements Template<UsagePointMetrologyConfiguration, MetrologyConfigurationBuilder> {
    CONSUMER("C&I 3-phased consumer with smart meter with 2 ToU", "C&I 3-phased consumer with smart meter 2 ToU", ServiceKind.ELECTRICITY,
            Arrays.<String>asList(RegisterTypeTpl.SECONDARY_BULK_A_PLUS.getMrid(),
                    RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1.getMrid(),
                    RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2.getMrid())),
    PROSUMER("Residential prosumer with 1 meter", "Typical installation for residential prosumers with smart meter", ServiceKind.ELECTRICITY,
            Arrays.<String>asList(RegisterTypeTpl.SECONDARY_BULK_A_PLUS.getMrid(),
                    RegisterTypeTpl.SECONDARY_BULK_A_MINUS.getMrid(),
                    RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1.getMrid(),
                    RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2.getMrid(),
                    RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1.getMrid(),
                    RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2.getMrid()));

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
