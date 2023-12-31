/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.LocaleNeutral;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.StageSet;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointManagementException;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Integration test for the {@link UsagePointImpl} component.
 */
public class UsagePointImplIT {

    private static final Instant JUNE_1ST_2016 = Instant.ofEpochMilli(1464777755000L);
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
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public LocaleNeutral localeNeutral = Using.localeOfMalta();
    @Rule
    public TimeZoneNeutral timeZoneNeutral = Using.timeZoneOfMcMurdo();
    private UsagePointLifeCycleConfigurationService usagePointLifeCycleConfService;

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
                        .newUsagePoint("twoEffectiveMetrologyConfigurationsOverlapsWithPeriodAtTheirBoundary", JUNE_1ST_2016)
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
        usagePoint.getEffectiveMetrologyConfiguration(AUG_1ST_2016).get().close(AUG_1ST_2016);
        usagePoint.apply(configuration2, AUG_1ST_2016);
        Range<Instant> period = Range.closedOpen(JULY_15TH_2016, SEPT_1ST_2016);

        // Business method
        List<EffectiveMetrologyConfigurationOnUsagePoint> metrologyConfigurations = usagePoint.getEffectiveMetrologyConfigurations(period);

        // Asserts
        assertThat(metrologyConfigurations).hasSize(2);
    }

    @Test
    @Transactional
    public void testCannotLinkMetrologyConfigWhenAnotherMetrConfigIsAlreadyLinked() {
        expectedException.expect(UsagePointManagementException.class);
        expectedException.expectMessage("Metrology configuration metrologyConfiguration2 could not be linked to usage point usagePoint at 10:00:00 Mon 01 Aug '16 because another metrology configuration is active at that point in time");
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint =
                serviceCategory
                        .newUsagePoint("usagePoint", JUNE_1ST_2016)
                        .create();
        UsagePointMetrologyConfiguration configuration1 =
                inMemoryBootstrapModule
                        .getMetrologyConfigurationService()
                        .newUsagePointMetrologyConfiguration("metrologyConfiguration1", serviceCategory)
                        .create();
        configuration1.activate();
        UsagePointMetrologyConfiguration configuration2 =
                inMemoryBootstrapModule
                        .getMetrologyConfigurationService()
                        .newUsagePointMetrologyConfiguration("metrologyConfiguration2", serviceCategory)
                        .create();
        configuration2.activate();
        usagePoint.apply(configuration1, JULY_1ST_2016);
        usagePoint.apply(configuration2, AUG_1ST_2016);
    }

    @Test
    @Transactional
    public void canNotLinkMetrologyConfigurationBeforeUsagePointInstallationTime() {
        expectedException.expect(UsagePointManagementException.class);
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint =
                serviceCategory
                        .newUsagePoint("usagePoint", SEPT_1ST_2016)
                        .create();
        UsagePointMetrologyConfiguration configuration1 =
                inMemoryBootstrapModule
                        .getMetrologyConfigurationService()
                        .newUsagePointMetrologyConfiguration("metrologyConfiguration1", serviceCategory)
                        .create();
        configuration1.activate();
        usagePoint.apply(configuration1, JUNE_1ST_2016);
    }

    @Test
    @Transactional
    public void testCannotActivateMetrologyConfigWithIncompatibleMeterRequirements() {
        State deviceState = mock(State.class);
        Stage deviceStage = mock(Stage.class);
        String operationalDeviceStageKey = "mtr.enddevicestage.operational";
        expectedException.expect(UsagePointManagementException.class);
        expectedException.expectMessage("Meter linking error. The meters of the usage point do not provide the necessary reading types for purposes [metrology.purpose.voltage.monitoring.name] of the new metrology configuration");
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem system = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter meter = spy(system.newMeter("Meter", "meterName").create());
        when(meter.getState(any(Instant.class))).thenReturn(Optional.of(deviceState));
        when(deviceState.getStage()).thenReturn(Optional.of(deviceStage));
        when(deviceStage.getName()).thenReturn(operationalDeviceStageKey);
        ReadingType bulkReadingType = inMemoryBootstrapModule.getMeteringService().getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        meter.activate(AUG_1ST_2016).getChannelsContainer().createChannel(bulkReadingType);
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("UsagePoint", JUNE_1ST_2016).create();
        MeterRole meterRole = inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT);
        usagePoint.linkMeters().activate(AUG_1ST_2016, meter, meterRole).complete();

        UsagePointMetrologyConfiguration configuration =
                inMemoryBootstrapModule
                        .getMetrologyConfigurationService()
                        .newUsagePointMetrologyConfiguration("metrologyConfiguration1", serviceCategory)
                        .create();
        MetrologyContract contract = configuration.addMandatoryMetrologyContract(getVoltageMonitoringPurpose());
        configuration.activate();
        usagePoint.apply(configuration, AUG_15TH_2016, Collections.singleton(contract));
    }

    private MetrologyPurpose getVoltageMonitoringPurpose() {
        return inMemoryBootstrapModule.getMetrologyConfigurationService().findMetrologyPurpose(3).get();
    }

    @Test
    @Transactional
    public void testMakeObsolete() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint =
                serviceCategory
                        .newUsagePoint("testMakeObsolete", AUG_1ST_2016)
                        .create();

        // Business method
        usagePoint.makeObsolete();

        // Asserts
        Optional<UsagePoint> foundUsagePoint;

        // Asserts that usage point is available by id
        foundUsagePoint = meteringService.findUsagePointById(usagePoint.getId());
        assertThat(foundUsagePoint).isPresent();
        assertThat(foundUsagePoint.get()).isEqualTo(usagePoint);
        assertThat(foundUsagePoint.get().getObsoleteTime()).isPresent();

        // Asserts that usage point is available by mrid
        foundUsagePoint = meteringService.findUsagePointByMRID(usagePoint.getMRID());
        assertThat(foundUsagePoint).isPresent();
        assertThat(foundUsagePoint.get()).isEqualTo(usagePoint);
        assertThat(foundUsagePoint.get().getObsoleteTime()).isPresent();

        // Asserts that usage point is NOT available by name
        foundUsagePoint = meteringService.findUsagePointByName(usagePoint.getName());
        assertThat(foundUsagePoint).isEmpty();
    }

    @Test
    @Transactional
    public void testGetObsoleteTime() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint =
                serviceCategory
                        .newUsagePoint("nonObsolete", AUG_1ST_2016)
                        .create();
        UsagePoint usagePointObsolete =
                serviceCategory
                        .newUsagePoint("obsolete", AUG_1ST_2016)
                        .create();

        // Business method
        usagePointObsolete.makeObsolete();

        // Asserts
        assertThat(meteringService.findUsagePointById(usagePoint.getId()).get().getObsoleteTime()).isEmpty();
        assertThat(meteringService.findUsagePointById(usagePointObsolete.getId()).get().getObsoleteTime()).isPresent();
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
        usagePointLifeCycleConfService = inMemoryBootstrapModule.getUsagePointLifeCycleConfService();
        StageSet defaultStageSet = usagePointLifeCycleConfService.getDefaultStageSet();
        Stage stage = defaultStageSet.getStageByName(UsagePointStage.OPERATIONAL.getKey()).get();
        UsagePointLifeCycle lifeCycle = usagePointLifeCycleConfService.newUsagePointLifeCycle("Test");
        State initialState = lifeCycle.getStates().stream().filter(State::isInitial).findFirst().get();
        FiniteStateMachineUpdater updater = lifeCycle.getUpdater();
        State state2 = updater.newCustomState("State 2", stage).complete();
        updater.complete();
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
        State initialState = lifeCycle.getStates().stream().filter(State::isInitial).findFirst().get();
        StageSet defaultStageSet = inMemoryBootstrapModule.getUsagePointLifeCycleConfService().getDefaultStageSet();
        Stage stage = defaultStageSet.getStageByName(UsagePointStage.OPERATIONAL.getKey()).get();
        FiniteStateMachineUpdater updater = lifeCycle.getUpdater();
        State state2 = updater.newCustomState("State 2", stage).complete();
        updater.complete();
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
        ((UsagePointImpl) usagePoint).setState(lifeCycle.getStates().stream().filter(State::isInitial).findFirst().get(), AUG_15TH_2016);
        ((UsagePointImpl) usagePoint).setLifeCycle(lifeCycle.getName());
        ((UsagePointImpl) usagePoint).update();
        lifeCycle.remove();
    }

    @Test(expected = UsagePointLifeCycleDeleteObjectException.class)
    @Transactional
    public void testCanNotDeleteUsagePointStateWhichIsInUse() {
        UsagePointLifeCycle lifeCycle = inMemoryBootstrapModule.getUsagePointLifeCycleConfService().newUsagePointLifeCycle("Test");
        StageSet defaultStageSet = inMemoryBootstrapModule.getUsagePointLifeCycleConfService().getDefaultStageSet();
        Stage stage = defaultStageSet.getStageByName(UsagePointStage.OPERATIONAL.getKey()).get();
        FiniteStateMachineUpdater updater = lifeCycle.getUpdater();
        State state = updater.newCustomState("State", stage).complete();
        updater.complete();
        ServiceCategory serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("testCanNotDeleteUsagePointStateWhichIsInUse", AUG_1ST_2016).create();
        ((UsagePointImpl) usagePoint).setState(state, AUG_15TH_2016);
        lifeCycle.removeState(state);
    }

    @Test
    @Transactional
    public void testCanQueryUsagePointsWithState() {
        UsagePointLifeCycle lifeCycle = inMemoryBootstrapModule.getUsagePointLifeCycleConfService().newUsagePointLifeCycle("Test");
        State initialState = lifeCycle.getStates().stream().filter(State::isInitial).findFirst().get();
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("testCanQueryUsagePointsWithState", AUG_1ST_2016).create();

        ((UsagePointImpl) usagePoint).setState(initialState, AUG_15TH_2016);

        UsagePoint found = meteringService.getUsagePointQuery().select(where("state.state").isEqualTo(initialState)).get(0);
        assertThat(found).isEqualTo(usagePoint);
    }

    // TODO: really need to allow operational stage here??? If yes, change the message seed & fix the test
    @Test(expected = UsagePointManagementException.class)
    @Transactional
    public void linkMetrologyConfigurationToUsagePointWithIncorrectStage() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        StageSet defaultStageSet = inMemoryBootstrapModule.getUsagePointLifeCycleConfService().getDefaultStageSet();
        Stage stage = defaultStageSet.getStageByName(UsagePointStage.POST_OPERATIONAL.getKey()).get(); // test updated corresponding with CONM-129
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
        FiniteStateMachineUpdater finiteStateMachineUpdater = usagePoint.getState().getFiniteStateMachine().startUpdate();
        finiteStateMachineUpdater.state(usagePoint.getState().getName()).stage(stage).complete();
        finiteStateMachineUpdater.complete();
        usagePoint = inMemoryBootstrapModule.getMeteringService().findUsagePointById(usagePoint.getId()).get();
        usagePoint.apply(configuration, now);
    }
}
