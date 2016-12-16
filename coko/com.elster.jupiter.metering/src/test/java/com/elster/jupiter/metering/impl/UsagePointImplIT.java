/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointManagementException;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the {@link UsagePointImpl} component.
 */
public class UsagePointImplIT {

    private static final Instant JULY_1ST_2016 = Instant.ofEpochMilli(1467324000000L);
    private static final Instant JULY_15TH_2016 = Instant.ofEpochMilli(1468533600000L);
    private static final Instant AUG_1ST_2016 = Instant.ofEpochMilli(1470002400000L);
    private static final Instant AUG_15TH_2016 = Instant.ofEpochMilli(1471212000000L);
    private static final Instant SEPT_1ST_2016 = Instant.ofEpochMilli(1472680800000L);

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void testCanCreateAndUpdateUsagePoint() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        DataModel dataModel = meteringService.getDataModel();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("mrID", Instant.EPOCH).create();
        long id = usagePoint.getId();
        assertThat(dataModel.mapper(UsagePoint.class).find()).hasSize(1);
        usagePoint.update();
        assertThat(usagePoint.getVersion()).isEqualTo(2);
        usagePoint.delete();
        assertThat(dataModel.mapper(UsagePoint.class).find()).hasSize(0);
        assertThat(dataModel.mapper(UsagePoint.class).getJournal(id)).hasSize(2);
    }

    @Test
    @Transactional
    public void noEffectiveMetrologyConfigurations() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        Instant now = inMemoryBootstrapModule.getClock().instant();
        UsagePoint usagePoint = meteringService
                .getServiceCategory(ServiceKind.ELECTRICITY).get()
                .newUsagePoint("noEffectiveMetrologyConfigurations", now)
                .create();

        // Business method
        List<EffectiveMetrologyConfigurationOnUsagePoint> metrologyConfigurations = usagePoint.getEffectiveMetrologyConfigurations(Range.all());

        // Asserts
        assertThat(metrologyConfigurations).isEmpty();
    }

    @Test
    @Transactional
    public void effectiveMetrologyConfigurationContainsPeriod() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        Instant usagePointCreationTime = AUG_1ST_2016;
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint =
                serviceCategory
                        .newUsagePoint("effectiveMetrologyConfigurationContainsPeriod", usagePointCreationTime)
                        .create();
        UsagePointMetrologyConfiguration configuration =
                inMemoryBootstrapModule
                        .getMetrologyConfigurationService()
                        .newUsagePointMetrologyConfiguration("effectiveMetrologyConfigurationContainsPeriod", serviceCategory)
                        .create();
        configuration.activate();
        usagePoint.apply(configuration, usagePointCreationTime);
        Instant periodStart = AUG_15TH_2016;
        Range<Instant> period = Range.closedOpen(periodStart, periodStart.plusSeconds(86400));

        // Business method
        List<EffectiveMetrologyConfigurationOnUsagePoint> metrologyConfigurations = usagePoint.getEffectiveMetrologyConfigurations(period);

        // Asserts
        assertThat(metrologyConfigurations).hasSize(1);
    }

    @Test
    @Transactional
    public void effectiveMetrologyConfigurationAfterPeriod() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        Instant usagePointCreationTime = AUG_1ST_2016;  // for curiosity's sake 2016-08-01 00:00:00 (Brussels)
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint =
                serviceCategory
                        .newUsagePoint("effectiveMetrologyConfigurationAfterPeriod", usagePointCreationTime)
                        .create();
        UsagePointMetrologyConfiguration configuration =
                inMemoryBootstrapModule
                        .getMetrologyConfigurationService()
                        .newUsagePointMetrologyConfiguration("effectiveMetrologyConfigurationAfterPeriod", serviceCategory)
                        .create();
        configuration.activate();
        usagePoint.apply(configuration, usagePointCreationTime);
        Range<Instant> period = Range.closedOpen(JULY_1ST_2016, JULY_15TH_2016);

        // Business method
        List<EffectiveMetrologyConfigurationOnUsagePoint> metrologyConfigurations = usagePoint.getEffectiveMetrologyConfigurations(period);

        // Asserts
        assertThat(metrologyConfigurations).isEmpty();
    }

    @Test
    @Transactional
    public void effectiveMetrologyConfigurationOverlapsWithPeriodAtStart() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        Instant usagePointCreationTime = AUG_1ST_2016;
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint =
                serviceCategory
                        .newUsagePoint("effectiveMetrologyConfigurationOverlapsWithPeriodAtStart", usagePointCreationTime)
                        .create();
        UsagePointMetrologyConfiguration configuration =
                inMemoryBootstrapModule
                        .getMetrologyConfigurationService()
                        .newUsagePointMetrologyConfiguration("effectiveMetrologyConfigurationOverlapsWithPeriodAtStart", serviceCategory)
                        .create();
        configuration.activate();
        usagePoint.apply(configuration, usagePointCreationTime);
        Range<Instant> period = Range.closedOpen(JULY_15TH_2016, SEPT_1ST_2016);

        // Business method
        List<EffectiveMetrologyConfigurationOnUsagePoint> metrologyConfigurations = usagePoint.getEffectiveMetrologyConfigurations(period);

        // Asserts
        assertThat(metrologyConfigurations).hasSize(1);
    }

    @Test
    @Transactional
    public void twoEffectiveMetrologyConfigurationsOverlapsWithPeriodAtTheirBoundary() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint =
                serviceCategory
                        .newUsagePoint("twoEffectiveMetrologyConfigurationsOverlapsWithPeriodAtTheirBoundary", AUG_1ST_2016)
                        .create();
        UsagePointMetrologyConfiguration configuration1 =
                inMemoryBootstrapModule
                        .getMetrologyConfigurationService()
                        .newUsagePointMetrologyConfiguration("twoEffectiveMetrologyConfigurationsOverlapsWithPeriodAtTheirBoundary1", serviceCategory)
                        .create();
        configuration1.activate();
        UsagePointMetrologyConfiguration configuration2 =
                inMemoryBootstrapModule
                        .getMetrologyConfigurationService()
                        .newUsagePointMetrologyConfiguration("twoEffectiveMetrologyConfigurationsOverlapsWithPeriodAtTheirBoundary2", serviceCategory)
                        .create();
        configuration2.activate();
        usagePoint.apply(configuration1, JULY_1ST_2016);
        usagePoint.apply(configuration2, AUG_1ST_2016);
        Range<Instant> period = Range.closedOpen(JULY_15TH_2016, SEPT_1ST_2016);

        // Business method
        List<EffectiveMetrologyConfigurationOnUsagePoint> metrologyConfigurations = usagePoint.getEffectiveMetrologyConfigurations(period);

        // Asserts
        assertThat(metrologyConfigurations).hasSize(2);
    }

    @Test
    @Transactional
    public void testUsagePointHasDefaultState() {
        ServiceCategory serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("testCanSetStateForUsagePoint", AUG_1ST_2016).create();

        usagePoint = inMemoryBootstrapModule.getMeteringService().findUsagePointById(usagePoint.getId()).get();
        assertThat(usagePoint.getState().isInitial()).isTrue();
        assertThat(usagePoint.getState(AUG_1ST_2016)).isEqualTo(usagePoint.getState());
    }

    @Test
    @Transactional
    public void testCanChangeStateForUsagePoint() {
        UsagePointLifeCycle lifeCycle = inMemoryBootstrapModule.getUsagePointLifeCycleConfService().newUsagePointLifeCycle("Test");
        UsagePointState initialState = lifeCycle.getStates().stream().filter(UsagePointState::isInitial).findFirst().get();
        UsagePointState state2 = lifeCycle.newState("State 2").setStage(UsagePointStage.Key.OPERATIONAL).complete();
        ServiceCategory serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("testCanChangeStateForUsagePoint", AUG_1ST_2016).create();

        ((UsagePointImpl) usagePoint).setState(initialState, AUG_15TH_2016);
        ((UsagePointImpl) usagePoint).setState(state2, SEPT_1ST_2016);

        usagePoint = inMemoryBootstrapModule.getMeteringService().findUsagePointById(usagePoint.getId()).get();
        assertThat(usagePoint.getState()).isEqualTo(state2);
        assertThat(usagePoint.getState(AUG_15TH_2016)).isEqualTo(initialState);
    }

    @Test(expected = IllegalArgumentException.class)
    @Transactional
    public void testCanNotChangeStateForUsagePointIfItHasOtherStatesInFuture() {
        UsagePointLifeCycle lifeCycle = inMemoryBootstrapModule.getUsagePointLifeCycleConfService().newUsagePointLifeCycle("Test");
        UsagePointState initialState = lifeCycle.getStates().stream().filter(UsagePointState::isInitial).findFirst().get();
        UsagePointState state2 = lifeCycle.newState("State 2").setStage(UsagePointStage.Key.OPERATIONAL).complete();
        ServiceCategory serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("testCanNotChangeStateForUsagePointIfItHasOtherStatesInFuture", AUG_1ST_2016).create();

        ((UsagePointImpl) usagePoint).setState(initialState, SEPT_1ST_2016);
        ((UsagePointImpl) usagePoint).setState(state2, AUG_15TH_2016);
    }

    @Test(expected = UsagePointLifeCycleDeleteObjectException.class)
    @Transactional
    public void testCanNotDeleteUsagePointLifeCycleWhichIsInUse() {
        UsagePointLifeCycle lifeCycle = inMemoryBootstrapModule.getUsagePointLifeCycleConfService().newUsagePointLifeCycle("Test");
        ServiceCategory serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("testCanNotDeleteUsagePointLifeCycleWhichIsInUse", AUG_1ST_2016).create();
        ((UsagePointImpl) usagePoint).setState(lifeCycle.getStates().stream().filter(UsagePointState::isInitial).findFirst().get(), AUG_15TH_2016);

        lifeCycle.remove();
    }

    @Test(expected = UsagePointLifeCycleDeleteObjectException.class)
    @Transactional
    public void testCanNotDeleteUsagePointStateWhichIsInUse() {
        UsagePointLifeCycle lifeCycle = inMemoryBootstrapModule.getUsagePointLifeCycleConfService().newUsagePointLifeCycle("Test");
        UsagePointState state = lifeCycle.newState("State").setStage(UsagePointStage.Key.OPERATIONAL).complete();
        ServiceCategory serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("testCanNotDeleteUsagePointStateWhichIsInUse", AUG_1ST_2016).create();
        ((UsagePointImpl) usagePoint).setState(state, AUG_15TH_2016);

        state.remove();
    }

    @Test
    @Transactional
    public void testCanQueryUsagePointsWithState() {
        UsagePointLifeCycle lifeCycle = inMemoryBootstrapModule.getUsagePointLifeCycleConfService().newUsagePointLifeCycle("Test");
        UsagePointState initialState = lifeCycle.getStates().stream().filter(UsagePointState::isInitial).findFirst().get();
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("testCanQueryUsagePointsWithState", AUG_1ST_2016).create();

        ((UsagePointImpl) usagePoint).setState(initialState, AUG_15TH_2016);

        UsagePoint found = meteringService.getUsagePointQuery().select(where("state.state").isEqualTo(initialState)).get(0);
        assertThat(found).isEqualTo(usagePoint);
    }

    @Test(expected = UsagePointManagementException.class)
    @Transactional
    public void linkMetrologyConfigurationToUsagePointWithIncorrectStage() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        Instant now = inMemoryBootstrapModule.getClock().instant();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory
                .newUsagePoint("testUP", now)
                .create();
        UsagePointMetrologyConfiguration configuration =
                inMemoryBootstrapModule
                        .getMetrologyConfigurationService()
                        .newUsagePointMetrologyConfiguration("testMC", serviceCategory)
                        .create();
        configuration.activate();
        usagePoint.getState().startUpdate().setStage(UsagePointStage.Key.OPERATIONAL).complete();
        usagePoint.apply(configuration, now);
    }
}