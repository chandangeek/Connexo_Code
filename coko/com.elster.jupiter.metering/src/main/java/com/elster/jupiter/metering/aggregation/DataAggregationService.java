/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.aggregation;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;

/**
 * Provides data aggregation services that are currently
 * designed for {@link UsagePoint}s. Support for
 * service delivery point and zone will be introduced.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (09:31)
 */
@ProviderType
public interface DataAggregationService {

    /**
     * Calculates the measurement data of the specified {@link MetrologyContract}
     * using the measurement data that is provided by the meters that have been
     * activated on the specified {@link UsagePoint}.<br>
     * Note that the requested period is clipped to the period during which
     * the MetrologyContract was active on the UsagePoint and will throw
     * a {@link MetrologyContractDoesNotApplyToUsagePointException}
     * when the MetrologyContract does not apply to the UsagePoint.
     *
     * @param usagePoint The UsagePoint
     * @param contract The MetrologyContract
     * @param period The period in time that should be taken into consideration
     * @return The CalculatedMetrologyContractData
     */
    CalculatedMetrologyContractData calculate(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period);

    /**
     * Introspects the calculation of the measurement data of the specified {@link MetrologyContract}
     * by returning the {@link com.elster.jupiter.metering.Channel}s that contain the measurement data
     * that is provided by the meters that have been activated on the specified {@link UsagePoint}.<br>
     * Note that the requested period is clipped to the period during which
     * the MetrologyContract was active on the UsagePoint and will throw
     * a {@link MetrologyContractDoesNotApplyToUsagePointException}
     * when the MetrologyContract does not apply to the UsagePoint.
     *
     * @param usagePoint The UsagePoint
     * @param contract The MetrologyContract
     * @param period The period in time that should be taken into consideration
     * @return The MetrologyContractIntrospector
     */
    MetrologyContractCalculationIntrospector introspect(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period);

}