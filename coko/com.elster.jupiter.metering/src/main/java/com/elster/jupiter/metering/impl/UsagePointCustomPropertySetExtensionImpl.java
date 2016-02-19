package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointCustomPropertySetValuesManageException;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.RangeInstantComparator;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UsagePointCustomPropertySetExtensionImpl implements UsagePointCustomPropertySetExtension {
    private final Clock clock;
    private final CustomPropertySetService customPropertySetService;
    private final Thesaurus thesaurus;
    private final UsagePointImpl usagePoint;

    @Inject
    public UsagePointCustomPropertySetExtensionImpl(Clock clock,
                                                    CustomPropertySetService customPropertySetService,
                                                    Thesaurus thesaurus,
                                                    UsagePointImpl usagePoint) {
        this.clock = clock;
        this.customPropertySetService = customPropertySetService;
        this.thesaurus = thesaurus;
        this.usagePoint = usagePoint;
    }

    @Override
    public UsagePoint getUsagePoint() {
        return this.usagePoint;
    }

    private Optional<MetrologyConfiguration> getMetrologyConfiguration() {
        return getUsagePoint().getMetrologyConfiguration();
    }

    private void validateCustomPropertySetDomain(RegisteredCustomPropertySet registeredCustomPropertySet) {
        if (!UsagePoint.class.isAssignableFrom(registeredCustomPropertySet.getCustomPropertySet().getDomainClass())) {
            throw UsagePointCustomPropertySetValuesManageException
                    .badDomainType(this.thesaurus, registeredCustomPropertySet.getCustomPropertySet().getName());
        }
    }

    @SuppressWarnings("unchecked")
    private CustomPropertySetValues getCustomPropertySetValueWithoutChecks(RegisteredCustomPropertySet rcps,
                                                                           Instant effectiveTimeStamp) {
        CustomPropertySet<UsagePoint, ?> customPropertySet = rcps.getCustomPropertySet();
        CustomPropertySetValues values;
        if (customPropertySet.isVersioned()) {
            values = this.customPropertySetService.getUniqueValuesFor(customPropertySet, getUsagePoint(), effectiveTimeStamp);
            if (values.isEmpty() && !this.customPropertySetService
                    .getUniqueValuesEntityFor(customPropertySet, getUsagePoint(), effectiveTimeStamp).isPresent()) {
                values = null;
            }
        } else {
            values = this.customPropertySetService.getUniqueValuesFor(customPropertySet, getUsagePoint());
        }
        return values;
    }

    private void setCustomPropertySetValuesWithoutChecks(CustomPropertySet<UsagePoint, ?> customPropertySet,
                                                         CustomPropertySetValues customPropertySetValue) {
        if (!customPropertySet.isVersioned()) {
            this.customPropertySetService.setValuesFor(customPropertySet, getUsagePoint(), customPropertySetValue);
        } else {
            Range<Instant> effectiveRange = customPropertySetValue.getEffectiveRange();
            if (!effectiveRange.hasLowerBound() && !effectiveRange.hasUpperBound()) {
                // Fallback case
                effectiveRange = getCurrentIntervalInternal(customPropertySet);
            }
            this.customPropertySetService.setValuesVersionFor(customPropertySet, getUsagePoint(), customPropertySetValue, effectiveRange);
        }
        this.usagePoint.touch();
    }

    @Override
    public List<RegisteredCustomPropertySet> getCustomPropertySetsOnMetrologyConfiguration() {
        Optional<MetrologyConfiguration> metrologyConfiguration = getMetrologyConfiguration();
        if (metrologyConfiguration.isPresent()) {
            return metrologyConfiguration.get().getCustomPropertySets();
        }
        return Collections.emptyList();
    }

    @Override
    public List<RegisteredCustomPropertySet> getCustomPropertySetsOnServiceCategory() {
        return getUsagePoint().getServiceCategory().getCustomPropertySets();
    }

    @Override
    public List<RegisteredCustomPropertySet> getAllCustomPropertySets() {
        List<RegisteredCustomPropertySet> mcCPS = getCustomPropertySetsOnMetrologyConfiguration();
        List<RegisteredCustomPropertySet> scCPS = getCustomPropertySetsOnServiceCategory();
        List<RegisteredCustomPropertySet> all = new ArrayList<>(mcCPS.size() + scCPS.size());
        all.addAll(mcCPS);
        all.addAll(scCPS);
        return all;
    }

    @Override
    public CustomPropertySetValues getCustomPropertySetValue(RegisteredCustomPropertySet registeredCustomPropertySet) {
        return getCustomPropertySetValue(registeredCustomPropertySet, this.clock.instant());
    }

    @Override
    public CustomPropertySetValues getCustomPropertySetValue(RegisteredCustomPropertySet registeredCustomPropertySet, Instant effectiveTimeStamp) {
        if (effectiveTimeStamp == null) {
            effectiveTimeStamp = this.clock.instant();
        }
        if (registeredCustomPropertySet != null) {
            validateCustomPropertySetDomain(registeredCustomPropertySet);
            return getCustomPropertySetValueWithoutChecks(registeredCustomPropertySet, effectiveTimeStamp);
        }
        return null;
    }

    @Override
    public void setCustomPropertySetValue(CustomPropertySet customPropertySet, CustomPropertySetValues customPropertySetValue) {
        // TODO maybe it will be better to cache all sets here
        RegisteredCustomPropertySet registeredCustomPropertySet = getAllCustomPropertySets()
                .stream()
                .filter(rcps -> rcps.getCustomPropertySet().getId().equals(customPropertySet.getId()))
                .findAny()
                .orElseThrow(() -> UsagePointCustomPropertySetValuesManageException
                        .noLinkedCustomPropertySet(this.thesaurus, customPropertySet.getName()));
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw UsagePointCustomPropertySetValuesManageException
                    .customPropertySetIsNotEditableByUser(this.thesaurus, customPropertySet.getName());
        }
        setCustomPropertySetValuesWithoutChecks(customPropertySet, customPropertySetValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Range<Instant> getCurrentInterval(RegisteredCustomPropertySet registeredCustomPropertySet) {
        if (registeredCustomPropertySet != null) {
            validateCustomPropertySetDomain(registeredCustomPropertySet);
            CustomPropertySet<UsagePoint, ?> customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
            return getCurrentIntervalInternal(customPropertySet);
        }
        return null;
    }

    private Range<Instant> getCurrentIntervalInternal(CustomPropertySet<UsagePoint, ?> customPropertySet) {
        if (!customPropertySet.isVersioned()) {
            throw UsagePointCustomPropertySetValuesManageException
                    .customPropertySetIsNotVersioned(this.thesaurus, customPropertySet.getName());
        }
        Instant now = this.clock.instant();
        List<Range<Instant>> versionIntervals = getCustomPropertySetValuesIntervals(customPropertySet);
        // Do we have an active version now?
        Optional<?> currentVersionValue = customPropertySetService.getUniqueValuesEntityFor(customPropertySet, getUsagePoint(), now);
        if (currentVersionValue.isPresent()) {
            // yep, we have an active version, so let's create a new version from the now till the active version end.
            return getCurrentIntervalWithActiveVersion(versionIntervals, now);
        }
        // no active version right now
        return getCurrentIntervalNoActiveVersion(versionIntervals, now);
    }

    private Range<Instant> getCurrentIntervalWithActiveVersion(List<Range<Instant>> versionIntervals, Instant currentTime) {
        Range<Instant> currentVersionRange = versionIntervals
                .stream()
                .filter(versionRange -> versionRange.contains(currentTime))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        return currentVersionRange.hasUpperBound() ? Range.closedOpen(currentTime, currentVersionRange.upperEndpoint()) : Range.atLeast(currentTime);
    }

    private Range<Instant> getCurrentIntervalNoActiveVersion(List<Range<Instant>> versionIntervals, Instant currentTime) {
        // do we have versions?
        if (!versionIntervals.isEmpty()) {
            Range<Instant> latestVersion = versionIntervals.get(0);
            Range<Instant> oldestVersion = versionIntervals.get(versionIntervals.size() - 1);
            if (latestVersion.hasUpperBound() && !latestVersion.upperEndpoint().isAfter(currentTime)) { // do we have version in the past?
                return Range.atLeast(latestVersion.upperEndpoint());
            } else if (oldestVersion.lowerEndpoint().isAfter(currentTime)) { // do we have version in the future?
                return Range.closedOpen(currentTime, latestVersion.lowerEndpoint());
            }
        }
        // return the default interval
        return Range.atLeast(currentTime);
    }

    /**
     * Returns intervals for versions for the given custom attribute set which are ordered from newest to oldest
     */
    List<Range<Instant>> getCustomPropertySetValuesIntervals(CustomPropertySet<UsagePoint, ?> customPropertySet) {
        List<CustomPropertySetValues> customPropertySetVersionValues = customPropertySetService.getAllVersionedValuesFor(customPropertySet, getUsagePoint());
        List<Range<Instant>> versionIntervals = customPropertySetVersionValues
                .stream()
                .map(CustomPropertySetValues::getEffectiveRange)
                .distinct()
                .sorted(new RangeInstantComparator())
                .collect(Collectors.toList());
        Collections.reverse(versionIntervals);
        return versionIntervals;
    }
}
