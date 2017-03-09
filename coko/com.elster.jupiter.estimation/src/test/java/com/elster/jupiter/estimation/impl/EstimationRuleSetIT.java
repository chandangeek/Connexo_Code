/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.EstimatorFactory;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UpdatableHolder;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.collections.KPermutation;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EstimationRuleSetIT {

    public static final String ZERO_FILL = "zeroFill";
    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private EstimatorFactory estimatorFactory;
    @Mock
    private Estimator minMax, zeroFill;
    @Mock
    private PropertySpec maxConsecutive;

    private TransactionService transactionService;

    private EstimationService estimationService;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private String MAX_NUMBER_IN_SEQUENCE = "maxNumberInSequence";
    private String MIN_MAX = "minMax";
    private ReadingType readingType;
    private BigDecimalFactory valueFactory = new BigDecimalFactory();


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(Clock.class).toInstance(Clock.systemDefaultZone());
        }
    }

    @Before
    public void setUp() throws SQLException {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new FiniteStateMachineModule(),
                    new UsagePointLifeCycleConfigurationModule(),
                    new CalendarModule(),
                    new MeteringModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"),
                    new BasicPropertiesModule(),
                    new TimeModule(),
                    new MeteringGroupsModule(),
                    new SearchModule(),
                    new TaskModule(),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new EstimationModule(),
                    new NlsModule(),
                    new DataVaultModule(),
                    new CustomPropertySetsModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(estimatorFactory.available()).thenReturn(Arrays.asList(MIN_MAX, ZERO_FILL));
        when(estimatorFactory.createTemplate(eq(MIN_MAX))).thenReturn(minMax);
        when(estimatorFactory.createTemplate(eq(ZERO_FILL))).thenReturn(zeroFill);
        when(minMax.getPropertySpecs()).thenReturn(Collections.emptyList());
        when(zeroFill.getPropertySpecs()).thenReturn(Arrays.asList(maxConsecutive));
        when(maxConsecutive.getName()).thenReturn(MAX_NUMBER_IN_SEQUENCE);
        when(maxConsecutive.getValueFactory()).thenReturn(valueFactory);
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(VoidTransaction.of(() -> {
                    injector.getInstance(FiniteStateMachineService.class);
                    estimationService = injector.getInstance(EstimationService.class);
                    EstimationServiceImpl instance = (EstimationServiceImpl) estimationService;
                    injector.getInstance(MeteringService.class);
                    instance.addEstimatorFactory(estimatorFactory);
                }
        ));
        readingType = injector.getInstance(MeteringService.class).getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testPersist() {
        UpdatableHolder<Long> ruleSetId = new UpdatableHolder<>(null);
        transactionService.execute(VoidTransaction.of(() -> {
            EstimationRuleSet estimationRuleSet = estimationService.createEstimationRuleSet("myRuleSet", QualityCodeSystem.MDC);
                    EstimationRule zeroesRule = estimationRuleSet.addRule(ZERO_FILL, "consecutiveZeroes")
                            .withReadingType(readingType)
                            .havingProperty(MAX_NUMBER_IN_SEQUENCE)
                            .withValue(BigDecimal.valueOf(20))
                            .active(true)
                            .create();
                    EstimationRule minMaxRule = estimationRuleSet.addRule(MIN_MAX, "minmax")
                            .withReadingType(readingType)
                            .active(true)
                            .create();
                    ruleSetId.update(estimationRuleSet.getId());
                }
        ));
        Optional<? extends EstimationRuleSet> found = estimationService.getEstimationRuleSet(ruleSetId.get());
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getRules()).hasSize(2);
    }

    @Test
    public void testAddSecondRuleSeeIfReadingTypesArentLost() {
        EstimationRuleSet estimationRuleSet;
        try (TransactionContext context = transactionService.getContext()) {
            estimationRuleSet = estimationService.createEstimationRuleSet("myRuleSet", QualityCodeSystem.MDC);
            EstimationRule zeroesRule = estimationRuleSet.addRule(ZERO_FILL, "consecutiveZeroes")
                    .withReadingType(readingType)
                    .havingProperty(MAX_NUMBER_IN_SEQUENCE).withValue(BigDecimal.valueOf(20))
                    .active(true)
                    .create();
            context.commit();
        }
        try (TransactionContext context = transactionService.getContext()) {
            estimationRuleSet = estimationService.getEstimationRuleSet(estimationRuleSet.getId()).get();
            EstimationRule minMaxRule = estimationRuleSet.addRule(MIN_MAX, "minmax")
                    .withReadingType(readingType)
                    .active(true)
                    .create();
            context.commit();
        }
        estimationRuleSet = estimationService.getEstimationRuleSet(estimationRuleSet.getId()).get();
        assertThat(estimationRuleSet.getRules()).hasSize(2);
        EstimationRule estimationRule = estimationRuleSet.getRules().get(0);
        assertThat(estimationRule.getReadingTypes()).hasSize(1);
    }

    @Test
    public void testAddSecondRuleSeeIfPropertiesArentLost() {
        EstimationRuleSet estimationRuleSet;
        try (TransactionContext context = transactionService.getContext()) {
            estimationRuleSet = estimationService.createEstimationRuleSet("myRuleSet", QualityCodeSystem.MDC);
            EstimationRule zeroesRule = estimationRuleSet.addRule(ZERO_FILL, "consecutiveZeroes")
                    .withReadingType(readingType)
                    .havingProperty(MAX_NUMBER_IN_SEQUENCE).withValue(BigDecimal.valueOf(20))
                    .active(true)
                    .create();
            context.commit();
        }
        try (TransactionContext context = transactionService.getContext()) {
            estimationRuleSet = estimationService.getEstimationRuleSet(estimationRuleSet.getId()).get();
            EstimationRule minMaxRule = estimationRuleSet.addRule(MIN_MAX, "minmax")
                    .withReadingType(readingType)
                    .active(true)
                    .create();
            context.commit();
        }
        estimationRuleSet = estimationService.getEstimationRuleSet(estimationRuleSet.getId()).get();
        assertThat(estimationRuleSet.getRules()).hasSize(2);
        EstimationRule estimationRule = estimationRuleSet.getRules().get(0);
        assertThat(estimationRule.getProperties()).hasSize(1);
    }

    @Test
    public void testReorderRules() {
        UpdatableHolder<Long> ruleSetId = new UpdatableHolder<>(null);
        transactionService.execute(VoidTransaction.of(() -> {
            EstimationRuleSet estimationRuleSet = estimationService.createEstimationRuleSet("myRuleSet", QualityCodeSystem.MDC);
                    EstimationRule zeroesRule = estimationRuleSet.addRule(ZERO_FILL, "consecutiveZeroes")
                            .withReadingType(readingType)
                            .havingProperty(MAX_NUMBER_IN_SEQUENCE).withValue(BigDecimal.valueOf(20))
                            .active(true)
                            .create();
                    EstimationRule minMaxRule = estimationRuleSet.addRule(MIN_MAX, "minmax")
                            .withReadingType(readingType)
                            .active(true)
                            .create();
                    ruleSetId.update(estimationRuleSet.getId());
                }
        ));
        Optional<? extends EstimationRuleSet> found = estimationService.getEstimationRuleSet(ruleSetId.get());
        assertThat(found.isPresent()).isTrue();
        EstimationRuleSet estimationRuleSet = found.get();
        transactionService.execute(VoidTransaction.of(() -> {
            estimationRuleSet.reorderRules(new KPermutation(1, 0));
        }));
        found = estimationService.getEstimationRuleSet(ruleSetId.get());
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getRules()).hasSize(2);
        assertThat(found.get().getRules().get(0).getImplementation()).isEqualTo(MIN_MAX);
        assertThat(found.get().getRules().get(1).getImplementation()).isEqualTo(ZERO_FILL);
    }

    @Test
    public void testFindEstimationRuleByQualityType() {
        UpdatableHolder<Long> ruleId = new UpdatableHolder<>(null);
        transactionService.execute(VoidTransaction.of(() -> {
            EstimationRuleSet estimationRuleSet = estimationService.createEstimationRuleSet("myRuleSet", QualityCodeSystem.MDC);
                    EstimationRule zeroesRule = estimationRuleSet.addRule(ZERO_FILL, "consecutiveZeroes")
                            .withReadingType(readingType)
                            .havingProperty(MAX_NUMBER_IN_SEQUENCE).withValue(BigDecimal.valueOf(20))
                            .active(true)
                            .create();
                    ruleId.update(zeroesRule.getId());
                }
        ));

        Optional<? extends EstimationRule> foundEstimationRule = estimationService.findEstimationRuleByQualityType(
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, ruleId.get().intValue()));
        assertThat(foundEstimationRule.isPresent()).isTrue();
        assertThat(foundEstimationRule.get().getId()).isEqualTo(ruleId.get());
    }
}
