/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.util.time.DayMonthTime;

import org.osgi.framework.BundleContext;

import java.time.Month;
import java.time.MonthDay;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link GasDayOptionsCreator} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-15 (16:42)
 */
@RunWith(MockitoJUnitRunner.class)
public class GasDayOptionsCreatorTest {

    @Mock
    private ServerMeteringService meteringService;
    @Mock
    private BundleContext bundleContext;

    @Before
    public void initializeMocks() {
        when(this.bundleContext.getProperty(GasDayOptionsCreator.GAS_DAY_START_PROPERTY_NAME)).thenReturn("OCT@05AM");
    }

    @Test
    public void createsIfMissing() {
        when(this.meteringService.getGasDayOptions()).thenReturn(Optional.empty());

        // Business method
        this.getTestInstance().createIfMissing(this.bundleContext);

        // Asserts
        verify(this.meteringService).getGasDayOptions();
        ArgumentCaptor<DayMonthTime> argumentCaptor = ArgumentCaptor.forClass(DayMonthTime.class);
        verify(this.meteringService).createGasDayOptions(argumentCaptor.capture());
        DayMonthTime dayMonthTime = argumentCaptor.getValue();
        assertThat(dayMonthTime).isNotNull();
        assertThat(dayMonthTime.getMonthValue()).isEqualTo(10);
        assertThat(dayMonthTime.getDayOfMonth()).isEqualTo(1);
        assertThat(dayMonthTime.getHour()).isEqualTo(5);
    }

    @Test
    public void doesNotCreateIfAlreadyExists() {
        GasDayOptions gasDayOptions = mock(GasDayOptions.class);
        when(gasDayOptions.getYearStart()).thenReturn(DayMonthTime.fromMidnight(MonthDay.of(Month.OCTOBER, 1)));
        when(this.meteringService.getGasDayOptions()).thenReturn(Optional.of(gasDayOptions));

        // Business method
        this.getTestInstance().createIfMissing(this.bundleContext);

        // Asserts
        verify(this.meteringService).getGasDayOptions();
        verify(this.meteringService, never()).createGasDayOptions(any(DayMonthTime.class));
    }

    @Test
    public void ignoresIfNotConfigured() {
        when(this.bundleContext.getProperty(GasDayOptionsCreator.GAS_DAY_START_PROPERTY_NAME)).thenReturn(null);
        when(this.meteringService.getGasDayOptions()).thenReturn(Optional.empty());

        // Business method
        this.getTestInstance().createIfMissing(this.bundleContext);

        // Asserts
        verify(this.meteringService).getGasDayOptions();
        verify(this.meteringService, never()).createGasDayOptions(any(DayMonthTime.class));
    }

    @Test
    public void ignoresIfNotConfiguredCorrectly() {
        when(this.bundleContext.getProperty(GasDayOptionsCreator.GAS_DAY_START_PROPERTY_NAME)).thenReturn("Not.the.expected.format");
        when(this.meteringService.getGasDayOptions()).thenReturn(Optional.empty());

        // Business method
        this.getTestInstance().createIfMissing(this.bundleContext);

        // Asserts
        verify(this.meteringService).getGasDayOptions();
        verify(this.meteringService, never()).createGasDayOptions(any(DayMonthTime.class));
    }

    private GasDayOptionsCreator getTestInstance() {
        return new GasDayOptionsCreator(this.meteringService);
    }

}