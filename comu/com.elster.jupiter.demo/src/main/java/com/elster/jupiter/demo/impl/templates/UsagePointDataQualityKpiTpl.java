/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.dataquality.UsagePointDataQualityKpi;
import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.UsagePointDataQualityKpiBuilder;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;

import java.time.Duration;
import java.time.temporal.TemporalAmount;

public enum UsagePointDataQualityKpiTpl implements Template<UsagePointDataQualityKpi, UsagePointDataQualityKpiBuilder> {

    RESIDENTIAL_ELECTRICITY(UsagePointGroupTpl.RESIDENTIAL_ELECTRICITY, Duration.ofHours(1L)),
    RESIDENTIAL_GAS(UsagePointGroupTpl.RESIDENTIAL_GAS, Duration.ofHours(1L)),
    RESIDENTIAL_WATER(UsagePointGroupTpl.RESIDENTIAL_WATER, Duration.ofHours(1L));

    private final UsagePointGroupTpl usagePointGroupTpl;
    private final TemporalAmount frequency;

    UsagePointDataQualityKpiTpl(UsagePointGroupTpl usagePointGroupTpl, TemporalAmount frequency) {
        this.usagePointGroupTpl = usagePointGroupTpl;
        this.frequency = frequency;
    }

    @Override
    public Class<UsagePointDataQualityKpiBuilder> getBuilderClass() {
        return UsagePointDataQualityKpiBuilder.class;
    }

    @Override
    public UsagePointDataQualityKpiBuilder get(UsagePointDataQualityKpiBuilder builder) {
        return builder.withUsagePointGroup(Builders.from(this.usagePointGroupTpl).get())
                .withMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                .withFrequency(this.frequency);
    }
}
