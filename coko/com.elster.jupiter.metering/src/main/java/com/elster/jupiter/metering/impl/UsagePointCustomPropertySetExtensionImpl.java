package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointCustomPropertySetValuesManageException;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.metering.UsagePointVersionedPropertySet;
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
import java.util.Set;
import java.util.stream.Collectors;

public class UsagePointCustomPropertySetExtensionImpl implements UsagePointCustomPropertySetExtension {
    private final Clock clock;
    private final CustomPropertySetService customPropertySetService;
    private final Thesaurus thesaurus;
    private final UsagePointImpl usagePoint;

    private List<UsagePointPropertySet> allProperties;

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

    @Override
    public List<UsagePointPropertySet> getPropertySetsOnMetrologyConfiguration() {
        Optional<MetrologyConfiguration> metrologyConfiguration = getMetrologyConfiguration();
        if (metrologyConfiguration.isPresent()) {
            return metrologyConfiguration.get().getCustomPropertySets()
                    .stream()
                    .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                    .map(rcps -> rcps.getCustomPropertySet().isVersioned() ? new UsagePointVersionedPropertySetImpl(rcps) : new UsagePointPropertySetImpl(rcps))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<UsagePointPropertySet> getPropertySetsOnServiceCategory() {
        return getUsagePoint().getServiceCategory().getCustomPropertySets()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .map(rcps -> rcps.getCustomPropertySet().isVersioned() ? new UsagePointVersionedPropertySetImpl(rcps) : new UsagePointPropertySetImpl(rcps))
                .collect(Collectors.toList());
    }

    @Override
    public List<UsagePointPropertySet> getAllPropertySets() {
        if (this.allProperties == null) {
            List<UsagePointPropertySet> mcCPS = getPropertySetsOnMetrologyConfiguration();
            List<UsagePointPropertySet> scCPS = getPropertySetsOnServiceCategory();
            this.allProperties = new ArrayList<>(mcCPS.size() + scCPS.size());
            this.allProperties.addAll(mcCPS);
            this.allProperties.addAll(scCPS);
        }
        return this.allProperties;
    }

    @Override
    public UsagePointPropertySet getPropertySet(long registeredCustomPropertySetId) {
        return getAllPropertySets()
                .stream()
                .filter(rcps -> rcps.getId() == registeredCustomPropertySetId)
                .findFirst()
                .orElseThrow(() -> UsagePointCustomPropertySetValuesManageException
                        .noLinkedCustomPropertySet(thesaurus, "id = " + String.valueOf(registeredCustomPropertySetId)));
    }

    @Override
    public UsagePointVersionedPropertySet getVersionedPropertySet(long registeredCustomPropertySetId) {
        UsagePointPropertySet property = getPropertySet(registeredCustomPropertySetId);
        if (!property.getCustomPropertySet().isVersioned()) {
            throw UsagePointCustomPropertySetValuesManageException
                    .customPropertySetIsNotVersioned(thesaurus, property.getCustomPropertySet().getName());
        }
        return (UsagePointVersionedPropertySet) property;
    }

    public class UsagePointPropertySetImpl implements UsagePointPropertySet {

        private final RegisteredCustomPropertySet delegate;

        public UsagePointPropertySetImpl(RegisteredCustomPropertySet rcps) {
            if (!UsagePoint.class.isAssignableFrom(rcps.getCustomPropertySet().getDomainClass())) {
                throw UsagePointCustomPropertySetValuesManageException
                        .badDomainType(thesaurus, rcps.getCustomPropertySet().getName());
            }
            this.delegate = rcps;
        }

        @Override
        public long getId() {
            return delegate.getId();
        }

        @Override
        @SuppressWarnings("uncheked")
        public CustomPropertySet<UsagePoint, ?> getCustomPropertySet() {
            return delegate.getCustomPropertySet();
        }

        @Override
        public Set<ViewPrivilege> getViewPrivileges() {
            return delegate.getViewPrivileges();
        }

        @Override
        public Set<EditPrivilege> getEditPrivileges() {
            return delegate.getEditPrivileges();
        }

        @Override
        public boolean isEditableByCurrentUser() {
            return delegate.isEditableByCurrentUser();
        }

        @Override
        public boolean isViewableByCurrentUser() {
            return delegate.isViewableByCurrentUser();
        }

        @Override
        public void updatePrivileges(Set<ViewPrivilege> viewPrivileges, Set<EditPrivilege> editPrivileges) {
            delegate.updatePrivileges(viewPrivileges, editPrivileges);
        }

        @Override
        public UsagePoint getUsagePoint() {
            return usagePoint;
        }

        @Override
        public CustomPropertySetValues getValues() {
            return customPropertySetService.getUniqueValuesFor(getCustomPropertySet(), getUsagePoint());
        }

        @Override
        public void setValues(CustomPropertySetValues values) {
            if (!isEditableByCurrentUser()) {
                throw UsagePointCustomPropertySetValuesManageException
                        .customPropertySetIsNotEditableByUser(thesaurus, getCustomPropertySet().getName());
            }
            customPropertySetService.setValuesFor(getCustomPropertySet(), getUsagePoint(), values);
            usagePoint.touch();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            UsagePointPropertySetImpl that = (UsagePointPropertySetImpl) o;
            return !(delegate != null ? !delegate.equals(that.delegate) : that.delegate != null);
        }

        @Override
        public int hashCode() {
            return this.delegate.hashCode();
        }
    }

    public class UsagePointVersionedPropertySetImpl extends UsagePointPropertySetImpl implements UsagePointVersionedPropertySet {

        public UsagePointVersionedPropertySetImpl(RegisteredCustomPropertySet rcps) {
            super(rcps);
            if (!getCustomPropertySet().isVersioned()) {
                throw UsagePointCustomPropertySetValuesManageException
                        .customPropertySetIsNotVersioned(thesaurus, rcps.getCustomPropertySet().getName());
            }
        }

        @Override
        public CustomPropertySetValues getValues() {
            return getVersionValues(clock.instant());
        }

        @Override
        public void setValues(CustomPropertySetValues values) {
            setVersionValues(clock.instant(), values);
        }

        @Override
        public CustomPropertySetValues getVersionValues(Instant anyTimeInVersionInterval) {
            CustomPropertySetValues values = customPropertySetService.getUniqueValuesFor(getCustomPropertySet(), getUsagePoint(), anyTimeInVersionInterval);
            if (values.isEmpty()) {
                // empty values with infinity range => no active version
                return null;
            }
            return values;
        }

        @Override
        public void setVersionValues(Instant anyTimeInVersionInterval, CustomPropertySetValues values) {
            if (!isEditableByCurrentUser()) {
                throw UsagePointCustomPropertySetValuesManageException
                        .customPropertySetIsNotEditableByUser(thesaurus, getCustomPropertySet().getName());
            }
            if (anyTimeInVersionInterval == null) {
                // create new version
                customPropertySetService.setValuesVersionFor(getCustomPropertySet(), getUsagePoint(), values, values.getEffectiveRange());
            } else {
                // update existing version
                customPropertySetService.setValuesVersionFor(getCustomPropertySet(), getUsagePoint(), values, values.getEffectiveRange(), anyTimeInVersionInterval);
            }
            usagePoint.touch();
        }

        @Override
        public List<CustomPropertySetValues> getAllVersionValues() {
            return customPropertySetService.getAllVersionedValuesFor(getCustomPropertySet(), getUsagePoint());
        }

        @Override
        public Range<Instant> getNewVersionInterval() {
            Instant now = clock.instant();
            List<Range<Instant>> versionIntervals = getCustomPropertySetValuesIntervals(getCustomPropertySet());
            // Do we have an active version now?
            Optional<?> currentVersionValue = customPropertySetService.getUniqueValuesEntityFor(getCustomPropertySet(), getUsagePoint(), now);
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
                if (latestVersion.hasUpperBound()) {
                    return Range.atLeast(latestVersion.upperEndpoint());
                }
            }
            // return the default interval
            return Range.all();
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
}
