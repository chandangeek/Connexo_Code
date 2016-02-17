package com.elster.insight.usagepoint.config.impl;

import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;

import java.util.Optional;

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

    private UsagePointConfigurationService getUsagePointConfigurationService() {
        return inMemoryBootstrapModule.getUsagePointConfigurationService();
    }

    private TransactionService getTransactionService() {
        return inMemoryBootstrapModule.getTransactionService();
    }

    private ValidationService getValidationService() {
        return inMemoryBootstrapModule.getValidationService();
    }

    @Test
    public void testMCValRuleSetLink() {
        MetrologyConfiguration mc;
        ValidationRuleSet vrs1;
        ValidationRuleSet vrs2;
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            ValidationService valService = getValidationService();
            mc = upcService.newMetrologyConfiguration("MC1");
            vrs1 = valService.createValidationRuleSet("Rule #1");
            upcService.addValidationRuleSet(mc, vrs1);
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            Optional<MetrologyConfiguration> mc2 = upcService.findMetrologyConfiguration(mc.getId());
            assertThat(mc2).isPresent();
            assertThat(upcService.getValidationRuleSets(mc2.get())).hasSize(1);
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            ValidationService valService = getValidationService();
            vrs2 = valService.createValidationRuleSet("Rule #2");
            vrs2.save();
            upcService.addValidationRuleSet(mc, vrs2);
            Optional<MetrologyConfiguration> mc2 = upcService.findMetrologyConfiguration(mc.getId());
            assertThat(mc2).isPresent();
            assertThat(upcService.getValidationRuleSets(mc2.get())).hasSize(2);
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            Optional<MetrologyConfiguration> mc2 = upcService.findMetrologyConfiguration(mc.getId());
            assertThat(mc2).isPresent();
            upcService.removeValidationRuleSet(mc2.get(), vrs1);
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            Optional<MetrologyConfiguration> mc2 = upcService.findMetrologyConfiguration(mc.getId());
            assertThat(mc2).isPresent();
            assertThat(upcService.getValidationRuleSets(mc2.get())).hasSize(1);
            context.commit();
        }
    }

}