/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeValueFactory;
import com.elster.jupiter.metering.UsagePointValueFactory;
import com.elster.jupiter.nls.NlsMessageFormat;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.logging.Logger;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class ReferenceEstimatorTest extends MainCheckEstimatorTest {

    @Mock
    private static ReadingType REFERENCE_READING_TYPE_COMPARABLE, REFERENCE_READING_TYPE_NOT_COMPARABLE;

    static BigDecimal COMPARABLE_READING_TYPE_MULTIPIER = BigDecimal.valueOf(0.001D);

    ReferenceSubstitutionEstimator mockEstimator(ReferenceEstimationConfiguration estimationConfiguration) {
        ReferenceSubstitutionEstimator estimator = new ReferenceSubstitutionEstimator(estimationConfiguration.thesaurus, estimationConfiguration.metrologyConfigurationService, estimationConfiguration.validationService, estimationConfiguration.propertySpecService, estimationConfiguration.meteringService, estimationConfiguration.properties);
        estimator.init(estimationConfiguration.logger == null ? LOGGER : estimationConfiguration.logger);
        return estimator;
    }

    class ReferenceEstimationConfiguration extends EstimationConfiguration {

        boolean referenceReadingTypeComparable = true;
        boolean fullyConfigured = true;

        MeteringService meteringService;

        ReferenceEstimationConfiguration withLogger(Logger logger) {
            super.withLogger(logger);
            return this;
        }

        ReferenceEstimationConfiguration withBlock(BlockConfiguration blockConfiguration) {
            super.withBlock(blockConfiguration);
            return this;
        }

        ReferenceEstimationConfiguration withNotAvailablePurpose() {
            super.withNotAvailablePurpose();
            return this;
        }

        ReferenceEstimationConfiguration notFullyConfigured() {
            fullyConfigured = false;
            return this;
        }

        ReferenceEstimationConfiguration withNotComparableReferenceReadingType() {
            referenceReadingTypeComparable = false;
            return this;
        }

        @Override
        void mockAll() {
            super.mockAll();

            NlsMessageFormat nameFormat = mock(NlsMessageFormat.class);
            when(nameFormat.format()).thenReturn(ReferenceSubstitutionEstimator.TranslationKeys.ESTIMATOR_NAME.getDefaultFormat());
            when(thesaurus.getFormat(ReferenceSubstitutionEstimator.TranslationKeys.ESTIMATOR_NAME)).thenReturn(nameFormat);

            mockReadingType(readingType, MacroPeriod.DAILY, Accumulation.DELTADELTA, Commodity.ELECTRICITY_SECONDARY_METERED, MetricMultiplier.KILO);

            when(REFERENCE_READING_TYPE_COMPARABLE.getMRID()).thenReturn("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.6.72.0");
            when(REFERENCE_READING_TYPE_COMPARABLE.getFullAliasName()).thenReturn("[Daily] Secondary Delta A+ (mWh)");
            mockReadingType(REFERENCE_READING_TYPE_COMPARABLE, MacroPeriod.DAILY, Accumulation.DELTADELTA, Commodity.ELECTRICITY_SECONDARY_METERED, MetricMultiplier.MEGA);

            when(REFERENCE_READING_TYPE_NOT_COMPARABLE.getMRID()).thenReturn("11.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
            when(REFERENCE_READING_TYPE_NOT_COMPARABLE.getFullAliasName()).thenReturn("[Daily] Secondary Bulk A+ (kWh)");
            mockReadingType(REFERENCE_READING_TYPE_NOT_COMPARABLE, MacroPeriod.DAILY, Accumulation.BULKQUANTITY, Commodity.ELECTRICITY_SECONDARY_METERED, MetricMultiplier.KILO);
        }

        private void mockReadingType(ReadingType readingType, MacroPeriod macroPeriod, Accumulation accumulation, Commodity commodity, MetricMultiplier multiplier) {
            when(readingType.getMacroPeriod()).thenReturn(macroPeriod);
            when(readingType.getAggregate()).thenReturn(Aggregate.NOTAPPLICABLE);
            when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
            when(readingType.getAccumulation()).thenReturn(accumulation);
            when(readingType.getFlowDirection()).thenReturn(FlowDirection.FORWARD);
            when(readingType.getCommodity()).thenReturn(commodity);
            when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
            when(readingType.getInterharmonic()).thenReturn(new RationalNumber(0, 1));
            when(readingType.getArgument()).thenReturn(new RationalNumber(0, 1));
            when(readingType.getTou()).thenReturn(0);
            when(readingType.getCpp()).thenReturn(0);
            when(readingType.getConsumptionTier()).thenReturn(0);
            when(readingType.getPhases()).thenReturn(Phase.NOTAPPLICABLE);
            when(readingType.getMultiplier()).thenReturn(multiplier);
            when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
            when(readingType.getCurrency()).thenReturn(Currency.getInstance("USD"));
            when(readingType.isRegular()).thenReturn(true);
        }

        @Override
        ReadingType getCheckReadingType() {
            return referenceReadingTypeComparable ? REFERENCE_READING_TYPE_COMPARABLE : REFERENCE_READING_TYPE_NOT_COMPARABLE;
        }

        @Override
        void mockProperties() {
            properties = new HashMap<String, Object>() {{
                if (fullyConfigured) {
                    put(ReferenceSubstitutionEstimator.CHECK_PURPOSE, notAvailablePurpose ? NOT_EXISTING_PURPOSE : PURPOSE);
                    put(ReferenceSubstitutionEstimator.CHECK_READING_TYPE, new ReadingTypeValueFactory.ReadingTypeReference(referenceReadingTypeComparable ? REFERENCE_READING_TYPE_COMPARABLE : REFERENCE_READING_TYPE_NOT_COMPARABLE));
                    put(ReferenceSubstitutionEstimator.CHECK_USAGE_POINT, new UsagePointValueFactory.UsagePointReference(usagePoint));
                }
            }};
        }
    }
}
