/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.DayMonthTime;

import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the {@link GasDayOptionsImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-15 (13:01)
 */
public class GasDayOptionsImplIT {

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = MeteringInMemoryBootstrapModule.withAllDefaults();

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private Subscriber topicHandler;

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @After
    public void after() {
        inMemoryBootstrapModule.getOrmService()
                .invalidateCache(MeteringService.COMPONENTNAME, TableSpecs.MTR_GASDAYOPTIONS.name());
    }

    @Test
    @Transactional
    public void createGasDayOptions() {
        // Business method
        GasDayOptions gasDayOptions = inMemoryBootstrapModule.getMeteringService()
                .createGasDayOptions(DayMonthTime.from(MonthDay.of(Month.OCTOBER, 1), LocalTime.of(5, 0)));

        // Asserts
        assertThat(gasDayOptions).isNotNull();
        assertThat(gasDayOptions.getYearStart()).isNotNull();
        assertThat(gasDayOptions.getYearStart().getMonth()).isEqualTo(Month.OCTOBER);
        assertThat(gasDayOptions.getYearStart().getDayOfMonth()).isEqualTo(1);
        assertThat(gasDayOptions.getYearStart().getHour()).isEqualTo(5);
    }

    @Test
    @Transactional
    public void createGasDayIgnoresMinuteSecondsAndNanos() {
        // Business method
        GasDayOptions gasDayOptions = inMemoryBootstrapModule.getMeteringService()
                .createGasDayOptions(DayMonthTime.from(MonthDay.of(Month.OCTOBER, 1), LocalTime.of(5, 13, 31, 951753)));

        // Asserts
        assertThat(gasDayOptions).isNotNull();
        assertThat(gasDayOptions.getYearStart()).isNotNull();
        assertThat(gasDayOptions.getYearStart().getMonth()).isEqualTo(Month.OCTOBER);
        assertThat(gasDayOptions.getYearStart().getDayOfMonth()).isEqualTo(1);
        assertThat(gasDayOptions.getYearStart().getHour()).isEqualTo(5);
        assertThat(gasDayOptions.getYearStart().getMinute()).isEqualTo(0);
        assertThat(gasDayOptions.getYearStart().getSecond()).isEqualTo(0);
        assertThat(gasDayOptions.getYearStart().getNano()).isEqualTo(0);
    }

    @Test(expected = IllegalStateException.class)
    @Transactional
    public void createGasDayOptions2ndTime() {
        inMemoryBootstrapModule.getMeteringService()
                .createGasDayOptions(DayMonthTime.from(MonthDay.of(Month.OCTOBER, 1), LocalTime.of(5, 0)));

        // Business method
        inMemoryBootstrapModule.getMeteringService()
                .createGasDayOptions(DayMonthTime.from(MonthDay.of(Month.OCTOBER, 1), LocalTime.of(5, 0)));

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void findGasDayOptions() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        meteringService.createGasDayOptions(DayMonthTime.from(MonthDay.of(Month.OCTOBER, 1), LocalTime.of(5, 0)));

        // Business method
        Optional<GasDayOptions> gasDayOptions = meteringService.getGasDayOptions();

        // Asserts
        assertThat(gasDayOptions).isPresent();
        assertThat(gasDayOptions.get().getYearStart()).isNotNull();
        assertThat(gasDayOptions.get().getYearStart().getMonth()).isEqualTo(Month.OCTOBER);
        assertThat(gasDayOptions.get().getYearStart().getDayOfMonth()).isEqualTo(1);
        assertThat(gasDayOptions.get().getYearStart().getHour()).isEqualTo(5);
    }

}