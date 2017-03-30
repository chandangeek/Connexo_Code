/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointCustomPropertySetExtensionImplTest {
    private static ZonedDateTime now = LocalDateTime.of(2016, 1, 1, 12, 0, 0).atZone(ZoneId.systemDefault());

    @Mock
    Clock clock;
    @Mock
    CustomPropertySetService customPropertySetService;
    @Mock
    UsagePointImpl usagePoint;

    UsagePointCustomPropertySetExtensionImpl.UsagePointVersionedPropertySetImpl versionedPropertySet;

    @Before
    public void before() {
        when(clock.instant()).thenReturn(now.toInstant());
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.isVersioned()).thenReturn(true);
        when(customPropertySet.getDomainClass()).thenReturn(UsagePoint.class);
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        when(registeredCustomPropertySet.getId()).thenReturn(1L);
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(registeredCustomPropertySet.isViewableByCurrentUser()).thenReturn(true);
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(serviceCategory.getCustomPropertySets()).thenReturn(Collections.singletonList(registeredCustomPropertySet));
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());
        when(usagePoint.getInstallationTime()).thenReturn(now.toInstant());
        Thesaurus thesaurus = mock(Thesaurus.class);
        versionedPropertySet = spy((UsagePointCustomPropertySetExtensionImpl.UsagePointVersionedPropertySetImpl)
                new UsagePointCustomPropertySetExtensionImpl(clock, customPropertySetService, thesaurus, usagePoint)
                        .getVersionedPropertySet(1L));
    }

    private void mockVersionIntervals(List<Range<Instant>> versionIntervals) {
        doReturn(versionIntervals).when(versionedPropertySet)
                .getCustomPropertySetValuesIntervals(any());
    }

    private void mockHasNoActiveVersion() {
        when(customPropertySetService.getUniqueValuesEntityFor(any(), eq(usagePoint), eq(now.toInstant())))
                .thenReturn(Optional.empty());
    }

    private void mockHasActiveVersion() {
        PersistentDomainExtension persistentDomainExtension = mock(PersistentDomainExtension.class);
        when(customPropertySetService.getUniqueValuesEntityFor(any(), eq(usagePoint), eq(now.toInstant())))
                .thenReturn(Optional.of(persistentDomainExtension));
    }

    @Test
    public void testGetCurrentIntervalNoVersionsAtAll() {
        mockHasNoActiveVersion();
        mockVersionIntervals(Collections.emptyList());
        Range<Instant> currentInterval = versionedPropertySet.getNewVersionInterval();

        // infinity
        assertThat(currentInterval.hasLowerBound()).isTrue();
        assertThat(currentInterval.hasUpperBound()).isFalse();
    }

    @Test
    public void testGetCurrentIntervalAndWeHaveVersionInThePast() {
        mockHasNoActiveVersion();
        Instant dayBefore = now.minus(1, ChronoUnit.DAYS).toInstant();
        mockVersionIntervals(Collections.singletonList(Range.atMost(dayBefore)));

        Range<Instant> currentInterval = versionedPropertySet.getNewVersionInterval();

        assertThat(currentInterval.hasLowerBound()).isTrue();
        assertThat(currentInterval.lowerBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(currentInterval.lowerEndpoint()).isEqualTo(dayBefore);

        assertThat(currentInterval.hasUpperBound()).isFalse();
    }

    @Test
    public void testGetCurrentIntervalAndWeHaveActiveVersion() {
        mockHasActiveVersion();
        Instant weekBefore = now.minus(1, ChronoUnit.WEEKS).toInstant();
        Instant weekAfter = now.plus(1, ChronoUnit.WEEKS).toInstant();
        mockVersionIntervals(Collections.singletonList(Range.openClosed(weekBefore, weekAfter)));

        Range<Instant> currentInterval = versionedPropertySet.getNewVersionInterval();

        assertThat(currentInterval.hasLowerBound()).isTrue();
        assertThat(currentInterval.lowerEndpoint()).isEqualTo(now.toInstant());
        assertThat(currentInterval.lowerBoundType()).isEqualTo(BoundType.CLOSED);

        assertThat(currentInterval.hasUpperBound()).isTrue();
        assertThat(currentInterval.upperEndpoint()).isEqualTo(weekAfter);
        assertThat(currentInterval.upperBoundType()).isEqualTo(BoundType.OPEN);
    }

    @Test
    public void testGetCurrentIntervalAndWeHaveVersionInFuture() {
        mockHasNoActiveVersion();
        Instant weekAfter = now.plus(1, ChronoUnit.WEEKS).toInstant();
        mockVersionIntervals(Collections.singletonList(Range.atLeast(weekAfter)));
        Range<Instant> currentInterval = versionedPropertySet.getNewVersionInterval();

        // infinity
        assertThat(currentInterval.hasLowerBound()).isTrue();
        assertThat(currentInterval.hasUpperBound()).isFalse();
    }

    @Test
    public void testGetCurrentIntervalAndWeHaveActiveVersionWithTheSameStart() {
        mockHasActiveVersion();
        Instant weekAfter = now.plus(1, ChronoUnit.WEEKS).toInstant();
        mockVersionIntervals(Collections.singletonList(Range.closedOpen(now.toInstant(), weekAfter)));

        Range<Instant> currentInterval = versionedPropertySet.getNewVersionInterval();

        assertThat(currentInterval.hasLowerBound()).isTrue();
        assertThat(currentInterval.lowerEndpoint()).isEqualTo(now.toInstant());
        assertThat(currentInterval.lowerBoundType()).isEqualTo(BoundType.CLOSED);

        assertThat(currentInterval.hasUpperBound()).isTrue();
        assertThat(currentInterval.upperEndpoint()).isEqualTo(weekAfter);
        assertThat(currentInterval.upperBoundType()).isEqualTo(BoundType.OPEN);
    }

    @Test
    public void testGetCurrentIntervalAndWeHaveActiveVersionWithTheSameEnd() {
        mockHasNoActiveVersion();
        Instant weekBefore = now.minus(1, ChronoUnit.WEEKS).toInstant();
        mockVersionIntervals(Collections.singletonList(Range.closedOpen(weekBefore, now.toInstant())));

        Range<Instant> currentInterval = versionedPropertySet.getNewVersionInterval();

        assertThat(currentInterval.hasLowerBound()).isTrue();
        assertThat(currentInterval.lowerEndpoint()).isEqualTo(now.toInstant());
        assertThat(currentInterval.lowerBoundType()).isEqualTo(BoundType.CLOSED);

        assertThat(currentInterval.hasUpperBound()).isFalse();
    }
}
