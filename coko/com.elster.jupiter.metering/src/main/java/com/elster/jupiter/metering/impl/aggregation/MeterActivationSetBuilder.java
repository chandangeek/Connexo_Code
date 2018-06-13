/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.ServerCalendarUsage;
import com.elster.jupiter.metering.impl.ServerUsagePoint;
import com.elster.jupiter.metering.slp.SyntheticLoadProfile;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.streams.Predicates;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builds {@link MeterActivationSet}s for a {@link UsagePoint} and a data aggregation period.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-06-09 (21:22)
 */
class MeterActivationSetBuilder {

    private final CustomPropertySetService customPropertySetService;
    private final ServerUsagePoint usagePoint;
    private final Range<Instant> period;
    private final UsagePointMetrologyConfiguration metrologyConfiguration;
    private final Set<CustomPropertySet<UsagePoint, ?>> customPropertySets;
    private int sequenceNumber;
    private MeterActivationSet last;

    MeterActivationSetBuilder(CustomPropertySetService customPropertySetService, ServerUsagePoint usagePoint, Range<Instant> period) {
        this.customPropertySetService = customPropertySetService;
        this.usagePoint = usagePoint;
        this.period = period;
        this.metrologyConfiguration = this.usagePoint.getEffectiveMetrologyConfiguration(this.period.lowerEndpoint()).get().getMetrologyConfiguration();
        this.customPropertySets = new HashSet<>();
        this.getVersionedCustomPropertySets().forEach(this.customPropertySets::add);
        this.getNonVersionedCustomPropertySets().forEach(this.customPropertySets::add);
    }

    MeterActivationSetBuilder(CustomPropertySetService customPropertySetService, ServerUsagePoint usagePoint, Instant when) {
        this(customPropertySetService, usagePoint, Range.singleton(when));
    }

    private Set<CustomPropertySet<UsagePoint, ?>> getVersionedCustomPropertySets() {
        return this.getCustomPropertySets(CustomPropertySet::isVersioned);
    }

    private Set<CustomPropertySet<UsagePoint, ?>> getNonVersionedCustomPropertySets() {
        return this.getCustomPropertySets(Predicates.not(CustomPropertySet::isVersioned));
    }

    private Set<CustomPropertySet<UsagePoint, ?>> getVersionedCustomPropertySetsWithSLPProperties() {
        return this.getCustomPropertySets(cps -> cps.isVersioned() && this.hasSLPProperty(cps));
    }

    private Set<CustomPropertySet<UsagePoint, ?>> getNonVersionedCustomPropertySetsWithSLPProperties() {
        return this.getCustomPropertySets(cps -> !cps.isVersioned() && this.hasSLPProperty(cps));
    }

    private Set<CustomPropertySet<UsagePoint, ?>> getCustomPropertySets(Predicate<CustomPropertySet> filter) {
        Set<CustomPropertySet<UsagePoint, ?>> customPropertySets = new HashSet<>();
        this.addCustomPropertySets(customPropertySets, this.usagePoint.getServiceCategory().getCustomPropertySets(), filter);
        this.addCustomPropertySets(customPropertySets, this.metrologyConfiguration.getCustomPropertySets(), filter);
        return customPropertySets;
    }

    private void addCustomPropertySets(Set<CustomPropertySet<UsagePoint, ?>> customPropertySets, Collection<RegisteredCustomPropertySet> registered, Predicate<CustomPropertySet> filter) {
        registered
                .stream()
                .map(RegisteredCustomPropertySet::getCustomPropertySet)
                .filter(Objects::nonNull)   // Remove the once that were not available on the classpath, which may not be a good idea but it does produce weird and ureadable exceptions
                .filter(filter)
                .forEach(customPropertySets::add);
    }

    private boolean hasSLPProperty(CustomPropertySet customPropertySet) {
        return this.syntheticLoadProfileSpecs(customPropertySet).count() > 0;
    }

    @SuppressWarnings("unchecked")
    private Stream<PropertySpec> syntheticLoadProfileSpecs(CustomPropertySet customPropertySet) {
        return customPropertySet
                .getPropertySpecs()
                .stream()
                .filter(propertySpec -> this.isSLPProperty((PropertySpec) propertySpec));
    }

