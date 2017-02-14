/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class MetrologyConfigurationBuilder extends NamedBuilder<UsagePointMetrologyConfiguration, MetrologyConfigurationBuilder> {
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final MeteringService meteringService;
    private String description;
    private ServiceKind serviceKind;
    private List<String> readingTypes;

    @Inject
    public MetrologyConfigurationBuilder(MeteringService meteringService,
                                         MetrologyConfigurationService metrologyConfigurationService) {
        super(MetrologyConfigurationBuilder.class);
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    public MetrologyConfigurationBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public MetrologyConfigurationBuilder withServiceKind(ServiceKind serviceKind) {
        this.serviceKind = serviceKind;
        return this;
    }

    public MetrologyConfigurationBuilder withReadingTypes(List<String> readingTypes) {
        this.readingTypes = readingTypes;
        return this;
    }

    @Override
    public Optional<UsagePointMetrologyConfiguration> find() {
        return metrologyConfigurationService.findMetrologyConfiguration(this.getName())
                .filter(metrologyConfiguration -> metrologyConfiguration instanceof UsagePointMetrologyConfiguration)
                .map(UsagePointMetrologyConfiguration.class::cast);
    }

    @Override
    public UsagePointMetrologyConfiguration create() {
        Log.write(this);

        if (this.serviceKind == null) {
            throw new UnableToCreate("You must specify a service kind");
        }

        ServiceCategory serviceCategory = meteringService.getServiceCategory(this.serviceKind)
                .orElseThrow(() -> new UnableToCreate("Service category not found"));

        UsagePointMetrologyConfiguration metrologyConfiguration = metrologyConfigurationService.newUsagePointMetrologyConfiguration(this.getName(), serviceCategory)
                .withDescription(this.description)
                .create();

        setReadingTypes(metrologyConfiguration, meteringService.findReadingTypes(this.readingTypes));
        metrologyConfiguration.activate();

        return metrologyConfiguration;
    }

    private void setReadingTypes(UsagePointMetrologyConfiguration metrologyConfiguration, List<ReadingType> readingTypes) {
        MetrologyPurpose purpose = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                .orElseThrow(() -> new UnableToCreate("Default metrology purpose not found"));
        MeterRole meterRoleDefault = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new UnableToCreate("Default meter role not found"));

        metrologyConfiguration.addMeterRole(meterRoleDefault);

        readingTypes.stream().forEach(readingType -> {
            FullySpecifiedReadingTypeRequirement fullySpecifiedReadingTypeRequirement =
                    metrologyConfiguration
                            .newReadingTypeRequirement(readingType.getFullAliasName(), meterRoleDefault)
                            .withReadingType(readingType);
            ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable(readingType.getFullAliasName(), readingType, Formula.Mode.AUTO);
            ReadingTypeDeliverable deliverable = builder.build(builder.requirement(fullySpecifiedReadingTypeRequirement));
            MetrologyContract metrologyContract = metrologyConfiguration.addMandatoryMetrologyContract(purpose);
            metrologyContract.addDeliverable(deliverable);
        });
    }
}
