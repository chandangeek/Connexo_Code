package com.elster.insight.usagepoint.config.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MetrologyConfigurationCrudTest {

    private static MetrologyInMemoryBootstrapModule inMemoryBootstrapModule = new MetrologyInMemoryBootstrapModule();

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    private UsagePointConfigurationService getUsagePointConfigurationService() {
        return inMemoryBootstrapModule.getUsagePointConfigurationService();
    }

    private TransactionService getTransactionService() {
        return inMemoryBootstrapModule.getTransactionService();
    }

    @Test
    public void testCrud() {
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            MetrologyConfiguration mc1 = upcService.newMetrologyConfiguration("Residenshull");
            assertThat(mc1.getName()).isEqualTo("Residenshull");
            MetrologyConfiguration mc2 = upcService.newMetrologyConfiguration("Commercial 1");
            assertThat(mc2.getName()).isEqualTo("Commercial 1");
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            Optional<MetrologyConfiguration> mc1 = upcService.findMetrologyConfiguration(1);
            assertThat(mc1).isPresent();
            assertThat(mc1.get().getName()).isEqualTo("Residenshull");
            Optional<MetrologyConfiguration> mc2 = getUsagePointConfigurationService().findMetrologyConfiguration(2);
            assertThat(mc2.isPresent());
            assertThat(mc2.get().getName()).isEqualTo("Commercial 1");
            List<MetrologyConfiguration> all = upcService.findAllMetrologyConfigurations();
            assertThat(all.size()).isEqualTo(2);
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            Optional<MetrologyConfiguration> mc1 = upcService.findMetrologyConfiguration(1);
            assertThat(mc1).isPresent();
            assertThat(mc1.get().getName()).isEqualTo("Residenshull");
            mc1.get().updateName("Residential");
            mc1 = upcService.findMetrologyConfiguration(1);
            assertThat(mc1).isPresent();
            assertThat(mc1.get().getName()).isEqualTo("Residential");
            mc1 = upcService.findMetrologyConfiguration("Residential");
            assertThat(mc1).isPresent();
            assertThat(mc1.get().getName()).isEqualTo("Residential");
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            List<MetrologyConfiguration> all = upcService.findAllMetrologyConfigurations();
            for (MetrologyConfiguration mc : all) {
                mc.delete();
            }
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            List<MetrologyConfiguration> all = upcService.findAllMetrologyConfigurations();
            assertThat(all).isEmpty();
            context.commit();
        }
    }

    @Test(expected = ConstraintViolationException.class)
    public void testEmptyName() {
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            upcService.newMetrologyConfiguration("");
            context.commit();
        }
    }

    @Test(expected = ConstraintViolationException.class)
    public void testNullName() {
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            upcService.newMetrologyConfiguration(null);
            context.commit();
        }
    }

    @Test(expected = ConstraintViolationException.class)
    public void testDuplicateNameCreate() {
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            upcService.newMetrologyConfiguration("dup1");
            upcService.newMetrologyConfiguration("dup1");
            context.commit();
        }
    }

    @Test(expected = ConstraintViolationException.class)
    public void testDuplicateNameRename() {
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            upcService.newMetrologyConfiguration("dup2");
            MetrologyConfiguration x = upcService.newMetrologyConfiguration("x");
            x.updateName("dup2");
            context.commit();
        }
    }
}