    @SuppressWarnings("unchecked")
    private boolean isSLPProperty(PropertySpec propertySpec) {
        return propertySpec.isReference() && propertySpec.getValueFactory().getValueType().isAssignableFrom(SyntheticLoadProfile.class);
    }

    List<MeterActivationSet> build() {
        this.sequenceNumber = 0;
        Stream.Builder<Instant> builder = Stream.builder();
        this.getOverlappingMeterActivations().flatMap(this::switchTimestamps).forEach(builder::add);
        this.getOverlappingCalendarUsages().flatMap(this::switchTimestamps).forEach(builder::add);
        this.customPropertySets.forEach(cps -> this.getOverlappingProperties(cps).forEach(builder::add));
        return builder
                .build()
                .distinct()
                .sorted()
                .map(this::createMeterActivationSet)
                .flatMap(Functions.asStream())
                .collect(Collectors.toList());
    }

    private Stream<MeterActivation> getOverlappingMeterActivations() {
        return this.getOverlappingMeterActivations(this.period);
    }

    private Stream<MeterActivation> getOverlappingMeterActivations(Range<Instant> period) {
        return this.usagePoint.getMeterActivations().stream().filter(each -> each.overlaps(period));
    }

    private Stream<Instant> switchTimestamps(MeterActivation meterActivation) {
        return this.switchTimestampsFromRange(this.period.intersection(meterActivation.getRange()));
    }

    private Stream<ServerCalendarUsage> getOverlappingCalendarUsages() {
        return this.getOverlappingCalendarUsages(this.period);
    }

    private Stream<ServerCalendarUsage> getOverlappingCalendarUsages(Range<Instant> period) {
        return this.usagePoint.getTimeOfUseCalendarUsages().stream().filter(each -> each.overlaps(period));
    }

    private Stream<Instant> switchTimestamps(ServerCalendarUsage calendarUsage) {
        return this.switchTimestampsFromRange(this.period.intersection(calendarUsage.getRange()));
    }

    private Stream<Instant> getOverlappingProperties(CustomPropertySet<UsagePoint, ?> customPropertySet) {
        List<CustomPropertySetValues> allValues;
        if (customPropertySet.isVersioned()) {
            allValues = this.customPropertySetService.getAllVersionedValuesFor(customPropertySet, this.usagePoint);
        } else {
            allValues = Collections.singletonList(this.customPropertySetService.getUniqueValuesFor(customPropertySet, this.usagePoint));
        }
        return allValues
                .stream()
                .map(CustomPropertySetValues::getEffectiveRange)
                .filter(effectiveRange -> this.areOverlapping(effectiveRange, this.period))
                .map(this.period::intersection)
                .flatMap(this::switchTimestampsFromRange);
    }

    private boolean areOverlapping(Range<Instant> r1, Range<Instant> r2) {
        return !ImmutableRangeSet.of(r1).subRangeSet(r2).isEmpty();
    }

    private Stream<Instant> switchTimestampsFromRange(Range<Instant> range) {
        Stream.Builder<Instant> builder = Stream.builder();
        builder.add(range.lowerEndpoint());
        if (range.hasUpperBound()) {
            builder.add(range.upperEndpoint());
        }
        return builder.build();
    }

    /**
     * Creates a {@link MeterActivationSet} for the instant in time
     * where a switch in {@link MeterActivation}, {@link com.elster.jupiter.metering.UsagePoint.CalendarUsage}
     * or {@link SyntheticLoadProfile} usage has occurred.
     * If the switching timestamp relates to closing of all related activation periods,
     * i.e. closing of MeterActivation, closing of CalendarUsage and closing of
     * SyntheticLoadProfile custom property set value then <code>Optional.empty()</code> is returned.
     *
     * @param switchTimestamp The point in time where the switching occurred
     * @return The MeterActivationSet or <code>Optional.empty()</code> as described above
     */
    private Optional<MeterActivationSet> createMeterActivationSet(Instant switchTimestamp) {
        if ((this.period.hasUpperBound() && switchTimestamp.equals(this.period.upperEndpoint()) &&
                !(this.period.hasLowerBound() && switchTimestamp.equals(this.period.lowerEndpoint())))) {
            return Optional.empty();
        } else {
            List<MeterActivation> meterActivations = this.getOverlappingMeterActivations()
                    .filter(meterActivation -> meterActivation.getRange().contains(switchTimestamp))
                    .collect(Collectors.toList());
            Optional<ServerCalendarUsage> calendarUsage = this.getOverlappingCalendarUsages().filter(each -> each.getRange().contains(switchTimestamp)).findAny();
            Optional<SyntheticLoadProfileUsage> syntheticLoadProfileUsage = this.getSyntheticLoadProfileUsage(switchTimestamp);
            List<CustomPropertySetValues> allCustomPropertyValues = this.getCustomPropertyValues(switchTimestamp);
            if (meterActivations.isEmpty() && !calendarUsage.isPresent() && !syntheticLoadProfileUsage.isPresent() && allCustomPropertyValues.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(this.createMeterActivationSet(switchTimestamp, meterActivations, calendarUsage, syntheticLoadProfileUsage));
            }
        }
    }

