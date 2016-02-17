package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MetrologyConfigurationCrudTest {

    private static MetrologyInMemoryBootstrapModule inMemoryBootstrapModule = new MetrologyInMemoryBootstrapModule();

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    private MetrologyConfigurationService getMetrologyConfigurationService() {
        return inMemoryBootstrapModule.getMetrologyConfigurationService();
    }

    private TransactionService getTransactionService() {
        return inMemoryBootstrapModule.getTransactionService();
    }

    @Test
    public void testCrud() {
        try (TransactionContext context = getTransactionService().getContext()) {
            MetrologyConfigurationService upcService = getMetrologyConfigurationService();
            MetrologyConfiguration mc1 = upcService.newMetrologyConfiguration("Residenshull");
            assertThat(mc1.getName()).isEqualTo("Residenshull");
            MetrologyConfiguration mc2 = upcService.newMetrologyConfiguration("Commercial 1");
            assertThat(mc2.getName()).isEqualTo("Commercial 1");
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            MetrologyConfigurationService upcService = getMetrologyConfigurationService();
            Optional<MetrologyConfiguration> mc1 = upcService.findMetrologyConfiguration(1);
            assertThat(mc1).isPresent();
            assertThat(mc1.get().getName()).isEqualTo("Residenshull");
            Optional<MetrologyConfiguration> mc2 = getMetrologyConfigurationService().findMetrologyConfiguration(2);
            assertThat(mc2.isPresent());
            assertThat(mc2.get().getName()).isEqualTo("Commercial 1");
            List<MetrologyConfiguration> all = upcService.findAllMetrologyConfigurations();
            assertThat(all.size()).isEqualTo(2);
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            MetrologyConfigurationService upcService = getMetrologyConfigurationService();
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
            MetrologyConfigurationService upcService = getMetrologyConfigurationService();
            List<MetrologyConfiguration> all = upcService.findAllMetrologyConfigurations();
            for (MetrologyConfiguration mc : all) {
                mc.delete();
            }
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            MetrologyConfigurationService upcService = getMetrologyConfigurationService();
            List<MetrologyConfiguration> all = upcService.findAllMetrologyConfigurations();
            assertThat(all).isEmpty();
            context.commit();
        }
    }

    @Test(expected = ConstraintViolationException.class)
    public void testEmptyName() {
        try (TransactionContext context = getTransactionService().getContext()) {
            MetrologyConfigurationService upcService = getMetrologyConfigurationService();
            upcService.newMetrologyConfiguration("");
            context.commit();
        }
    }

    @Test(expected = ConstraintViolationException.class)
    public void testNullName() {
        try (TransactionContext context = getTransactionService().getContext()) {
            MetrologyConfigurationService upcService = getMetrologyConfigurationService();
            upcService.newMetrologyConfiguration(null);
            context.commit();
        }
    }

    @Test(expected = ConstraintViolationException.class)
    public void testDuplicateNameCreate() {
        try (TransactionContext context = getTransactionService().getContext()) {
            MetrologyConfigurationService upcService = getMetrologyConfigurationService();
            upcService.newMetrologyConfiguration("dup1");
            upcService.newMetrologyConfiguration("dup1");
            context.commit();
        }
    }

    @Test(expected = ConstraintViolationException.class)
    public void testDuplicateNameRename() {
        try (TransactionContext context = getTransactionService().getContext()) {
            MetrologyConfigurationService upcService = getMetrologyConfigurationService();
            upcService.newMetrologyConfiguration("dup2");
            MetrologyConfiguration x = upcService.newMetrologyConfiguration("x");
            x.updateName("dup2");
            context.commit();
        }
    }

}