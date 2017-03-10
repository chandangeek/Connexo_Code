/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
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
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LinkTest {
    private static MetrologyInMemoryBootstrapModule inMemoryBootstrapModule = new MetrologyInMemoryBootstrapModule();

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