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

        DataQualityOverviewBuilder having(Collection<ReadingQualityType> readingQualityTypes);

        DataQualityOverviewBuilder havingSuspectsBy(Collection<Validator> validators);

        DataQualityOverviewBuilder havingEstimatesBy(Collection<Estimator> estimators);

        MetricSpecificationBuilder withSuspectsAmount();

        MetricSpecificationBuilder withConfirmedAmount();

        MetricSpecificationBuilder withEstimatesAmount();

        MetricSpecificationBuilder withInformativesAmount();

        MetricSpecificationBuilder withEditedAmount();

        MetricSpecificationBuilder withProjectedAmount();

        DataQualityOverviews paged(int from, int to);
    }

    enum ReadingQualityType {
        SUSPECTS,
        INFORMATIVES,
        ESTIMATES,
        EDITED,
        CONFIRMED,
        PROJECTED
    }

    @ProviderType
    interface MetricSpecificationBuilder {

        DataQualityOverviewBuilder equalTo(long value);

        DataQualityOverviewBuilder inRange(Range<Long> range);
    }
}
