/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl.meteradvance;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.properties.NonOrBigDecimalValueFactory;
import com.elster.jupiter.properties.NonOrBigDecimalValueProperty;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.TimeDurationValueFactory;
import com.elster.jupiter.properties.TwoValuesAbsoluteDifference;
import com.elster.jupiter.properties.TwoValuesDifferenceValueFactory;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validators.impl.MessageSeeds;
import com.elster.jupiter.validators.impl.properties.ReadingTypeReference;
import com.elster.jupiter.validators.impl.properties.ReadingTypeValueFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeterAdvanceValidatorTest {

    @Rule
    public TimeZoneNeutral murdo = Using.timeZoneOfMcMurdo();

    private static final String NAME = "UP0001";
    private static final TemporalAmount CHANNEL_INTERVAL_LENGTH = Period.ofDays(1);
    private final static BigDecimal MAX_DIFFERENCE = BigDecimal.TEN;
    private final static BigDecimal MIN_THRESHOLD = BigDecimal.TEN;

    private final ZoneId CHANNEL_ZONE_ID = ZoneId.of(this.murdo.getSubstitute());

    private final Instant START = ZonedDateTime.of(LocalDateTime.of(LocalDate.of(2017, 4, 1), LocalTime.of(0, 0)), CHANNEL_ZONE_ID).toInstant();
    private final Instant END = ZonedDateTime.of(LocalDateTime.of(LocalDate.of(2017, 4, 20), LocalTime.of(0, 0)), CHANNEL_ZONE_ID).toInstant();

    private final Range<Instant> validatedInterval = Range.openClosed(START, END);

    @Mock
    private Logger logger;
    @Mock
    private MeteringService meteringService;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private Channel channel;
    @Mock
    private Channel register;
    @Mock(extraInterfaces = HasName.class)
    private UsagePoint usagePoint;

    private ReadingType readingType;

    private ReadingType referenceReadingType;

    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;

    private PropertySpecService propertySpecService = new PropertySpecServiceImpl();

    private MeterAdvanceValidator meterAdvanceValidator;

    @Before
    public void setUp() throws Exception {
        when(channelsContainer.getMeter()).thenReturn(Optional.empty());
        when(channelsContainer.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getName()).thenReturn(NAME);
        when(channel.getChannelsContainer()).thenReturn(channelsContainer);
        when(channel.getIntervalLength()).thenReturn(Optional.of(CHANNEL_INTERVAL_LENGTH));
        when(channel.getZoneId()).thenReturn(CHANNEL_ZONE_ID);

        this.readingType = mockRegularReadingType(MacroPeriod.DAILY, Accumulation.DELTADELTA, MetricMultiplier.ZERO);
        when(readingType.getIntervalLength()).thenReturn(Optional.of(CHANNEL_INTERVAL_LENGTH));
        when(channelsContainer.getChannel(readingType)).thenReturn(Optional.of(channel));
        this.referenceReadingType = mockIrregularReadingType(Accumulation.DELTADELTA, MetricMultiplier.KILO);
        when(channelsContainer.getChannel(referenceReadingType)).thenReturn(Optional.of(register));

        Map<String, Object> properties = ImmutableMap.of(
                MeterAdvanceValidator.REFERENCE_READING_TYPE, new ReadingTypeReference(this.readingType),
                MeterAdvanceValidator.MAX_ABSOLUTE_DIFFERENCE, new TwoValuesAbsoluteDifference(MAX_DIFFERENCE),
                MeterAdvanceValidator.REFERENCE_PERIOD, new TimeDuration(10, TimeDuration.TimeUnit.DAYS),
                MeterAdvanceValidator.MIN_THRESHOLD, new NonOrBigDecimalValueProperty(MIN_THRESHOLD)
        );
        this.meterAdvanceValidator = new MeterAdvanceValidator(this.thesaurus, this.propertySpecService, this.meteringService, properties);
        field("logger").ofType(Logger.class).in(this.meterAdvanceValidator).set(this.logger);
    }

    @Test
    public void irregularChannelCanNotBeValidated() {
        ReadingType irregularReadingType = mockIrregularReadingType(Accumulation.BULKQUANTITY, MetricMultiplier.ZERO);

        // Business method
        this.meterAdvanceValidator.init(channel, irregularReadingType, Range.all());

        // Asserts
        ValidationStrategy validationStrategy = this.meterAdvanceValidator.getValidationStrategy();
        SkipValidationOption skipValidationOption = validationStrategy.getSkipValidationOption();
        assertThat(skipValidationOption).isEqualTo(SkipValidationOption.MARK_ALL_NOT_VALIDATED);

        verify(this.logger).log(Level.SEVERE, thesaurus.getFormat(MessageSeeds.UNSUPPORTED_IRREGULAR_CHANNEL).format(this.meterAdvanceValidator.getDisplayName()));
    }

    @Test
    public void channelWithUnsupportedIntervalCanNotBeValidated() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.isRegular()).thenReturn(true);
        when(readingType.getIntervalLength()).thenReturn(Optional.empty());
        when(readingType.getMRID()).thenReturn("rt");

        // Business method
        this.meterAdvanceValidator.init(channel, readingType, Range.all());

        // Asserts
        ValidationStrategy validationStrategy = this.meterAdvanceValidator.getValidationStrategy();
        SkipValidationOption skipValidationOption = validationStrategy.getSkipValidationOption();
        assertThat(skipValidationOption).isEqualTo(SkipValidationOption.MARK_ALL_NOT_VALIDATED);

        verify(this.logger).log(Level.SEVERE, thesaurus.getFormat(MessageSeeds.UNSUPPORTED_READINGTYPE).format("rt", this.meterAdvanceValidator.getDisplayName()));
    }

    @Test
    public void validatedReadingTypeIsNotCompatibleWithReferenceReadingType() {
        ReadingType readingType = mockReadingType(
                MacroPeriod.DAILY, TimeAttribute.NOTAPPLICABLE, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED, MetricMultiplier.KILO);
        when(readingType.getIntervalLength()).thenReturn(Optional.of(CHANNEL_INTERVAL_LENGTH));

        // Business method
        this.meterAdvanceValidator.init(channel, readingType, validatedInterval);

        // Asserts
        ValidationStrategy validationStrategy = this.meterAdvanceValidator.getValidationStrategy();
        SkipValidationOption skipValidationOption = validationStrategy.getSkipValidationOption();
        assertThat(skipValidationOption).isEqualTo(SkipValidationOption.MARK_ALL_NOT_VALIDATED);

        verify(this.logger).log(Level.SEVERE, thesaurus.getFormat(MessageSeeds.REFERENCE_READINGTYPE_DOES_NOT_MATCH_VALIDATED_ONE).format(getDefaultMessageSeedArgs()));
    }

    @Test
    public void getPropertySpecsForRuleLevel() {
        // Business method
        List<PropertySpec> propertySpecs = this.meterAdvanceValidator.getPropertySpecs(ValidationPropertyDefinitionLevel.VALIDATION_RULE);

        // Asserts
        assertThat(propertySpecs).hasSize(4);
        List<String> propertyNames = propertySpecs.stream().map(PropertySpec::getName).collect(Collectors.toList());
        assertThat(propertyNames).containsExactly(
                MeterAdvanceValidator.REFERENCE_READING_TYPE,
                MeterAdvanceValidator.MAX_ABSOLUTE_DIFFERENCE,
                MeterAdvanceValidator.REFERENCE_PERIOD,
                MeterAdvanceValidator.MIN_THRESHOLD
        );
    }

    @Test
    public void getPropertySpecsForChannelLevel() {
        // Business method
        List<PropertySpec> propertySpecs = this.meterAdvanceValidator.getPropertySpecs(ValidationPropertyDefinitionLevel.TARGET_OBJECT);

        // Asserts
        assertThat(propertySpecs).hasSize(1);
        List<String> propertyNames = propertySpecs.stream().map(PropertySpec::getName).collect(Collectors.toList());
        assertThat(propertyNames).containsExactly(MeterAdvanceValidator.MAX_ABSOLUTE_DIFFERENCE);
    }

    @Test
    public void getPropertySpecs() {
        // Business method
        List<PropertySpec> propertySpecs = this.meterAdvanceValidator.getPropertySpecs();

        // Asserts
        assertThat(propertySpecs).hasSize(4);

        PropertySpec propertySpec_1 = propertySpecs.get(0);
        assertThat(propertySpec_1.getName()).isEqualTo(MeterAdvanceValidator.REFERENCE_READING_TYPE);
        assertThat(propertySpec_1.getValueFactory()).isInstanceOf(ReadingTypeValueFactory.class);
        assertThat(propertySpec_1.isRequired()).isTrue();

        PropertySpec propertySpec_2 = propertySpecs.get(1);
        assertThat(propertySpec_2.getName()).isEqualTo(MeterAdvanceValidator.MAX_ABSOLUTE_DIFFERENCE);
        assertThat(propertySpec_2.getValueFactory()).isInstanceOf(TwoValuesDifferenceValueFactory.class);
        assertThat(propertySpec_2.isRequired()).isTrue();

        PropertySpec propertySpec_3 = propertySpecs.get(2);
        assertThat(propertySpec_3.getName()).isEqualTo(MeterAdvanceValidator.REFERENCE_PERIOD);
        assertThat(propertySpec_3.getValueFactory()).isInstanceOf(TimeDurationValueFactory.class);
        assertThat(propertySpec_3.isRequired()).isTrue();

        PropertySpec propertySpec_4 = propertySpecs.get(3);
        assertThat(propertySpec_4.getName()).isEqualTo(MeterAdvanceValidator.MIN_THRESHOLD);
        assertThat(propertySpec_4.getValueFactory()).isInstanceOf(NonOrBigDecimalValueFactory.class);
        assertThat(propertySpec_4.isRequired()).isTrue();
    }

    @Test
    public void getSupportedQualityCodeSystems() {
        // Business method
        Set<QualityCodeSystem> supportedQualityCodeSystems = this.meterAdvanceValidator.getSupportedQualityCodeSystems();

        // Asserts
        assertThat(supportedQualityCodeSystems).containsOnly(QualityCodeSystem.MDC, QualityCodeSystem.MDM);
    }

    private ReadingType mockRegularReadingType(MacroPeriod macroPeriod, Accumulation accumulation, MetricMultiplier multiplier) {
        return mockReadingType(macroPeriod, TimeAttribute.NOTAPPLICABLE, accumulation, Commodity.ELECTRICITY_SECONDARY_METERED, multiplier);
    }

    private ReadingType mockRegularReadingType(TimeAttribute timeAttribute, Accumulation accumulation, MetricMultiplier multiplier) {
        return mockReadingType(MacroPeriod.NOTAPPLICABLE, timeAttribute, accumulation, Commodity.ELECTRICITY_SECONDARY_METERED, multiplier);
    }

    private ReadingType mockIrregularReadingType(Accumulation accumulation, MetricMultiplier multiplier) {
        return mockReadingType(MacroPeriod.NOTAPPLICABLE, TimeAttribute.NOTAPPLICABLE, accumulation, Commodity.ELECTRICITY_SECONDARY_METERED, multiplier);
    }

    private ReadingType mockReadingType(MacroPeriod macroPeriod, TimeAttribute measuringPeriod, Accumulation accumulation, Commodity commodity, MetricMultiplier multiplier) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(macroPeriod);
        when(readingType.getAggregate()).thenReturn(Aggregate.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(measuringPeriod);
        when(readingType.getAccumulation()).thenReturn(accumulation);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.FORWARD);
        when(readingType.getCommodity()).thenReturn(commodity);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.NOTAPPLICABLE);
        when(readingType.getInterharmonic()).thenReturn(new RationalNumber(0, 1));
        when(readingType.getArgument()).thenReturn(new RationalNumber(0, 1));
        when(readingType.getTou()).thenReturn(0);
        when(readingType.getCpp()).thenReturn(0);
        when(readingType.getConsumptionTier()).thenReturn(0);
        when(readingType.getPhases()).thenReturn(Phase.NOTAPPLICABLE);
        when(readingType.getMultiplier()).thenReturn(multiplier);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("USD"));
        if (macroPeriod.isApplicable() || measuringPeriod.isApplicable()) {
            when(readingType.isRegular()).thenReturn(true);
        }
        return readingType;
    }

    private Object[] getDefaultMessageSeedArgs() {
        return new Object[]{
                this.meterAdvanceValidator.instantToString(this.validatedInterval.lowerEndpoint()),
                this.meterAdvanceValidator.instantToString(this.validatedInterval.upperEndpoint()),
                this.meterAdvanceValidator.getDisplayName(),
                this.readingType.getMRID(),
                this.usagePoint.getName()
        };
    }
}
