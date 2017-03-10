/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl.calc;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.dataquality.impl.UsagePointDataQualityKpiImpl;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.util.streams.Functions;

import com.google.common.collect.Range;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

class UsagePointDataQualityKpiCalculator extends AbstractDataQualityKpiCalculator {

    private final UsagePointGroup usagePointGroup;
    private final MetrologyPurpose metrologyPurpose;

    UsagePointDataQualityKpiCalculator(DataQualityServiceProvider serviceProvider, UsagePointDataQualityKpiImpl dataQualityKpi, Logger logger) {
        super(serviceProvider, dataQualityKpi, logger);
        this.usagePointGroup = dataQualityKpi.getUsagePointGroup();
        this.metrologyPurpose = dataQualityKpi.getMetrologyPurpose();
    }

    @Override
    QualityCodeSystem getQualityCodeSystem() {
        return QualityCodeSystem.MDM;
    }

    @Override
    DataQualityKpiSqlBuilder sqlBuilder() {
        return new UsagePointDataQualityKpiSqlBuilder(usagePointGroup, metrologyPurpose);
    }

    @Override
    public void calculateAndStore() {
        super.calculateInTransaction();
        this.usagePointGroup
                .getMembers(getClock().instant())
                .forEach(this::storeInTransaction);
    }

    private void storeInTransaction(UsagePoint usagePoint) {
        try {
            getTransactionService().run(() -> store(usagePoint));
        } catch (Exception ex) {
            getTransactionService().run(() -> getLogger().log(Level.WARNING, "Failed to store data quality KPI data for usage point " + usagePoint.getName()
                    + ". Error: " + ex.getLocalizedMessage(), ex));
        }
    }

    private void store(UsagePoint usagePoint) {
        usagePoint.getEffectiveMetrologyConfigurations(Range.openClosed(super.getStart(), super.getEnd()))
                .stream()
                .map(effectiveMC -> getChannelsContainerForPurpose(effectiveMC, this.metrologyPurpose))
                .flatMap(Functions.asStream())
                .forEach(channelsContainer ->
                        super.storeForChannels(
                                usagePoint.getId(),
                                UsagePointDataQualityKpiImpl.kpiMemberNameSuffix(usagePoint, channelsContainer),
                                channelsContainer.getChannels()));
    }

    private Optional<ChannelsContainer> getChannelsContainerForPurpose(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC, MetrologyPurpose purpose) {
        return effectiveMC
                .getMetrologyConfiguration()
                .getContracts()
                .stream()
                .filter(metrologyContract -> metrologyContract.getMetrologyPurpose().equals(purpose))
                .findAny()
                .flatMap(effectiveMC::getChannelsContainer);
    }
}
