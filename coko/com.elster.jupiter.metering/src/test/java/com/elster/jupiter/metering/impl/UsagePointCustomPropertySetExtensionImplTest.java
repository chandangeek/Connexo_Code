package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    UsagePointCustomPropertySetExtensionImpl usagePointExtension;

    @Before
    public void before() {
        when(clock.instant()).thenReturn(now.toInstant());
        usagePointExtension = spy(new UsagePointCustomPropertySetExtensionImpl(clock, customPropertySetService, null, usagePoint));
    }

    @Test
    public void testGetCurrentIntervalNoVersionsAtAll() {
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getDomainClass()).thenReturn(UsagePoint.class);
        when(customPropertySet.isVersioned()).thenReturn(true);
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(customPropertySetService.getUniqueValuesEntityFor(eq(customPropertySet), eq(usagePoint), eq(now.toInstant())))
                .thenReturn(Optional.empty());
        doReturn(Collections.emptyList()).when(usagePointExtension).getCustomPropertySetValuesIntervals(eq(customPropertySet));

        Range<Instant> currentInterval = usagePointExtension.getCurrentInterval(registeredCustomPropertySet);

        assertThat(currentInterval.hasLowerBound()).isTrue();
        assertThat(currentInterval.lowerBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(currentInterval.lowerEndpoint()).isEqualTo(now.toInstant());

        assertThat(currentInterval.hasUpperBound()).isFalse();
    }

    @Test
    public void testGetCurrentIntervalAndWeHaveVersionInThePast() {
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getDomainClass()).thenReturn(UsagePoint.class);
        when(customPropertySet.isVersioned()).thenReturn(true);
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(customPropertySetService.getUniqueValuesEntityFor(eq(customPropertySet), eq(usagePoint), eq(now.toInstant())))
                .thenReturn(Optional.empty());
        Instant dayBefore = now.minus(1, ChronoUnit.DAYS).toInstant();
        doReturn(Collections.singletonList(Range.atMost(dayBefore)))
                .when(usagePointExtension).getCustomPropertySetValuesIntervals(eq(customPropertySet));

        Range<Instant> currentInterval = usagePointExtension.getCurrentInterval(registeredCustomPropertySet);

        assertThat(currentInterval.hasLowerBound()).isTrue();
        assertThat(currentInterval.lowerBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(currentInterval.lowerEndpoint()).isEqualTo(dayBefore);

        assertThat(currentInterval.hasUpperBound()).isFalse();
    }

    @Test
    public void testGetCurrentIntervalAndWeHaveActiveVersion() {
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getDomainClass()).thenReturn(UsagePoint.class);
        when(customPropertySet.isVersioned()).thenReturn(true);
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        PersistentDomainExtension persistentDomainExtension = mock(PersistentDomainExtension.class);
        when(customPropertySetService.getUniqueValuesEntityFor(eq(customPropertySet), eq(usagePoint), eq(now.toInstant())))
                .thenReturn(Optional.of(persistentDomainExtension));
        Instant weekBefore = now.minus(1, ChronoUnit.WEEKS).toInstant();
        Instant weekAfter = now.plus(1, ChronoUnit.WEEKS).toInstant();
        doReturn(Collections.singletonList(Range.openClosed(weekBefore, weekAfter)))
                .when(usagePointExtension).getCustomPropertySetValuesIntervals(eq(customPropertySet));

        Range<Instant> currentInterval = usagePointExtension.getCurrentInterval(registeredCustomPropertySet);

        assertThat(currentInterval.hasLowerBound()).isTrue();
        assertThat(currentInterval.lowerEndpoint()).isEqualTo(now.toInstant());
        assertThat(currentInterval.lowerBoundType()).isEqualTo(BoundType.CLOSED);

        assertThat(currentInterval.hasUpperBound()).isTrue();
        assertThat(currentInterval.upperEndpoint()).isEqualTo(weekAfter);
        assertThat(currentInterval.upperBoundType()).isEqualTo(BoundType.OPEN);
    }

    @Test
    public void testGetCurrentIntervalAndWeHaveVersionInFuture() {
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getDomainClass()).thenReturn(UsagePoint.class);
        when(customPropertySet.isVersioned()).thenReturn(true);
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(customPropertySetService.getUniqueValuesEntityFor(eq(customPropertySet), eq(usagePoint), eq(now.toInstant())))
                .thenReturn(Optional.empty());
        Instant weekAfter = now.plus(1, ChronoUnit.WEEKS).toInstant();
        doReturn(Collections.singletonList(Range.atLeast(weekAfter)))
                .when(usagePointExtension).getCustomPropertySetValuesIntervals(eq(customPropertySet));

        Range<Instant> currentInterval = usagePointExtension.getCurrentInterval(registeredCustomPropertySet);

        assertThat(currentInterval.hasLowerBound()).isTrue();
        assertThat(currentInterval.lowerEndpoint()).isEqualTo(now.toInstant());
        assertThat(currentInterval.lowerBoundType()).isEqualTo(BoundType.CLOSED);

        assertThat(currentInterval.hasUpperBound()).isTrue();
        assertThat(currentInterval.upperEndpoint()).isEqualTo(weekAfter);
        assertThat(currentInterval.upperBoundType()).isEqualTo(BoundType.OPEN);
    }

    @Test
    public void testGetCurrentIntervalAndWeHaveActiveVersionWithTheSameStart() {
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getDomainClass()).thenReturn(UsagePoint.class);
        when(customPropertySet.isVersioned()).thenReturn(true);
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        PersistentDomainExtension persistentDomainExtension = mock(PersistentDomainExtension.class);
        when(customPropertySetService.getUniqueValuesEntityFor(eq(customPropertySet), eq(usagePoint), eq(now.toInstant())))
                .thenReturn(Optional.of(persistentDomainExtension));
        Instant weekAfter = now.plus(1, ChronoUnit.WEEKS).toInstant();
        doReturn(Collections.singletonList(Range.closedOpen(now.toInstant(), weekAfter)))
                .when(usagePointExtension).getCustomPropertySetValuesIntervals(eq(customPropertySet));

        Range<Instant> currentInterval = usagePointExtension.getCurrentInterval(registeredCustomPropertySet);

        assertThat(currentInterval.hasLowerBound()).isTrue();
        assertThat(currentInterval.lowerEndpoint()).isEqualTo(now.toInstant());
        assertThat(currentInterval.lowerBoundType()).isEqualTo(BoundType.CLOSED);

        assertThat(currentInterval.hasUpperBound()).isTrue();
        assertThat(currentInterval.upperEndpoint()).isEqualTo(weekAfter);
        assertThat(currentInterval.upperBoundType()).isEqualTo(BoundType.OPEN);
    }

    @Test
    public void testGetCurrentIntervalAndWeHaveActiveVersionWithTheSameEnd() {
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getDomainClass()).thenReturn(UsagePoint.class);
        when(customPropertySet.isVersioned()).thenReturn(true);
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(customPropertySetService.getUniqueValuesEntityFor(eq(customPropertySet), eq(usagePoint), eq(now.toInstant())))
                .thenReturn(Optional.empty());
        Instant weekBefore = now.minus(1, ChronoUnit.WEEKS).toInstant();
        doReturn(Collections.singletonList(Range.closedOpen(weekBefore, now.toInstant())))
                .when(usagePointExtension).getCustomPropertySetValuesIntervals(eq(customPropertySet));

        Range<Instant> currentInterval = usagePointExtension.getCurrentInterval(registeredCustomPropertySet);

        assertThat(currentInterval.hasLowerBound()).isTrue();
        assertThat(currentInterval.lowerEndpoint()).isEqualTo(now.toInstant());
        assertThat(currentInterval.lowerBoundType()).isEqualTo(BoundType.CLOSED);

        assertThat(currentInterval.hasUpperBound()).isFalse();
    }
}
