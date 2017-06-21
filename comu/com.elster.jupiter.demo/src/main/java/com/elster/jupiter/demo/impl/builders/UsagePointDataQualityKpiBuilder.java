/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.UsagePointDataQualityKpi;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.UsagePointGroup;

import javax.inject.Inject;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

public class UsagePointDataQualityKpiBuilder implements Builder<UsagePointDataQualityKpi> {
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final DataQualityKpiService dataQualityKpiService;

    private UsagePointGroup usagePointGroup;
    private MetrologyPurpose purpose;
    private TemporalAmount frequency;

    @Inject
    public UsagePointDataQualityKpiBuilder(MetrologyConfigurationService metrologyConfigurationService,
                                           DataQualityKpiService dataQualityKpiService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.dataQualityKpiService = dataQualityKpiService;
    }

    public UsagePointDataQualityKpiBuilder withUsagePointGroup(UsagePointGroup usagePointGroup) {
        this.usagePointGroup = usagePointGroup;
        return this;
    }

    public UsagePointDataQualityKpiBuilder withMetrologyPurpose(DefaultMetrologyPurpose purpose) {
        this.purpose = metrologyConfigurationService.findMetrologyPurpose(purpose)
                .orElseThrow(() -> new UnableToCreate("Unable to find metrology purpose '" + purpose.getName() + "'."));
        return this;
    }

    public UsagePointDataQualityKpiBuilder withFrequency(TemporalAmount frequency) {
        this.frequency = frequency;
        return this;
    }

    @Override
    public Optional<UsagePointDataQualityKpi> find() {
        return dataQualityKpiService.usagePointDataQualityKpiFinder()
                .forGroup(usagePointGroup)
                .find().stream().findFirst();
    }

    @Override
    public UsagePointDataQualityKpi create() {
        return dataQualityKpiService.newDataQualityKpi(usagePointGroup, purpose, frequency);
    }
}
