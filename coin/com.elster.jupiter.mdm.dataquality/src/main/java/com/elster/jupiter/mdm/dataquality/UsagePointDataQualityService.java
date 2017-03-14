/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality;

import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.validation.Validator;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collection;

@ProviderType
public interface UsagePointDataQualityService {

    String COMPONENT_NAME = "UDQ";

    /**
     * Starts the building process to obtain {@link DataQualityOverview}.
     */
    DataQualityOverviewBuilder forAllUsagePoints();

    @ProviderType
    interface DataQualityOverviewBuilder {

        DataQualityOverviewBuilder in(Collection<UsagePointGroup> usagePointGroups);

        DataQualityOverviewBuilder of(Collection<MetrologyConfiguration> metrologyConfigurations);

        DataQualityOverviewBuilder with(Collection<MetrologyPurpose> purposes);

        DataQualityOverviewBuilder in(Range<Instant> range);

        DataQualityOverviewBuilder havingSuspects();

        DataQualityOverviewBuilder suspectedBy(Collection<Validator> validators);

        DataQualityOverviewBuilder havingEstimates();

        DataQualityOverviewBuilder estimatedBy(Collection<Estimator> estimators);

        DataQualityOverviewBuilder havingEdited();

        DataQualityOverviewBuilder havingConfirmed();

        DataQualityOverviewBuilder havingInformatives();

        MetricSpecificationBuilder withSuspectsAmount();

        MetricSpecificationBuilder withConfirmedAmount();

        MetricSpecificationBuilder withEstimatesAmount();

        MetricSpecificationBuilder withInformativesAmount();

        MetricSpecificationBuilder withEditedAmount();

        DataQualityOverviews paged(int from, int to);
    }

    @ProviderType
    interface MetricSpecificationBuilder {

        DataQualityOverviewBuilder equalTo(long value);

        DataQualityOverviewBuilder inRange(Range<Long> range);
    }
}
