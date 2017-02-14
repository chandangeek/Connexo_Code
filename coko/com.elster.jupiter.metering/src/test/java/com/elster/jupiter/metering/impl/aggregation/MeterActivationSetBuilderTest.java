/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.ServerCalendarUsage;
import com.elster.jupiter.metering.impl.ServerUsagePoint;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.slp.SyntheticLoadProfile;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link MeterActivationSetBuilder} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterActivationSetBuilderTest {

    private static final long CALENDAR1_ID = 97L;
    private static final long CALENDAR2_ID = 101L;

    private static final Instant MAY_1ST_2016 = Instant.ofEpochMilli(1462053600000L);
    private static final Instant MAY_2ND_2016 = Instant.ofEpochMilli(1462140000000L);
    private static final Instant JUNE_1ST_2016 = Instant.ofEpochMilli(1464732000000L);
    private static final Instant JUNE_2ND_2016 = Instant.ofEpochMilli(1464818400000L);
    private static final Instant JULY_1ST_2016 = Instant.ofEpochMilli(1467324000000L);
    private static final Instant JULY_2ND_2016 = Instant.ofEpochMilli(1467410400000L);
    private static final Instant AUG_1ST_2016 = Instant.ofEpochMilli(1470002400000L);

    private static final String SLP1_PROPERTY_NAME = "slp1";
    private static final String SLP2_PROPERTY_NAME = "slp2";
    private static final String OTHER_PROPERTY_NAME = "other";
    private static final String UNKNOWN_PROPERTY_NAME = "unknown";

    @Mock
    private ServiceCategory serviceCategory;
    @Mock
    private ServerUsagePoint usagePoint;
    @Mock
    private MeterRole main;
    @Mock
    private MeterActivation mainMeterActivation;
    @Mock
    private MeterRole check;
    @Mock
    private MeterActivation checkMeterActivation;
    @Mock
    private UsagePointMetrologyConfiguration configuration;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private RegisteredCustomPropertySet registeredCustomPropertySet;
    @Mock
    private CustomPropertySet<UsagePoint, ?> customPropertySet;
    @Mock
    private SyntheticLoadProfile syntheticLoadProfile;
    @Mock
    private Category category;
    @Mock
    private Calendar calendar;

    private Range<Instant> period;

    @Before
    public void initializeMocks() {
        when(this.main.getKey()).thenReturn("meterole.main");
        when(this.main.getDisplayName()).thenReturn("Main");
        when(this.mainMeterActivation.getMeterRole()).thenReturn(Optional.of(this.main));
        when(this.check.getKey()).thenReturn("meterole.check");
        when(this.check.getDisplayName()).thenReturn("Check");
        when(this.checkMeterActivation.getMeterRole()).thenReturn(Optional.of(this.check));
        when(this.serviceCategory.getCustomPropertySets()).thenReturn(Collections.emptyList());
        when(this.usagePoint.getServiceCategory()).thenReturn(this.serviceCategory);
        when(this.usagePoint.getEffectiveMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.of(this.effectiveMetrologyConfigurationOnUsagePoint));
        when(this.usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(this.effectiveMetrologyConfigurationOnUsagePoint));
        when(this.usagePoint.getTimeOfUseCalendarUsages()).thenReturn(Collections.emptyList());
        when(this.effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(this.configuration);
        when(this.category.getId()).thenReturn(1L);
        when(this.category.getName()).thenReturn(OutOfTheBoxCategory.TOU.getDefaultDisplayName());
        this.initializeCalendar(this.calendar, CALENDAR1_ID, 1);
        this.initializeCustomPropertySet();
    }

    private void initializeCustomPropertySet() {
        this.initializeCustomPropertySet(this.registeredCustomPropertySet, this.customPropertySet, SLP1_PROPERTY_NAME);
    }

    private void initializeCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet, CustomPropertySet<UsagePoint, ?> customPropertySet, String slpPropertyName) {
        ValueFactory slpValueFactory = mock(ValueFactory.class);
        when(slpValueFactory.isReference()).thenReturn(true);
        when(slpValueFactory.getValueType()).thenReturn(SyntheticLoadProfile.class);
        PropertySpec slp = mock(PropertySpec.class);
        when(slp.getName()).thenReturn(slpPropertyName);
        when(slp.isReference()).thenReturn(true);
        when(slp.getValueFactory()).thenReturn(slpValueFactory);
        PropertySpec other = mock(PropertySpec.class);
        when(other.getName()).thenReturn(OTHER_PROPERTY_NAME);
        when(other.isReference()).thenReturn(false);
        when(other.getValueFactory()).thenReturn(new StringFactory());
        when(customPropertySet.getPropertySpecs()).thenReturn(Arrays.asList(other, slp));
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(this.customPropertySet);
    }

    private void markCustomPropertySetVersioned(CustomPropertySet<UsagePoint, ?> customPropertySet) {
        when(customPropertySet.isVersioned()).thenReturn(true);
        when(customPropertySet.getId()).thenReturn(MeterActivationSetBuilderTest.class.getSimpleName() + ".versioned");
    }

    private void markCustomPropertySetNonVersioned(CustomPropertySet<UsagePoint, ?> customPropertySet) {
        when(customPropertySet.isVersioned()).thenReturn(false);
        when(customPropertySet.getId()).thenReturn(MeterActivationSetBuilderTest.class.getSimpleName() + "not.versioned");
    }

    private void initializeCalendar(Calendar calendar, long id, int sequenceNumber) {
        this.initializeCalendar(calendar, id, MeterActivationSetBuilderTest.class.getSimpleName() + sequenceNumber);
    }

    private void initializeCalendar(Calendar calendar, long id, String name) {
        when(calendar.getId()).thenReturn(id);
        when(calendar.getMRID()).thenReturn(name);
        when(calendar.getCategory()).thenReturn(this.category);
        when(calendar.getObsoleteTime()).thenReturn(Optional.empty());
    }

    @Test
    public void noMeterActivations_NoCalendarUsages_NoCustomPropertySets() {
        when(this.usagePoint.getMeterActivations()).thenReturn(Collections.emptyList());
        this.period = Range.atLeast(MAY_1ST_2016);
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).isEmpty();
    }

    @Test
    public void noMeterActivations_SingleCalendarUsage_NoCustomPropertySets() {
        this.period = Range.atLeast(MAY_1ST_2016);
        when(this.usagePoint.getMeterActivations()).thenReturn(Collections.emptyList());
        ServerCalendarUsage calendarUsage = mock(ServerCalendarUsage.class);
        when(calendarUsage.getCalendar()).thenReturn(this.calendar);
        when(calendarUsage.overlaps(this.period)).thenReturn(true);
        when(calendarUsage.getRange()).thenReturn(Range.closedOpen(MAY_1ST_2016, MAY_2ND_2016));
        when(this.usagePoint.getTimeOfUseCalendarUsages()).thenReturn(Collections.singletonList(calendarUsage));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        List<Calendar> calendars = meterActivationSets.stream().map(MeterActivationSet::getCalendar).collect(Collectors.toList());
        assertThat(calendars).containsOnly(this.calendar);
    }

    @Test
    public void noMeterActivations_noCalendarUsage_NonVersionedCustomPropertySetsOnMetrologyConfiguration() {
        this.period = Range.atLeast(MAY_1ST_2016);
        when(this.usagePoint.getMeterActivations()).thenReturn(Collections.emptyList());
        when(this.usagePoint.getTimeOfUseCalendarUsages()).thenReturn(Collections.emptyList());
        this.markCustomPropertySetNonVersioned(this.customPropertySet);
        when(this.configuration.getCustomPropertySets()).thenReturn(Collections.singletonList(this.registeredCustomPropertySet));
        CustomPropertySetValues customPropertySetValues = CustomPropertySetValues.empty();
        customPropertySetValues.setProperty(OTHER_PROPERTY_NAME, "OTHER");
        customPropertySetValues.setProperty(SLP1_PROPERTY_NAME, this.syntheticLoadProfile);
        when(this.customPropertySetService.getUniqueValuesFor(this.customPropertySet, this.usagePoint)).thenReturn(customPropertySetValues);
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(1);
        assertThat(meterActivationSets.get(0).getRange()).isEqualTo(Range.atLeast(MAY_1ST_2016));
        assertThat(meterActivationSets.get(0).getCalendar()).isNull();
        assertThat(meterActivationSets.get(0).getMeterActivations()).isEmpty();
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isEqualTo(this.syntheticLoadProfile);
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(OTHER_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(UNKNOWN_PROPERTY_NAME)).isNull();
    }

    @Test
    public void noMeterActivations_noCalendarUsage_NonVersionedCustomPropertySetsOnServiceCategory() {
        this.period = Range.atLeast(MAY_1ST_2016);
        when(this.usagePoint.getMeterActivations()).thenReturn(Collections.emptyList());
        when(this.usagePoint.getTimeOfUseCalendarUsages()).thenReturn(Collections.emptyList());
        this.markCustomPropertySetNonVersioned(this.customPropertySet);
        when(this.serviceCategory.getCustomPropertySets()).thenReturn(Collections.singletonList(this.registeredCustomPropertySet));
        CustomPropertySetValues customPropertySetValues = CustomPropertySetValues.empty();
        customPropertySetValues.setProperty(OTHER_PROPERTY_NAME, "OTHER");
        customPropertySetValues.setProperty(SLP1_PROPERTY_NAME, this.syntheticLoadProfile);
        when(this.customPropertySetService.getUniqueValuesFor(this.customPropertySet, this.usagePoint)).thenReturn(customPropertySetValues);
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(1);
        assertThat(meterActivationSets.get(0).getRange()).isEqualTo(Range.atLeast(MAY_1ST_2016));
        assertThat(meterActivationSets.get(0).getCalendar()).isNull();
        assertThat(meterActivationSets.get(0).getMeterActivations()).isEmpty();
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isEqualTo(this.syntheticLoadProfile);
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(OTHER_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(UNKNOWN_PROPERTY_NAME)).isNull();
    }

    @Test
    public void noMeterActivations_noCalendarUsage_singleVersionedCustomPropertySetsOnMetrologyConfiguration() {
        this.period = Range.atLeast(MAY_1ST_2016);
        when(this.usagePoint.getMeterActivations()).thenReturn(Collections.emptyList());
        when(this.usagePoint.getTimeOfUseCalendarUsages()).thenReturn(Collections.emptyList());
        this.markCustomPropertySetVersioned(this.customPropertySet);
        when(this.configuration.getCustomPropertySets()).thenReturn(Collections.singletonList(this.registeredCustomPropertySet));
        CustomPropertySetValues customPropertySetValues = CustomPropertySetValues.emptyDuring(Range.atLeast(MAY_1ST_2016));
        customPropertySetValues.setProperty(OTHER_PROPERTY_NAME, "OTHER");
        customPropertySetValues.setProperty(SLP1_PROPERTY_NAME, this.syntheticLoadProfile);
        when(this.customPropertySetService.getUniqueValuesFor(this.customPropertySet, this.usagePoint, MAY_1ST_2016)).thenReturn(customPropertySetValues);
        when(this.customPropertySetService.getAllVersionedValuesFor(this.customPropertySet, this.usagePoint)).thenReturn(Collections.singletonList(customPropertySetValues));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(1);
        assertThat(meterActivationSets.get(0).getRange()).isEqualTo(Range.atLeast(MAY_1ST_2016));
        assertThat(meterActivationSets.get(0).getCalendar()).isNull();
        assertThat(meterActivationSets.get(0).getMeterActivations()).isEmpty();
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isEqualTo(this.syntheticLoadProfile);
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(OTHER_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(UNKNOWN_PROPERTY_NAME)).isNull();
    }

    @Test
    public void noMeterActivations_noCalendarUsage_singleVersionedCustomPropertySetsOnServiceCategory() {
        this.period = Range.atLeast(MAY_1ST_2016);
        when(this.usagePoint.getMeterActivations()).thenReturn(Collections.emptyList());
        when(this.usagePoint.getTimeOfUseCalendarUsages()).thenReturn(Collections.emptyList());
        this.markCustomPropertySetVersioned(this.customPropertySet);
        when(this.serviceCategory.getCustomPropertySets()).thenReturn(Collections.singletonList(this.registeredCustomPropertySet));
        CustomPropertySetValues customPropertySetValues = CustomPropertySetValues.emptyDuring(Range.atLeast(MAY_1ST_2016));
        customPropertySetValues.setProperty(OTHER_PROPERTY_NAME, "OTHER");
        customPropertySetValues.setProperty(SLP1_PROPERTY_NAME, this.syntheticLoadProfile);
        when(this.customPropertySetService.getUniqueValuesFor(this.customPropertySet, this.usagePoint, MAY_1ST_2016)).thenReturn(customPropertySetValues);
        when(this.customPropertySetService.getAllVersionedValuesFor(this.customPropertySet, this.usagePoint)).thenReturn(Collections.singletonList(customPropertySetValues));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(1);
        assertThat(meterActivationSets.get(0).getRange()).isEqualTo(Range.atLeast(MAY_1ST_2016));
        assertThat(meterActivationSets.get(0).getCalendar()).isNull();
        assertThat(meterActivationSets.get(0).getMeterActivations()).isEmpty();
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isEqualTo(this.syntheticLoadProfile);
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(OTHER_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(UNKNOWN_PROPERTY_NAME)).isNull();
    }

    @Test
    public void noMeterActivations_noCalendarUsage_multipleVersionedCustomPropertySetsOnMetrologyConfiguration() {
        this.period = Range.atLeast(MAY_1ST_2016);
        when(this.usagePoint.getMeterActivations()).thenReturn(Collections.emptyList());
        when(this.usagePoint.getTimeOfUseCalendarUsages()).thenReturn(Collections.emptyList());
        this.markCustomPropertySetVersioned(this.customPropertySet);
        when(this.configuration.getCustomPropertySets()).thenReturn(Collections.singletonList(this.registeredCustomPropertySet));
        CustomPropertySetValues customPropertySetValues1 = CustomPropertySetValues.emptyDuring(Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        customPropertySetValues1.setProperty(OTHER_PROPERTY_NAME, "OTHER");
        customPropertySetValues1.setProperty(SLP1_PROPERTY_NAME, this.syntheticLoadProfile);
        when(this.customPropertySetService.getUniqueValuesFor(this.customPropertySet, this.usagePoint, MAY_1ST_2016)).thenReturn(customPropertySetValues1);
        CustomPropertySetValues customPropertySetValues2 = CustomPropertySetValues.emptyDuring(Range.atLeast(JUNE_1ST_2016));
        customPropertySetValues2.setProperty(OTHER_PROPERTY_NAME, "SOMETHING ELSE");
        SyntheticLoadProfile otherSlp = mock(SyntheticLoadProfile.class);
        customPropertySetValues2.setProperty(SLP1_PROPERTY_NAME, otherSlp);
        when(this.customPropertySetService.getUniqueValuesFor(this.customPropertySet, this.usagePoint, JUNE_1ST_2016)).thenReturn(customPropertySetValues2);
        when(this.customPropertySetService.getAllVersionedValuesFor(this.customPropertySet, this.usagePoint)).thenReturn(Arrays.asList(customPropertySetValues1, customPropertySetValues2));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(2);
        assertThat(meterActivationSets.get(0).getRange()).isEqualTo(Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        assertThat(meterActivationSets.get(0).getCalendar()).isNull();
        assertThat(meterActivationSets.get(0).getMeterActivations()).isEmpty();
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isEqualTo(this.syntheticLoadProfile);
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(OTHER_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(UNKNOWN_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(1).getRange()).isEqualTo(Range.atLeast(JUNE_1ST_2016));
        assertThat(meterActivationSets.get(1).getCalendar()).isNull();
        assertThat(meterActivationSets.get(1).getMeterActivations()).isEmpty();
        assertThat(meterActivationSets.get(1).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isEqualTo(otherSlp);
        assertThat(meterActivationSets.get(1).getSyntheticLoadProfile(OTHER_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(1).getSyntheticLoadProfile(UNKNOWN_PROPERTY_NAME)).isNull();
    }

    @Test
    public void noMeterActivations_noCalendarUsage_multipleVersionedCustomPropertySetsOnServiceCategory() {
        this.period = Range.atLeast(MAY_1ST_2016);
        when(this.usagePoint.getMeterActivations()).thenReturn(Collections.emptyList());
        when(this.usagePoint.getTimeOfUseCalendarUsages()).thenReturn(Collections.emptyList());
        this.markCustomPropertySetVersioned(this.customPropertySet);
        when(this.serviceCategory.getCustomPropertySets()).thenReturn(Collections.singletonList(this.registeredCustomPropertySet));
        CustomPropertySetValues customPropertySetValues1 = CustomPropertySetValues.emptyDuring(Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        customPropertySetValues1.setProperty(OTHER_PROPERTY_NAME, "OTHER");
        customPropertySetValues1.setProperty(SLP1_PROPERTY_NAME, this.syntheticLoadProfile);
        when(this.customPropertySetService.getUniqueValuesFor(this.customPropertySet, this.usagePoint, MAY_1ST_2016)).thenReturn(customPropertySetValues1);
        CustomPropertySetValues customPropertySetValues2 = CustomPropertySetValues.emptyDuring(Range.atLeast(JUNE_1ST_2016));
        customPropertySetValues2.setProperty(OTHER_PROPERTY_NAME, "SOMETHING ELSE");
        SyntheticLoadProfile otherSlp = mock(SyntheticLoadProfile.class);
        customPropertySetValues2.setProperty(SLP1_PROPERTY_NAME, otherSlp);
        when(this.customPropertySetService.getUniqueValuesFor(this.customPropertySet, this.usagePoint, JUNE_1ST_2016)).thenReturn(customPropertySetValues2);
        when(this.customPropertySetService.getAllVersionedValuesFor(this.customPropertySet, this.usagePoint)).thenReturn(Arrays.asList(customPropertySetValues1, customPropertySetValues2));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(2);
        assertThat(meterActivationSets.get(0).getRange()).isEqualTo(Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        assertThat(meterActivationSets.get(0).getCalendar()).isNull();
        assertThat(meterActivationSets.get(0).getMeterActivations()).isEmpty();
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isEqualTo(this.syntheticLoadProfile);
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(OTHER_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(UNKNOWN_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(1).getRange()).isEqualTo(Range.atLeast(JUNE_1ST_2016));
        assertThat(meterActivationSets.get(1).getCalendar()).isNull();
        assertThat(meterActivationSets.get(1).getMeterActivations()).isEmpty();
        assertThat(meterActivationSets.get(1).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isEqualTo(otherSlp);
        assertThat(meterActivationSets.get(1).getSyntheticLoadProfile(OTHER_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(1).getSyntheticLoadProfile(UNKNOWN_PROPERTY_NAME)).isNull();
    }

    @Test
    public void noMeterActivations_MultipleCalendarUsages_NoCustomPropertySets() {
        this.period = Range.atLeast(MAY_2ND_2016);
        when(this.usagePoint.getMeterActivations()).thenReturn(Collections.emptyList());
        ServerCalendarUsage calendar1Usage = mock(ServerCalendarUsage.class);
        when(calendar1Usage.getCalendar()).thenReturn(this.calendar);
        when(calendar1Usage.overlaps(this.period)).thenReturn(true);
        when(calendar1Usage.overlaps(Range.closedOpen(MAY_2ND_2016, JUNE_1ST_2016))).thenReturn(true);
        when(calendar1Usage.getRange()).thenReturn(Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        Calendar calendar2 = mock(Calendar.class);
        this.initializeCalendar(calendar2, CALENDAR2_ID, 2);
        ServerCalendarUsage calendar2Usage = mock(ServerCalendarUsage.class);
        when(calendar2Usage.getCalendar()).thenReturn(calendar2);
        when(calendar2Usage.overlaps(this.period)).thenReturn(true);
        when(calendar2Usage.overlaps(Range.atLeast(JUNE_1ST_2016))).thenReturn(true);
        when(calendar2Usage.getRange()).thenReturn(Range.atLeast(JUNE_1ST_2016));
        when(this.usagePoint.getTimeOfUseCalendarUsages()).thenReturn(Arrays.asList(calendar1Usage, calendar2Usage));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(2);
        assertThat(meterActivationSets.get(0).getRange()).isEqualTo(Range.closedOpen(MAY_2ND_2016, JUNE_1ST_2016));
        assertThat(meterActivationSets.get(0).getCalendar()).isEqualTo(this.calendar);
        assertThat(meterActivationSets.get(0).getMeterActivations()).isEmpty();
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(1).getRange()).isEqualTo(Range.atLeast(JUNE_1ST_2016));
        assertThat(meterActivationSets.get(1).getCalendar()).isEqualTo(calendar2);
        assertThat(meterActivationSets.get(1).getMeterActivations()).isEmpty();
        assertThat(meterActivationSets.get(1).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isNull();
    }

    @Test
    public void mainCheckMeterActivations_MultipleCalendarUsages_VersionCPSOnConfigurationAndNonVersionedCPSOnServiceCategory() {
        // Setup aggregation period
        this.period = Range.atLeast(MAY_1ST_2016);

        // Setup meter activations
        MeterActivation mainActivation1 = mock(MeterActivation.class);
        this.mockRange(mainActivation1, Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        when(mainActivation1.overlaps(this.period)).thenReturn(true);
        MeterActivation mainActivation2 = mock(MeterActivation.class);
        this.mockRange(mainActivation2, Range.atLeast(JUNE_1ST_2016));
        when(mainActivation2.overlaps(this.period)).thenReturn(true);
        this.mockRange(this.checkMeterActivation, Range.atLeast(JULY_1ST_2016));
        when(this.checkMeterActivation.overlaps(this.period)).thenReturn(true);
        when(this.usagePoint.getMeterActivations()).thenReturn(Arrays.asList(mainActivation1, mainActivation2, this.checkMeterActivation));

        // Setup calendar usage
        ServerCalendarUsage calendar1Usage = mock(ServerCalendarUsage.class);
        when(calendar1Usage.getCalendar()).thenReturn(this.calendar);
        when(calendar1Usage.overlaps(this.period)).thenReturn(true);
        when(calendar1Usage.getRange()).thenReturn(Range.closedOpen(MAY_1ST_2016, JULY_2ND_2016));
        Calendar calendar2 = mock(Calendar.class);
        this.initializeCalendar(calendar2, CALENDAR2_ID, 2);
        ServerCalendarUsage calendar2Usage = mock(ServerCalendarUsage.class);
        when(calendar2Usage.getCalendar()).thenReturn(calendar2);
        when(calendar2Usage.overlaps(this.period)).thenReturn(true);
        when(calendar2Usage.getRange()).thenReturn(Range.atLeast(JULY_2ND_2016));
        when(this.usagePoint.getTimeOfUseCalendarUsages()).thenReturn(Arrays.asList(calendar1Usage, calendar2Usage));

        // Setup custom properties on metrology configuration
        when(this.configuration.getCustomPropertySets()).thenReturn(Collections.singletonList(this.registeredCustomPropertySet));
        when(this.customPropertySet.isVersioned()).thenReturn(true);
        CustomPropertySetValues customPropertySetValues1 = CustomPropertySetValues.emptyDuring(Range.closedOpen(MAY_1ST_2016, JUNE_2ND_2016));
        customPropertySetValues1.setProperty(OTHER_PROPERTY_NAME, "OTHER");
        customPropertySetValues1.setProperty(SLP1_PROPERTY_NAME, this.syntheticLoadProfile);
        when(this.customPropertySetService.getUniqueValuesFor(this.customPropertySet, this.usagePoint, MAY_1ST_2016)).thenReturn(customPropertySetValues1);
        when(this.customPropertySetService.getUniqueValuesFor(this.customPropertySet, this.usagePoint, JUNE_1ST_2016)).thenReturn(customPropertySetValues1);
        CustomPropertySetValues customPropertySetValues2 = CustomPropertySetValues.emptyDuring(Range.atLeast(JUNE_2ND_2016));
        customPropertySetValues2.setProperty(OTHER_PROPERTY_NAME, "SOMETHING ELSE");
        SyntheticLoadProfile otherSlp = mock(SyntheticLoadProfile.class);
        customPropertySetValues2.setProperty(SLP1_PROPERTY_NAME, otherSlp);
        when(this.customPropertySetService.getUniqueValuesFor(this.customPropertySet, this.usagePoint, JUNE_2ND_2016)).thenReturn(customPropertySetValues2);
        when(this.customPropertySetService.getUniqueValuesFor(this.customPropertySet, this.usagePoint, JULY_1ST_2016)).thenReturn(customPropertySetValues2);
        when(this.customPropertySetService.getUniqueValuesFor(this.customPropertySet, this.usagePoint, JULY_2ND_2016)).thenReturn(customPropertySetValues2);
        when(this.customPropertySetService.getAllVersionedValuesFor(this.customPropertySet, this.usagePoint)).thenReturn(Arrays.asList(customPropertySetValues1, customPropertySetValues2));

        // Setup custom properties on service category
        RegisteredCustomPropertySet nonVersionedRegisteredCPS = mock(RegisteredCustomPropertySet.class);
        CustomPropertySet<UsagePoint, ?> nonVersionedCPS = mock(CustomPropertySet.class);
        this.markCustomPropertySetNonVersioned(nonVersionedCPS);
        this.initializeCustomPropertySet(nonVersionedRegisteredCPS, nonVersionedCPS, SLP2_PROPERTY_NAME);
        when(nonVersionedRegisteredCPS.getCustomPropertySet()).thenReturn(nonVersionedCPS);
        when(this.serviceCategory.getCustomPropertySets()).thenReturn(Collections.singletonList(nonVersionedRegisteredCPS));
        SyntheticLoadProfile serviceCategorySlp = mock(SyntheticLoadProfile.class);
        CustomPropertySetValues nonVersionCPSValues = CustomPropertySetValues.empty();
        nonVersionCPSValues.setProperty(OTHER_PROPERTY_NAME, "OTHER");
        nonVersionCPSValues.setProperty(SLP2_PROPERTY_NAME, serviceCategorySlp);
        when(this.customPropertySetService.getUniqueValuesFor(nonVersionedCPS, this.usagePoint)).thenReturn(nonVersionCPSValues);

        // Create test instance
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(5);
        assertThat(meterActivationSets.get(0).getRange()).isEqualTo(Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        assertThat(meterActivationSets.get(0).getMeterActivations()).containsOnly(mainActivation1);
        assertThat(meterActivationSets.get(0).getCalendar()).isEqualTo(this.calendar);
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isEqualTo(this.syntheticLoadProfile);
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(SLP2_PROPERTY_NAME)).isEqualTo(serviceCategorySlp);
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(OTHER_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(UNKNOWN_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(1).getRange()).isEqualTo(Range.closedOpen(JUNE_1ST_2016, JUNE_2ND_2016));
        assertThat(meterActivationSets.get(1).getMeterActivations()).containsOnly(mainActivation2);
        assertThat(meterActivationSets.get(1).getCalendar()).isEqualTo(this.calendar);
        assertThat(meterActivationSets.get(1).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isEqualTo(this.syntheticLoadProfile);
        assertThat(meterActivationSets.get(1).getSyntheticLoadProfile(SLP2_PROPERTY_NAME)).isEqualTo(serviceCategorySlp);
        assertThat(meterActivationSets.get(1).getSyntheticLoadProfile(OTHER_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(1).getSyntheticLoadProfile(UNKNOWN_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(2).getRange()).isEqualTo(Range.closedOpen(JUNE_2ND_2016, JULY_1ST_2016));
        assertThat(meterActivationSets.get(2).getMeterActivations()).containsOnly(mainActivation2);
        assertThat(meterActivationSets.get(2).getCalendar()).isEqualTo(this.calendar);
        assertThat(meterActivationSets.get(2).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isEqualTo(otherSlp);
        assertThat(meterActivationSets.get(2).getSyntheticLoadProfile(SLP2_PROPERTY_NAME)).isEqualTo(serviceCategorySlp);
        assertThat(meterActivationSets.get(2).getSyntheticLoadProfile(OTHER_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(2).getSyntheticLoadProfile(UNKNOWN_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(3).getRange()).isEqualTo(Range.closedOpen(JULY_1ST_2016, JULY_2ND_2016));
        assertThat(meterActivationSets.get(3).getMeterActivations()).containsOnly(mainActivation2, this.checkMeterActivation);
        assertThat(meterActivationSets.get(3).getCalendar()).isEqualTo(this.calendar);
        assertThat(meterActivationSets.get(3).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isEqualTo(otherSlp);
        assertThat(meterActivationSets.get(3).getSyntheticLoadProfile(SLP2_PROPERTY_NAME)).isEqualTo(serviceCategorySlp);
        assertThat(meterActivationSets.get(3).getSyntheticLoadProfile(OTHER_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(3).getSyntheticLoadProfile(UNKNOWN_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(4).getRange()).isEqualTo(Range.atLeast(JULY_2ND_2016));
        assertThat(meterActivationSets.get(4).getMeterActivations()).containsOnly(mainActivation2, this.checkMeterActivation);
        assertThat(meterActivationSets.get(4).getCalendar()).isEqualTo(calendar2);
        assertThat(meterActivationSets.get(4).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isEqualTo(otherSlp);
        assertThat(meterActivationSets.get(4).getSyntheticLoadProfile(SLP2_PROPERTY_NAME)).isEqualTo(serviceCategorySlp);
        assertThat(meterActivationSets.get(4).getSyntheticLoadProfile(OTHER_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(4).getSyntheticLoadProfile(UNKNOWN_PROPERTY_NAME)).isNull();
    }

    @Test
    public void noOverlappingMeterActivations_NoOverlappingCalendarUsages_NoCustomPropertySets() {
        this.period = Range.closedOpen(JUNE_1ST_2016, JUNE_2ND_2016);
        MeterActivation meterActivation = mock(MeterActivation.class);
        this.mockRange(meterActivation, Range.closedOpen(MAY_1ST_2016, Instant.ofEpochMilli(1462140000000L)));  // (May 1st 2016, May 2nd 2016]
        when(meterActivation.overlaps(this.period)).thenReturn(false);
        when(this.usagePoint.getMeterActivations()).thenReturn(Collections.singletonList(meterActivation));
        ServerCalendarUsage calendarUsage = mock(ServerCalendarUsage.class);
        when(calendarUsage.overlaps(this.period)).thenReturn(false);
        when(calendarUsage.getCalendar()).thenReturn(this.calendar);
        when(this.usagePoint.getTimeOfUseCalendarUsages()).thenReturn(Collections.singletonList(calendarUsage));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).isEmpty();
    }

    @Test
    public void noOverlappingMeterActivations_SingleOverlappingCalendarUsage_NoCustomPropertySets() {
        this.period = Range.closedOpen(JUNE_1ST_2016, JUNE_2ND_2016);
        MeterActivation meterActivation = mock(MeterActivation.class);
        this.mockRange(meterActivation, Range.closedOpen(MAY_1ST_2016, Instant.ofEpochMilli(1462140000000L)));  // (May 1st 2016, May 2nd 2016]
        when(meterActivation.overlaps(this.period)).thenReturn(false);
        when(this.usagePoint.getMeterActivations()).thenReturn(Collections.singletonList(meterActivation));
        ServerCalendarUsage calendarUsage = mock(ServerCalendarUsage.class);
        when(calendarUsage.overlaps(this.period)).thenReturn(true);
        when(calendarUsage.getRange()).thenReturn(Range.atLeast(MAY_1ST_2016));
        when(calendarUsage.getCalendar()).thenReturn(this.calendar);
        when(this.usagePoint.getTimeOfUseCalendarUsages()).thenReturn(Collections.singletonList(calendarUsage));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(1);
        assertThat(meterActivationSets.get(0).getRange()).isEqualTo(Range.closedOpen(JUNE_1ST_2016, JUNE_2ND_2016));
        assertThat(meterActivationSets.get(0).getCalendar()).isEqualTo(this.calendar);
        assertThat(meterActivationSets.get(0).getMeterActivations()).isEmpty();
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isNull();
    }

    @Test
    public void noOverlappingMeterActivations_MultipleOverlappingCalendarUsage_NoCustomPropertySets() {
        this.period = Range.closedOpen(MAY_2ND_2016, AUG_1ST_2016);
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(meterActivation.overlaps(any(Range.class))).thenReturn(false);
        when(this.usagePoint.getMeterActivations()).thenReturn(Collections.singletonList(meterActivation));
        ServerCalendarUsage calendar1Usage = mock(ServerCalendarUsage.class);
        when(calendar1Usage.overlaps(this.period)).thenReturn(true);
        when(calendar1Usage.getRange()).thenReturn(Range.closedOpen(MAY_1ST_2016, JULY_1ST_2016));
        when(calendar1Usage.getCalendar()).thenReturn(this.calendar);
        Calendar calendar2 = mock(Calendar.class);
        this.initializeCalendar(calendar2, CALENDAR2_ID, 2);
        ServerCalendarUsage calendar2Usage = mock(ServerCalendarUsage.class);
        when(calendar2Usage.getCalendar()).thenReturn(calendar2);
        when(calendar2Usage.overlaps(this.period)).thenReturn(true);
        when(calendar2Usage.getRange()).thenReturn(Range.atLeast(JULY_1ST_2016));
        when(this.usagePoint.getTimeOfUseCalendarUsages()).thenReturn(Arrays.asList(calendar1Usage, calendar2Usage));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(2);
        assertThat(meterActivationSets.get(0).getRange()).isEqualTo(Range.closedOpen(MAY_2ND_2016, JULY_1ST_2016));
        assertThat(meterActivationSets.get(0).getCalendar()).isEqualTo(calendar);
        assertThat(meterActivationSets.get(0).getMeterActivations()).isEmpty();
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(1).getRange()).isEqualTo(Range.closedOpen(JULY_1ST_2016, AUG_1ST_2016));
        assertThat(meterActivationSets.get(1).getCalendar()).isEqualTo(calendar2);
        assertThat(meterActivationSets.get(1).getMeterActivations()).isEmpty();
        assertThat(meterActivationSets.get(1).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isNull();
    }

    @Test
    public void oneOverlappingMeterActivations_MultipleOverlappingCalendarUsage_NoCustomPropertySets() {
        this.period = Range.closedOpen(MAY_2ND_2016, AUG_1ST_2016);
        MeterActivation meterActivation = mock(MeterActivation.class);
        this.mockRange(meterActivation, Range.atLeast(MAY_1ST_2016));
        when(meterActivation.overlaps(this.period)).thenReturn(true);
        when(this.usagePoint.getMeterActivations()).thenReturn(Collections.singletonList(meterActivation));
        ServerCalendarUsage calendar1Usage = mock(ServerCalendarUsage.class);
        when(calendar1Usage.overlaps(this.period)).thenReturn(true);
        when(calendar1Usage.getRange()).thenReturn(Range.closedOpen(JUNE_1ST_2016, JULY_1ST_2016));
        when(calendar1Usage.getCalendar()).thenReturn(this.calendar);
        Calendar calendar2 = mock(Calendar.class);
        this.initializeCalendar(calendar2, CALENDAR2_ID, 2);
        ServerCalendarUsage calendar2Usage = mock(ServerCalendarUsage.class);
        when(calendar2Usage.getCalendar()).thenReturn(calendar2);
        when(calendar2Usage.overlaps(this.period)).thenReturn(true);
        when(calendar2Usage.getRange()).thenReturn(Range.atLeast(JULY_1ST_2016));
        when(this.usagePoint.getTimeOfUseCalendarUsages()).thenReturn(Arrays.asList(calendar1Usage, calendar2Usage));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(3);
        assertThat(meterActivationSets.get(0).getRange()).isEqualTo(Range.closedOpen(MAY_2ND_2016, JUNE_1ST_2016));
        assertThat(meterActivationSets.get(0).getCalendar()).isNull();
        assertThat(meterActivationSets.get(0).getMeterActivations()).containsOnly(meterActivation);
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(1).getRange()).isEqualTo(Range.closedOpen(JUNE_1ST_2016, JULY_1ST_2016));
        assertThat(meterActivationSets.get(1).getCalendar()).isEqualTo(this.calendar);
        assertThat(meterActivationSets.get(1).getMeterActivations()).containsOnly(meterActivation);
        assertThat(meterActivationSets.get(1).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(2).getRange()).isEqualTo(Range.closedOpen(JULY_1ST_2016, AUG_1ST_2016));
        assertThat(meterActivationSets.get(2).getCalendar()).isEqualTo(calendar2);
        assertThat(meterActivationSets.get(2).getMeterActivations()).containsOnly(meterActivation);
        assertThat(meterActivationSets.get(2).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isNull();
    }

    @Test
    public void multipleOverlappingMeterActivations_MultipleOverlappingCalendarUsage_NoCustomPropertySets() {
        this.period = Range.closedOpen(MAY_2ND_2016, AUG_1ST_2016);
        MeterActivation meterActivation1 = mock(MeterActivation.class);
        this.mockRange(meterActivation1, Range.closedOpen(MAY_1ST_2016, JUNE_2ND_2016));
        when(meterActivation1.overlaps(this.period)).thenReturn(true);
        MeterActivation meterActivation2 = mock(MeterActivation.class);
        this.mockRange(meterActivation2, Range.atLeast(JUNE_2ND_2016));
        when(meterActivation2.overlaps(this.period)).thenReturn(true);
        when(this.usagePoint.getMeterActivations()).thenReturn(Arrays.asList(meterActivation1, meterActivation2));
        ServerCalendarUsage calendar1Usage = mock(ServerCalendarUsage.class);
        when(calendar1Usage.overlaps(this.period)).thenReturn(true);
        when(calendar1Usage.getRange()).thenReturn(Range.closedOpen(JUNE_1ST_2016, JULY_1ST_2016));
        when(calendar1Usage.getCalendar()).thenReturn(this.calendar);
        Calendar calendar2 = mock(Calendar.class);
        this.initializeCalendar(calendar2, CALENDAR2_ID, 2);
        ServerCalendarUsage calendar2Usage = mock(ServerCalendarUsage.class);
        when(calendar2Usage.getCalendar()).thenReturn(calendar2);
        when(calendar2Usage.overlaps(this.period)).thenReturn(true);
        when(calendar2Usage.getRange()).thenReturn(Range.atLeast(JULY_1ST_2016));
        when(this.usagePoint.getTimeOfUseCalendarUsages()).thenReturn(Arrays.asList(calendar1Usage, calendar2Usage));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(4);
        assertThat(meterActivationSets.get(0).getRange()).isEqualTo(Range.closedOpen(MAY_2ND_2016, JUNE_1ST_2016));
        assertThat(meterActivationSets.get(0).getCalendar()).isNull();
        assertThat(meterActivationSets.get(0).getMeterActivations()).containsOnly(meterActivation1);
        assertThat(meterActivationSets.get(0).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(1).getRange()).isEqualTo(Range.closedOpen(JUNE_1ST_2016, JUNE_2ND_2016));
        assertThat(meterActivationSets.get(1).getCalendar()).isEqualTo(this.calendar);
        assertThat(meterActivationSets.get(1).getMeterActivations()).containsOnly(meterActivation1);
        assertThat(meterActivationSets.get(1).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(2).getRange()).isEqualTo(Range.closedOpen(JUNE_2ND_2016, JULY_1ST_2016));
        assertThat(meterActivationSets.get(2).getCalendar()).isEqualTo(this.calendar);
        assertThat(meterActivationSets.get(2).getMeterActivations()).containsOnly(meterActivation2);
        assertThat(meterActivationSets.get(2).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isNull();
        assertThat(meterActivationSets.get(3).getRange()).isEqualTo(Range.closedOpen(JULY_1ST_2016, AUG_1ST_2016));
        assertThat(meterActivationSets.get(3).getCalendar()).isEqualTo(calendar2);
        assertThat(meterActivationSets.get(3).getMeterActivations()).containsOnly(meterActivation2);
        assertThat(meterActivationSets.get(3).getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isNull();
    }

    @Test
    public void twoMeterActivationsWithTheSamePeriod_NoCalendarUsages_NoCustomPropertySets() {
        this.period = Range.closedOpen(JUNE_1ST_2016, JULY_1ST_2016);
        this.mockRange(this.mainMeterActivation, Range.atLeast(JUNE_1ST_2016));
        when(this.mainMeterActivation.overlaps(this.period)).thenReturn(true);
        this.mockRange(this.checkMeterActivation, Range.atLeast(JUNE_1ST_2016));
        when(this.checkMeterActivation.overlaps(this.period)).thenReturn(true);
        when(this.usagePoint.getMeterActivations()).thenReturn(Arrays.asList(this.mainMeterActivation, this.checkMeterActivation));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(1);
        MeterActivationSet set = meterActivationSets.get(0);
        assertThat(set.getRange()).isEqualTo(Range.closedOpen(JUNE_1ST_2016, JULY_1ST_2016));
        assertThat(set.sequenceNumber()).isEqualTo(1);
        assertThat(set.getCalendar()).isNull();
        assertThat(set.getMeterActivations()).containsOnly(this.mainMeterActivation, this.checkMeterActivation);
        assertThat(set.getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isNull();
    }

    @Test
    public void twoMainMeterActivations_NoCalendarUsages_NoCustomPropertySets() {
        this.period = Range.closedOpen(MAY_1ST_2016, JULY_1ST_2016);
        MeterActivation mainActivation1 = mock(MeterActivation.class);
        this.mockRange(mainActivation1, Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        when(mainActivation1.overlaps(this.period)).thenReturn(true);
        MeterActivation mainActivation2 = mock(MeterActivation.class);
        this.mockRange(mainActivation2, Range.atLeast(JUNE_1ST_2016));
        when(mainActivation2.overlaps(this.period)).thenReturn(true);
        this.mockRange(this.checkMeterActivation, Range.atLeast(JUNE_1ST_2016));
        when(this.checkMeterActivation.overlaps(this.period)).thenReturn(true);
        when(this.usagePoint.getMeterActivations()).thenReturn(Arrays.asList(mainActivation1, mainActivation2, this.checkMeterActivation));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(2);
        MeterActivationSet set1 = meterActivationSets.get(0);
        assertThat(set1.getRange()).isEqualTo(Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        assertThat(set1.sequenceNumber()).isEqualTo(1);
        assertThat(set1.getMeterActivations()).containsOnly(mainActivation1);
        assertThat(set1.getCalendar()).isNull();
        assertThat(set1.getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isNull();
        MeterActivationSet set2 = meterActivationSets.get(1);
        assertThat(set2.getRange()).isEqualTo(Range.closedOpen(JUNE_1ST_2016, JULY_1ST_2016));
        assertThat(set2.sequenceNumber()).isEqualTo(2);
        assertThat(set2.getMeterActivations()).containsOnly(mainActivation2, this.checkMeterActivation);
        assertThat(set2.getCalendar()).isNull();
        assertThat(set2.getSyntheticLoadProfile(SLP1_PROPERTY_NAME)).isNull();
    }

    @Test
    public void twoMainMeterActivationsAndCheckThatClosesHalfwayThrough2ndMainActivation() {
        this.period = Range.closedOpen(MAY_1ST_2016, JULY_1ST_2016);
        MeterActivation mainActivation1 = mock(MeterActivation.class);
        this.mockRange(mainActivation1, Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        when(mainActivation1.overlaps(this.period)).thenReturn(true);
        MeterActivation mainActivation2 = mock(MeterActivation.class);
        this.mockRange(mainActivation2, Range.atLeast(JUNE_1ST_2016));
        when(mainActivation2.overlaps(this.period)).thenReturn(true);
        this.mockRange(this.checkMeterActivation, Range.closedOpen(JUNE_1ST_2016, JUNE_2ND_2016));
        when(this.checkMeterActivation.overlaps(this.period)).thenReturn(true);
        when(this.usagePoint.getMeterActivations()).thenReturn(Arrays.asList(mainActivation1, mainActivation2, this.checkMeterActivation));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(3);
        MeterActivationSet set1 = meterActivationSets.get(0);
        assertThat(set1.getRange()).isEqualTo(Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        assertThat(set1.sequenceNumber()).isEqualTo(1);
        assertThat(set1.getCalendar()).isNull();
        MeterActivationSet set2 = meterActivationSets.get(1);
        assertThat(set2.getRange()).isEqualTo(Range.closedOpen(JUNE_1ST_2016, JUNE_2ND_2016));
        assertThat(set2.sequenceNumber()).isEqualTo(2);
        assertThat(set2.getCalendar()).isNull();
        MeterActivationSet set3 = meterActivationSets.get(2);
        assertThat(set3.getRange()).isEqualTo(Range.closedOpen(JUNE_2ND_2016, JULY_1ST_2016));
        assertThat(set3.sequenceNumber()).isEqualTo(3);
        assertThat(set3.getCalendar()).isNull();
    }

    @Test
    public void twoMeterActivationsWithGapInBetween() {
        this.period = Range.closedOpen(MAY_1ST_2016, AUG_1ST_2016);
        MeterActivation ma1 = mock(MeterActivation.class);
        this.mockRange(ma1, Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        when(ma1.overlaps(this.period)).thenReturn(true);
        MeterActivation ma2 = mock(MeterActivation.class);
        this.mockRange(ma2, Range.atLeast(JULY_1ST_2016));
        when(ma2.overlaps(this.period)).thenReturn(true);
        when(this.usagePoint.getMeterActivations()).thenReturn(Arrays.asList(ma1, ma2));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(2);
        MeterActivationSet set1 = meterActivationSets.get(0);
        assertThat(set1.getRange()).isEqualTo(Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        assertThat(set1.sequenceNumber()).isEqualTo(1);
        assertThat(set1.getCalendar()).isNull();
        MeterActivationSet set2 = meterActivationSets.get(1);
        assertThat(set2.getRange()).isEqualTo(Range.closedOpen(JULY_1ST_2016, AUG_1ST_2016));
        assertThat(set2.sequenceNumber()).isEqualTo(2);
        assertThat(set2.getCalendar()).isNull();
    }

    @Test
    public void twoMeterActivationsWithGapInBetweenAndRequestedPeriodThatStartsAtEndOfFirstMeterActivation() {
        this.period = Range.closedOpen(JUNE_1ST_2016, AUG_1ST_2016);
        MeterActivation ma1 = mock(MeterActivation.class);
        this.mockRange(ma1, Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        when(ma1.overlaps(this.period)).thenReturn(false);
        MeterActivation ma2 = mock(MeterActivation.class);
        this.mockRange(ma2, Range.atLeast(JULY_1ST_2016));
        when(ma2.overlaps(this.period)).thenReturn(true);
        when(this.usagePoint.getMeterActivations()).thenReturn(Arrays.asList(ma1, ma2));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(1);
        MeterActivationSet set1 = meterActivationSets.get(0);
        assertThat(set1.getRange()).isEqualTo(Range.closedOpen(JULY_1ST_2016, AUG_1ST_2016));
        assertThat(set1.sequenceNumber()).isEqualTo(1);
        assertThat(set1.getCalendar()).isNull();
    }

    @Test
    public void twoClosedMeterActivationsWithGapInBetween() {
        this.period = Range.closedOpen(MAY_1ST_2016, AUG_1ST_2016);
        MeterActivation ma1 = mock(MeterActivation.class);
        this.mockRange(ma1, Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        when(ma1.overlaps(this.period)).thenReturn(true);
        when(ma1.overlaps(Range.closed(MAY_1ST_2016, JUNE_1ST_2016))).thenReturn(true);
        MeterActivation ma2 = mock(MeterActivation.class);
        this.mockRange(ma2, Range.closedOpen(JULY_1ST_2016, AUG_1ST_2016));
        when(ma2.overlaps(this.period)).thenReturn(true);
        when(ma1.overlaps(Range.closed(JULY_1ST_2016, AUG_1ST_2016))).thenReturn(true);
        when(this.usagePoint.getMeterActivations()).thenReturn(Arrays.asList(ma1, ma2));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(2);
        MeterActivationSet set1 = meterActivationSets.get(0);
        assertThat(set1.getRange()).isEqualTo(Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        assertThat(set1.sequenceNumber()).isEqualTo(1);
        assertThat(set1.getCalendar()).isNull();
        MeterActivationSet set2 = meterActivationSets.get(1);
        assertThat(set2.getRange()).isEqualTo(Range.closedOpen(JULY_1ST_2016, AUG_1ST_2016));
        assertThat(set2.sequenceNumber()).isEqualTo(2);
        assertThat(set2.getCalendar()).isNull();
    }

    private MeterActivationSetBuilder getTestInstance() {
        return new MeterActivationSetBuilder(this.customPropertySetService, this.usagePoint, this.period);
    }

    private void mockRange(MeterActivation meterActivation, Range<Instant> range) {
        when(meterActivation.getRange()).thenReturn(range);
        when(meterActivation.getStart()).thenReturn(range.lowerEndpoint());
        if (range.hasUpperBound()) {
            when(meterActivation.getEnd()).thenReturn(range.upperEndpoint());
        } else {
            when(meterActivation.getEnd()).thenReturn(null);
        }
    }

}