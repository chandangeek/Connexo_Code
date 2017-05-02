/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

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
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeValueFactory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointValueFactory;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.validators.impl.Utils.INSTANT_2016_FEB_01;
import static com.elster.jupiter.validators.impl.Utils.INSTANT_2016_FEB_05;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
abstract public class ReferenceValidatorTest {

    Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    protected PropertySpecService propertySpecService = new PropertySpecServiceImpl();

    @Mock
    MetrologyPurpose VALIDATING_PURPOSE, REFERENCE_PURPOSE;

    @Mock
    UsagePoint VALIDATING_USAGE_POINT, REFERENCE_USAGE_POINT;

    @Mock
    ReadingType VALIDATING_READING_TYPE, REFERENCE_READING_TYPE_COMPARABLE, REFERENCE_READING_TYPE_NOT_COMPARABLE;

    @Before
    public void mockBefore() {
        when(VALIDATING_USAGE_POINT.getName()).thenReturn("Validating usage point");
        when(REFERENCE_USAGE_POINT.getName()).thenReturn("Reference usage point");

        when(VALIDATING_PURPOSE.getName()).thenReturn("Purpose 1");
        when(REFERENCE_PURPOSE.getName()).thenReturn("Purpose 2");

        when(VALIDATING_READING_TYPE.getMRID()).thenReturn("11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        when(VALIDATING_READING_TYPE.getFullAliasName()).thenReturn("[Daily] Secondary Delta A+ (kWh)");
        mockReadingType(VALIDATING_READING_TYPE, MacroPeriod.DAILY, Accumulation.DELTADELTA, Commodity.ELECTRICITY_SECONDARY_METERED, MetricMultiplier.KILO);


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

    ReferenceComparisonValidator initValidator(ValidationConfiguration validationConfiguration) {
        ReferenceComparisonValidator validator = new ReferenceComparisonValidator(thesaurus, propertySpecService, validationConfiguration.metrologyConfigurationService, validationConfiguration.validationService, validationConfiguration.meteringService, validationConfiguration.rule
                .createProperties());
        validator.init(validationConfiguration.validatingChannel, validationConfiguration.rule.validatingReadingType, validationConfiguration.range);
        return validator;
    }

    class ReferenceValidatorRule extends ValidatorRule {

        MetrologyPurpose referencePurpose;
        UsagePointValueFactory.UsagePointReference referenceUsagePoint;
        ReadingTypeValueFactory.ReadingTypeReference referenceReadingType;
        ReadingType validatingReadingType;

        boolean fullyConfigured = true;
        boolean changedReferenceConfiguration = false;

        @Override
        ReferenceValidatorRule withCheckPurpose(MetrologyPurpose checkPurpose) {
            super.withCheckPurpose(checkPurpose);
            return this;
        }

        @Override
        ReferenceValidatorRule withNotExistingCheckPurpose(MetrologyPurpose notExistingCheckPurpose) {
            super.withNotExistingCheckPurpose(notExistingCheckPurpose);
            return this;
        }

        @Override
        ReferenceValidatorRule withNotExistingCheckChannel() {
            super.withNotExistingCheckChannel();
            return this;
        }

        @Override
        ReferenceValidatorRule withValuedDifference(BigDecimal value) {
            super.withValuedDifference(value);
            return this;
        }

        @Override
        ReferenceValidatorRule withPercentDifference(Double percent) {
            super.withPercentDifference(percent);
            return this;
        }

        @Override
        ReferenceValidatorRule passIfNoRefData(boolean passIfNoData) {
            super.passIfNoRefData(passIfNoData);
            return this;
        }

        @Override
        ReferenceValidatorRule useValidatedData(boolean useValidatedData) {
            super.useValidatedData(useValidatedData);
            return this;
        }

        @Override
        ReferenceValidatorRule withNoMinThreshold() {
            super.withNoMinThreshold();
            return this;
        }

        @Override
        ReferenceValidatorRule withMinThreshold(BigDecimal minThreshold) {
            super.withMinThreshold(minThreshold);
            return this;
        }

        ReferenceValidatorRule notFullyConfigured() {
            fullyConfigured = false;
            referencePurpose = null;
            referenceReadingType = null;
            referenceUsagePoint = null;
            return this;
        }

        ReferenceValidatorRule withReferencePurpose(MetrologyPurpose referencePurpose) {
            this.referencePurpose = referencePurpose;
            return this;
        }

        ReferenceValidatorRule withChangedReferenceConfiguration() {
            changedReferenceConfiguration = true;
            return this;
        }

        ReferenceValidatorRule withReferenceUsagePoint(UsagePoint referenceUsagePoint) {
            this.referenceUsagePoint = new UsagePointValueFactory.UsagePointReference(referenceUsagePoint);
            return this;
        }

        ReferenceValidatorRule withReferenceReadingType(ReadingType referenceReadingType) {
            this.referenceReadingType = new ReadingTypeValueFactory.ReadingTypeReference(referenceReadingType);
            return this;
        }

        ReferenceValidatorRule withValidatingReadingType(ReadingType validatingReadingType) {
            this.validatingReadingType = validatingReadingType;
            return this;
        }

        @Override
        Map<String, Object> createProperties() {
            ImmutableMap.Builder builder = ImmutableMap.<String, Object>builder()
                    .put(ReferenceComparisonValidator.MAX_ABSOLUTE_DIFF, twoValuesDifference)
                    .put(ReferenceComparisonValidator.MIN_THRESHOLD, minThreshold)
                    .put(ReferenceComparisonValidator.PASS_IF_NO_REF_DATA, passIfNoData)
                    .put(ReferenceComparisonValidator.USE_VALIDATED_DATA, useValidatedData);
            if (fullyConfigured) {
                builder.put(ReferenceComparisonValidator.CHECK_PURPOSE, referencePurpose);
                builder.put(ReferenceComparisonValidator.CHECK_USAGE_POINT, referenceUsagePoint);
                builder.put(ReferenceComparisonValidator.CHECK_READING_TYPE, referenceReadingType);
            }
            return builder.build();
        }
    }

    class ValidationConfiguration {

        MetrologyPurpose validatingMetrologyPurpose;
        ChannelReadings validatingChannelReadings;
        ValidatedChannelReadings referenceChannelReadings;

        ValidationService validationService;
        MetrologyConfigurationService metrologyConfigurationService;
        MeteringService meteringService;
        ReferenceValidatorRule rule;

        Channel validatingChannel;
        Range<Instant> range;

        ValidationConfiguration(ReferenceValidatorRule rule, ChannelReadings validatingChannelReadings, ValidatedChannelReadings referenceChannelReadings) {
            this.rule = rule;
            this.validatingChannelReadings = validatingChannelReadings;
            this.referenceChannelReadings = referenceChannelReadings;
            mockAll();
        }

        void mockAll() {

            range = Range.closed(INSTANT_2016_FEB_01, INSTANT_2016_FEB_05);

            validatingChannel = validatingChannelReadings.mockChannel(range);

            MetrologyContractChannelsContainer validatingChannelsContainer = mock(MetrologyContractChannelsContainer.class);
            MetrologyContract validatingMetrologyContract = mock(MetrologyContract.class);
            validatingMetrologyPurpose = VALIDATING_PURPOSE;
            when(validatingMetrologyContract.getMetrologyPurpose()).thenReturn(validatingMetrologyPurpose);
            when(validatingChannelsContainer.getMetrologyContract()).thenReturn(validatingMetrologyContract);
            when(validatingChannel.getChannelsContainer()).thenReturn(validatingChannelsContainer);
            when(validatingChannelsContainer.getUsagePoint()).thenReturn(Optional.of(VALIDATING_USAGE_POINT));

            if (rule.referenceUsagePoint != null) {
                UsagePoint referenceUsagePoint = rule.referenceUsagePoint.getUsagePoint();

                MetrologyContract referenceMetrologyContract = mock(MetrologyContract.class);
                when(referenceMetrologyContract.getMetrologyPurpose()).thenReturn(rule.changedReferenceConfiguration ? mock(MetrologyPurpose.class) : rule.referencePurpose);
                UsagePointMetrologyConfiguration referenceMetrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
                when(referenceMetrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(referenceMetrologyContract));
                EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnReferenceUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
                when(referenceUsagePoint.getEffectiveMetrologyConfigurations(range)).thenReturn(Collections.singletonList(effectiveMetrologyConfigurationOnReferenceUsagePoint));
                when(effectiveMetrologyConfigurationOnReferenceUsagePoint.getMetrologyConfiguration()).thenReturn(referenceMetrologyConfiguration);
            }
        }
    }
}
