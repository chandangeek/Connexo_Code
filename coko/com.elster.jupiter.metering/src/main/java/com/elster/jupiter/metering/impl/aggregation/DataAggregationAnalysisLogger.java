/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Isolates logging of data and/or operations as part of data aggregation calculation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-08-19 (13:02)
 */
class DataAggregationAnalysisLogger {

    String calculationStarted(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period) {
        return "Start: calculate aggregated data for usagePoint(name=" + usagePoint.getName() + "), contract(id=" + contract.getId() + ") and period " + period;
    }

    String verboseEffectiveMetrologyConfigurations(List<EffectiveMetrologyConfigurationOnUsagePoint> effectivities) {
        String configurationAndPurposes = effectivities
                .stream()
                .map(this::verboseConfigurationAndContractPurposes)
                .collect(Collectors.joining("\n"));
        return "The following configurations and contracts were found to apply to the requested period\n" + configurationAndPurposes;
    }

    private String verboseConfigurationAndContractPurposes(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint) {
        return this.verboseConfigurationAndContractPurposes(
                effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration(),
                effectiveMetrologyConfigurationOnUsagePoint.getRange());
    }

    private String verboseConfigurationAndContractPurposes(UsagePointMetrologyConfiguration configuration, Range<Instant> effective) {
        String contractPurposes =
                configuration
                        .getContracts()
                        .stream()
                        .map(MetrologyContract::getMetrologyPurpose)
                        .map(MetrologyPurpose::getName)
                        .collect(Collectors.joining(", "));
        return configuration.getName() + " with purposes: " + contractPurposes;
    }

    String meterActivationSetCreated(MeterActivationSetImpl set) {
        String rolesAndRanges = set.getMeterActivations().stream().map(this::verboseMeterActivation).collect(Collectors.joining("\n"));
        return "Created meter activation set: " + set.getRange() + "\n" + rolesAndRanges;
    }

    private String verboseMeterActivation(MeterActivation meterActivation) {
        return meterActivation.getMeterRole().map(MeterRole::getDisplayName).orElse("") + ": " + meterActivation.getRange();
    }

}
