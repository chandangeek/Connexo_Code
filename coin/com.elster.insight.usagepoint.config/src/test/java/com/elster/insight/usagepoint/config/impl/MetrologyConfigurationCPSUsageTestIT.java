package com.elster.insight.usagepoint.config.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.transaction.TransactionContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MetrologyConfigurationCPSUsageTestIT {
    private static MetrologyInMemoryBootstrapModule inMemoryBootstrapModule = new MetrologyInMemoryBootstrapModule();
    private static MetrologyTestCustomPropertySet customPropertySet;

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
        customPropertySet = new MetrologyTestCustomPropertySet(inMemoryBootstrapModule.getPropertySpecService());
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Before
    public void beforeTest() {
        inMemoryBootstrapModule.getCustomPropertySetService().addCustomPropertySet(customPropertySet);
    }

    private void inTransaction(Consumer<TransactionContext> worker) {
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            worker.accept(context);
        }
    }

    private MetrologyConfiguration getMetrologyConfiguration() {
        return inMemoryBootstrapModule.getUsagePointConfigurationService().newMetrologyConfiguration("Test metrology configuration");
    }

    @Test
    public void testCPSSuccessfullyRegistered() {
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = inMemoryBootstrapModule.getCustomPropertySetService()
                .findActiveCustomPropertySet(customPropertySet.getId());
        assertThat(registeredCustomPropertySet).isPresent();
    }

    private RegisteredCustomPropertySet getRegisteredCPS() {
        return inMemoryBootstrapModule.getCustomPropertySetService().findActiveCustomPropertySet(customPropertySet.getId()).get();
    }

    @Test
    public void testAddCPSToMetrologyConfig() {
        inTransaction(ctx -> {
            MetrologyConfiguration metrologyConfiguration = getMetrologyConfiguration();
            metrologyConfiguration.addCustomPropertySet(getRegisteredCPS());

            assertThat(metrologyConfiguration.getCustomPropertySets()).hasSize(1);
            assertThat(metrologyConfiguration.getCustomPropertySets().get(0).getCustomPropertySet().getId())
                    .isEqualTo(customPropertySet.getId());
        });
    }

    @Test
    public void testAddTheSameCPSTwice() {
        inTransaction(ctx -> {
            MetrologyConfiguration metrologyConfiguration = getMetrologyConfiguration();
            metrologyConfiguration.addCustomPropertySet(getRegisteredCPS());
            metrologyConfiguration.addCustomPropertySet(getRegisteredCPS());

            assertThat(metrologyConfiguration.getCustomPropertySets()).hasSize(1);
            assertThat(metrologyConfiguration.getCustomPropertySets().get(0).getCustomPropertySet().getId())
                    .isEqualTo(customPropertySet.getId());
        });
    }

    @Test
    public void testRemoveNotAddedCPS() {
        inTransaction(ctx -> {
            MetrologyConfiguration metrologyConfiguration = getMetrologyConfiguration();
            metrologyConfiguration.removeCustomPropertySet(getRegisteredCPS());

            assertThat(metrologyConfiguration.getCustomPropertySets()).hasSize(0);
            // plus we expect no exceptions
        });
    }

    @Test
    public void testAddCPSAndRemoveAfterThat() {
        inTransaction(ctx -> {
            MetrologyConfiguration metrologyConfiguration = getMetrologyConfiguration();
            RegisteredCustomPropertySet registeredCPS = getRegisteredCPS();
            metrologyConfiguration.addCustomPropertySet(registeredCPS);
            metrologyConfiguration.removeCustomPropertySet(registeredCPS);

            assertThat(metrologyConfiguration.getCustomPropertySets()).hasSize(0);
        });
    }

    @Test
    public void testAddCPSAndRemoveItTwice() {
        inTransaction(ctx -> {
            MetrologyConfiguration metrologyConfiguration = getMetrologyConfiguration();
            RegisteredCustomPropertySet registeredCPS = getRegisteredCPS();
            metrologyConfiguration.addCustomPropertySet(registeredCPS);
            metrologyConfiguration.removeCustomPropertySet(registeredCPS);
            metrologyConfiguration.removeCustomPropertySet(registeredCPS);

            assertThat(metrologyConfiguration.getCustomPropertySets()).hasSize(0);
            // plus we expect no exceptions
        });
    }
}