    private List<CustomPropertySetValues> getCustomPropertyValues(Instant switchTimestamp) {
        return this.getVersionedCustomPropertySets()
                    .stream()
                    .map(customPropertySet -> this.customPropertySetService.getUniqueValuesFor(customPropertySet, this.usagePoint, switchTimestamp))
                    .filter(Predicates.not(CustomPropertySetValues::isEmpty))
                    .collect(Collectors.toList());
    }

    private Optional<SyntheticLoadProfileUsage> getSyntheticLoadProfileUsage(Instant switchTimestamp) {
        SyntheticLoadProfileUsage.Builder syntheticLoadProfileUsageBuilder = SyntheticLoadProfileUsage.builder(this.period);
        this.getVersionedCustomPropertySetsWithSLPProperties()
                .forEach(customPropertySet -> this.buildFromVersionedSyntheticLoadProfiles(syntheticLoadProfileUsageBuilder, customPropertySet, switchTimestamp));
        this.getNonVersionedCustomPropertySetsWithSLPProperties()
                .forEach(customPropertySet -> this.builderFromNonVersionedSyntheticLoadProfiles(syntheticLoadProfileUsageBuilder, customPropertySet));
        return syntheticLoadProfileUsageBuilder.build();
    }

    private void buildFromVersionedSyntheticLoadProfiles(SyntheticLoadProfileUsage.Builder builder, CustomPropertySet<UsagePoint, ?> customPropertySet, Instant startDate) {
        CustomPropertySetValues values = this.customPropertySetService.getUniqueValuesFor(customPropertySet, this.usagePoint, startDate);
        this.addSyntheticLoadProfileProperties(builder, values.getEffectiveRange(), customPropertySet, values);
    }

    private void builderFromNonVersionedSyntheticLoadProfiles(SyntheticLoadProfileUsage.Builder builder, CustomPropertySet<UsagePoint, ?> customPropertySet) {
        CustomPropertySetValues values = this.customPropertySetService.getUniqueValuesFor(customPropertySet, this.usagePoint);
        this.addSyntheticLoadProfileProperties(builder, this.period, customPropertySet, values);
    }

    private void addSyntheticLoadProfileProperties(SyntheticLoadProfileUsage.Builder builder, Range<Instant> effectiveRange, CustomPropertySet<UsagePoint, ?> customPropertySet, CustomPropertySetValues values) {
        builder.setRange(effectiveRange);
        this.syntheticLoadProfileSpecs(customPropertySet)
                .forEach(propertySpec -> builder.add(propertySpec.getName(), (SyntheticLoadProfile) values.getProperty(propertySpec.getName())));
    }

    private MeterActivationSet createMeterActivationSet(Instant switchTimestamp, List<MeterActivation> meterActivations, Optional<ServerCalendarUsage> calendarUsage, Optional<SyntheticLoadProfileUsage> syntheticLoadProfileUsage) {
        this.sequenceNumber++;
        MeterActivationSetImpl set = new MeterActivationSetImpl(this.usagePoint, this.metrologyConfiguration, this.sequenceNumber, this.period, switchTimestamp);
        meterActivations.forEach(set::add);
        calendarUsage.ifPresent(set::setCalendar);
        syntheticLoadProfileUsage.ifPresent(set::addSyntheticLoadProfile);
        Loggers.ANALYSIS.debug(() -> new DataAggregationAnalysisLogger().meterActivationSetCreated(set));
        if (this.last != null) {
            this.last.avoidOverlapWith(set);
        }
        this.last = set;
        return set;
    }

}
