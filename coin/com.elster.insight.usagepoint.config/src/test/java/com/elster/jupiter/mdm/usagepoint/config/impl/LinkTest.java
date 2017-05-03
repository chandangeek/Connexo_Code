/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.StageSet;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

import java.util.Arrays;
import java.util.Collections;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LinkTest {
    private static MetrologyInMemoryBootstrapModule inMemoryBootstrapModule = new MetrologyInMemoryBootstrapModule();
    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testMCValRuleSetLink() {
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            UsagePointConfigurationService usagePointConfigurationService = inMemoryBootstrapModule.getUsagePointConfigurationService();
            ServerMetrologyConfigurationService serverMetrologyConfigurationService = inMemoryBootstrapModule.getMetrologyConfigurationService();
            ValidationService validationService = inMemoryBootstrapModule.getValidationService();
            MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();

            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = serverMetrologyConfigurationService
                    .newUsagePointMetrologyConfiguration("Test residential prosumer with 2 meters", serviceCategory).create();
            MetrologyPurpose purposeBilling = serverMetrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING).get();
            MetrologyContract contractBilling = usagePointMetrologyConfiguration.addMandatoryMetrologyContract(purposeBilling);
            ValidationRuleSet vrs1 = validationService.createValidationRuleSet("Rule #1", QualityCodeSystem.MDM);
            usagePointConfigurationService.addValidationRuleSet(contractBilling, vrs1);
            assertThat(serverMetrologyConfigurationService.findMetrologyConfiguration(usagePointMetrologyConfiguration.getId())).isPresent();
            assertThat(usagePointConfigurationService.getValidationRuleSets(contractBilling)).hasSize(1);
            ValidationRuleSet vrs2 = validationService.createValidationRuleSet("Rule #2", QualityCodeSystem.MDM);
            usagePointConfigurationService.addValidationRuleSet(contractBilling, vrs2);
            assertThat(usagePointConfigurationService.getValidationRuleSets(contractBilling)).hasSize(2);
            usagePointConfigurationService.removeValidationRuleSet(contractBilling, vrs1);
            assertThat(usagePointConfigurationService.getValidationRuleSets(contractBilling)).hasSize(1);
            usagePointMetrologyConfiguration.removeMetrologyContract(contractBilling);
            assertThat(usagePointConfigurationService.getValidationRuleSets(contractBilling)).hasSize(0);
        }
    }

    @Test
    public void testMCValRuleSetLinkWithLifecycleStates() {
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            UsagePointConfigurationService usagePointConfigurationService = inMemoryBootstrapModule.getUsagePointConfigurationService();
            ServerMetrologyConfigurationService serverMetrologyConfigurationService = inMemoryBootstrapModule.getMetrologyConfigurationService();
            ValidationService validationService = inMemoryBootstrapModule.getValidationService();
            MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
            UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService = inMemoryBootstrapModule.getUsagePointLifeCycleConfigurationService();

            UsagePointLifeCycle lifeCycle = usagePointLifeCycleConfigurationService.newUsagePointLifeCycle("lifecycle");
            State state1 = usagePointLifeCycleConfigurationService.getUsagePointStates().get(0);
            State state2 = usagePointLifeCycleConfigurationService.getUsagePointStates().get(1);
            State state3 = usagePointLifeCycleConfigurationService.getUsagePointStates().get(2);

            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = serverMetrologyConfigurationService
                    .newUsagePointMetrologyConfiguration("Test residential prosumer with 2 meters", serviceCategory).create();
            MetrologyPurpose purposeBilling = serverMetrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING).get();
            MetrologyContract contractBilling = usagePointMetrologyConfiguration.addMandatoryMetrologyContract(purposeBilling);
            ValidationRuleSet vrs1 = validationService.createValidationRuleSet("Rule #1", QualityCodeSystem.MDM);
            usagePointConfigurationService.addValidationRuleSet(contractBilling, vrs1, Collections.singletonList(state1));
            assertThat(serverMetrologyConfigurationService.findMetrologyConfiguration(usagePointMetrologyConfiguration.getId())).isPresent();
            assertThat(usagePointConfigurationService.getValidationRuleSets(contractBilling, state1)).hasSize(1);
            ValidationRuleSet vrs2 = validationService.createValidationRuleSet("Rule #2", QualityCodeSystem.MDM);
            usagePointConfigurationService.addValidationRuleSet(contractBilling, vrs2, Arrays.asList(state1, state2));
            assertThat(usagePointConfigurationService.getValidationRuleSets(contractBilling, state1)).hasSize(2);
            assertThat(usagePointConfigurationService.getValidationRuleSets(contractBilling, state2)).hasSize(1);
            assertThat(usagePointConfigurationService.getValidationRuleSets(contractBilling, state3)).hasSize(0);
            usagePointConfigurationService.removeValidationRuleSet(contractBilling, vrs1);
            assertThat(usagePointConfigurationService.getValidationRuleSets(contractBilling, state1)).hasSize(1);
            assertThat(usagePointConfigurationService.getValidationRuleSets(contractBilling, state2)).hasSize(1);
            usagePointMetrologyConfiguration.removeMetrologyContract(contractBilling);
            assertThat(usagePointConfigurationService.getValidationRuleSets(contractBilling)).hasSize(0);
            assertThat(usagePointConfigurationService.getValidationRuleSets(contractBilling, state1)).hasSize(0);
            assertThat(usagePointConfigurationService.getValidationRuleSets(contractBilling, state2)).hasSize(0);
        }
    }

    @Test
    @Expected(value = UsagePointLifeCycleDeleteObjectException.class, message = "This state can't be removed from this usage point life cycle because one or more metrology configurations use this state.")
    public void testRemoveLifecycleStates() throws Exception {
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            UsagePointConfigurationService usagePointConfigurationService = inMemoryBootstrapModule.getUsagePointConfigurationService();
            ServerMetrologyConfigurationService serverMetrologyConfigurationService = inMemoryBootstrapModule.getMetrologyConfigurationService();
            ValidationService validationService = inMemoryBootstrapModule.getValidationService();
            MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
            UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService = inMemoryBootstrapModule.getUsagePointLifeCycleConfigurationService();

            UsagePointLifeCycle lifeCycle = usagePointLifeCycleConfigurationService.newUsagePointLifeCycle("lifecycle");
            StageSet defaultStageSet = usagePointLifeCycleConfigurationService.getDefaultStageSet();
            Stage stage = defaultStageSet.getStages().get(0);
            FiniteStateMachineUpdater lifeCycleUpdater = lifeCycle.getUpdater();
            FiniteStateMachineBuilder.StateBuilder stateBuilder = lifeCycleUpdater.newCustomState("state", stage);
            State state = stateBuilder.complete();
            lifeCycleUpdater.complete();

            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = serverMetrologyConfigurationService
                    .newUsagePointMetrologyConfiguration("Test residential prosumer with 2 meters", serviceCategory).create();
            MetrologyPurpose purposeBilling = serverMetrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING).get();
            MetrologyContract contractBilling = usagePointMetrologyConfiguration.addMandatoryMetrologyContract(purposeBilling);
            ValidationRuleSet vrs1 = validationService.createValidationRuleSet("Rule #1", QualityCodeSystem.MDM);
            usagePointConfigurationService.addValidationRuleSet(contractBilling, vrs1, Collections.singletonList(state));
            assertThat(serverMetrologyConfigurationService.findMetrologyConfiguration(usagePointMetrologyConfiguration.getId())).isPresent();
            assertThat(usagePointConfigurationService.getValidationRuleSets(contractBilling, state)).hasSize(1);

            lifeCycle.removeState(state);
        }
    }

    @Test
    @Expected(value = UsagePointLifeCycleDeleteObjectException.class, message = "This life cycle can't be removed because one or more metrology configurations use states of this life cycle.")
    public void testRemoveLifecycle() throws Exception {
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            UsagePointConfigurationService usagePointConfigurationService = inMemoryBootstrapModule.getUsagePointConfigurationService();
            ServerMetrologyConfigurationService serverMetrologyConfigurationService = inMemoryBootstrapModule.getMetrologyConfigurationService();
            ValidationService validationService = inMemoryBootstrapModule.getValidationService();
            MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
            UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService = inMemoryBootstrapModule.getUsagePointLifeCycleConfigurationService();

            UsagePointLifeCycle lifeCycle = usagePointLifeCycleConfigurationService.newUsagePointLifeCycle("lifecycle");
            StageSet defaultStageSet = usagePointLifeCycleConfigurationService.getDefaultStageSet();
            Stage stage = defaultStageSet.getStages().get(0);
            FiniteStateMachineUpdater lifeCycleUpdater = lifeCycle.getUpdater();
            FiniteStateMachineBuilder.StateBuilder stateBuilder = lifeCycleUpdater.newCustomState("state", stage);
            State state = stateBuilder.complete();
            lifeCycleUpdater.complete();

            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = serverMetrologyConfigurationService
                    .newUsagePointMetrologyConfiguration("Test residential prosumer with 2 meters", serviceCategory).create();
            MetrologyPurpose purposeBilling = serverMetrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING).get();
            MetrologyContract contractBilling = usagePointMetrologyConfiguration.addMandatoryMetrologyContract(purposeBilling);
            ValidationRuleSet vrs1 = validationService.createValidationRuleSet("Rule #1", QualityCodeSystem.MDM);
            usagePointConfigurationService.addValidationRuleSet(contractBilling, vrs1, Collections.singletonList(state));
            assertThat(serverMetrologyConfigurationService.findMetrologyConfiguration(usagePointMetrologyConfiguration.getId())).isPresent();
            assertThat(usagePointConfigurationService.getValidationRuleSets(contractBilling, state)).hasSize(1);

            lifeCycle.remove();
        }
    }

    @Test
    public void testRemoveLifecycleSuccessful() {
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            UsagePointConfigurationService usagePointConfigurationService = inMemoryBootstrapModule.getUsagePointConfigurationService();
            ServerMetrologyConfigurationService serverMetrologyConfigurationService = inMemoryBootstrapModule.getMetrologyConfigurationService();
            ValidationService validationService = inMemoryBootstrapModule.getValidationService();
            MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
            UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService = inMemoryBootstrapModule.getUsagePointLifeCycleConfigurationService();

            UsagePointLifeCycle lifeCycle = usagePointLifeCycleConfigurationService.newUsagePointLifeCycle("lifecycle");
            StageSet defaultStageSet = usagePointLifeCycleConfigurationService.getDefaultStageSet();
            Stage stage = defaultStageSet.getStages().get(0);
            FiniteStateMachineUpdater lifeCycleUpdater = lifeCycle.getUpdater();
            FiniteStateMachineBuilder.StateBuilder stateBuilder = lifeCycleUpdater.newCustomState("state", stage);
            State state = stateBuilder.complete();
            lifeCycleUpdater.complete();

            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = serverMetrologyConfigurationService
                    .newUsagePointMetrologyConfiguration("Test residential prosumer with 2 meters", serviceCategory).create();
            MetrologyPurpose purposeBilling = serverMetrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING).get();
            MetrologyContract contractBilling = usagePointMetrologyConfiguration.addMandatoryMetrologyContract(purposeBilling);
            ValidationRuleSet vrs1 = validationService.createValidationRuleSet("Rule #1", QualityCodeSystem.MDM);
            usagePointConfigurationService.addValidationRuleSet(contractBilling, vrs1, Collections.singletonList(state));
            assertThat(serverMetrologyConfigurationService.findMetrologyConfiguration(usagePointMetrologyConfiguration.getId())).isPresent();
            assertThat(usagePointConfigurationService.getValidationRuleSets(contractBilling, state)).hasSize(1);


            usagePointMetrologyConfiguration.removeMetrologyContract(contractBilling);
            assertThat(usagePointConfigurationService.getValidationRuleSets(contractBilling, state)).hasSize(0);

            lifeCycle.removeState(state);
            lifeCycle.remove();
        }
    }


    @Test
    public void testMCEstRuleSetLink() {
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            UsagePointConfigurationService usagePointConfigurationService = inMemoryBootstrapModule.getUsagePointConfigurationService();
            ServerMetrologyConfigurationService serverMetrologyConfigurationService = inMemoryBootstrapModule.getMetrologyConfigurationService();
            EstimationService estimationService = inMemoryBootstrapModule.getEstimationService();
            MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();

            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = serverMetrologyConfigurationService
                    .newUsagePointMetrologyConfiguration("Test residential prosumer with 3 meters", serviceCategory).create();
            MetrologyPurpose purposeBilling = serverMetrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING).get();
            MetrologyContract contractBilling = usagePointMetrologyConfiguration.addMandatoryMetrologyContract(purposeBilling);
            EstimationRuleSet ers1 = estimationService.createEstimationRuleSet("Rule #1", QualityCodeSystem.MDM);
            usagePointConfigurationService.addEstimationRuleSet(contractBilling, ers1);
            assertThat(serverMetrologyConfigurationService.findMetrologyConfiguration(usagePointMetrologyConfiguration.getId())).isPresent();
            assertThat(usagePointConfigurationService.getEstimationRuleSets(contractBilling)).hasSize(1);
            EstimationRuleSet ers2 = estimationService.createEstimationRuleSet("Rule #2", QualityCodeSystem.MDM);
            usagePointConfigurationService.addEstimationRuleSet(contractBilling, ers2);
            assertThat(usagePointConfigurationService.getEstimationRuleSets(contractBilling)).hasSize(2);
            assertThat(usagePointConfigurationService.getEstimationRuleSets(contractBilling).get(0)).isEqualTo(ers1);
            assertThat(usagePointConfigurationService.getEstimationRuleSets(contractBilling).get(1)).isEqualTo(ers2);
            usagePointConfigurationService.reorderEstimationRuleSets(contractBilling, Arrays.asList(ers2, ers1));
            assertThat(usagePointConfigurationService.getEstimationRuleSets(contractBilling)).hasSize(2);
            assertThat(usagePointConfigurationService.getEstimationRuleSets(contractBilling).get(0)).isEqualTo(ers2);
            assertThat(usagePointConfigurationService.getEstimationRuleSets(contractBilling).get(1)).isEqualTo(ers1);
            usagePointConfigurationService.removeEstimationRuleSet(contractBilling, ers1);
            assertThat(usagePointConfigurationService.getEstimationRuleSets(contractBilling)).hasSize(1);
            usagePointMetrologyConfiguration.removeMetrologyContract(contractBilling);
            assertThat(usagePointConfigurationService.getEstimationRuleSets(contractBilling)).hasSize(0);
        }
    }

}